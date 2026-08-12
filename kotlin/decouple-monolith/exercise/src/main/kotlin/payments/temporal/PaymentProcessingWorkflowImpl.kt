package payments.temporal

import compliance.domain.ComplianceRequest
import compliance.temporal.activity.ComplianceActivity
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.workflow.NexusOperationOptions
import io.temporal.workflow.NexusServiceOptions
import io.temporal.workflow.Workflow
import payments.domain.PaymentRequest
import payments.domain.PaymentResult
import payments.temporal.activity.PaymentActivity
import shared.nexus.ComplianceNexusService
import java.time.Duration

/**
 * MONOLITH VERSION. This already works, you run it in challenge 01.
 *
 * Three steps, all on one Worker:
 *   Step 1: validatePayment  (PaymentActivity)
 *   Step 2: checkCompliance  (ComplianceActivity)  <- this one crosses a team boundary
 *   Step 3: executePayment   (PaymentActivity)
 *
 * TODO 4 has two parts, each marked at the line it applies to.
 */
class PaymentProcessingWorkflowImpl : PaymentProcessingWorkflow {

    private val paymentActivity: PaymentActivity = Workflow.newActivityStub(
        PaymentActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setBackoffCoefficient(2.0)
                    .build()
            )
            .build(),
    )

    // ── TODO 4a ──────────────────────────────────────────────────────────────────
    // Delete the whole `complianceActivity` declaration below (all 6 lines, from
    // `private val` down to the closing `)`) and write a Nexus Service stub instead.
    //
    // What that declaration is: a stub is a fake object you call like a normal Kotlin
    // object. Under the hood it does not run the code, it tells Temporal to schedule
    // the work. `Workflow.newActivityStub` makes one that schedules an Activity, and
    // Activities run on THIS Worker. That is the coupling you are removing.
    //
    // `Workflow.newNexusServiceStub` makes the same kind of fake object, but calls on
    // it go out to whichever Worker owns the Nexus Service. Shape of what you write:
    //
    //     private val complianceService: ComplianceNexusService =
    //         Workflow.newNexusServiceStub(
    //             ComplianceNexusService::class.java,
    //             NexusServiceOptions.newBuilder()
    //                 // set a scheduleToCloseTimeout of 10 minutes here,
    //                 // using NexusOperationOptions
    //                 .build(),
    //         )
    //
    // The 10 minutes is the budget for the whole call, retries included. It is what
    // lets the Operation survive the Compliance Worker going away, instead of failing
    // the moment it does. You prove that in challenge 05.
    //
    // Notice what you will NOT write here: the Endpoint name. This Workflow knows the
    // contract; the Worker knows where the contract lives. That split is what keeps
    // this Workflow portable, and you wire it up next in PaymentsWorkerApp.kt (TODO 5).
    //
    // Stuck? "Ask AI" on https://docs.temporal.io:
    //   "how do I call a Nexus Operation from a Workflow in Java?"
    private val complianceActivity: ComplianceActivity = Workflow.newActivityStub(
        ComplianceActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build(),
    )

    override fun processPayment(request: PaymentRequest): PaymentResult {
        val logger = Workflow.getLogger(PaymentProcessingWorkflowImpl::class.java)

        // Step 1: Validate payment (Payments team)
        if (!paymentActivity.validatePayment(request)) {
            return PaymentResult(
                success = false,
                transactionId = request.transactionId,
                status = "REJECTED",
                error = "Payment validation failed",
            )
        }
        logger.info("Step 1 passed: validation OK for ${request.transactionId}")

        // Step 2: Compliance check
        val compReq = ComplianceRequest(
            request.transactionId,
            request.amount,
            request.senderCountry,
            request.receiverCountry,
            request.description,
        )

        logger.info("Step 2: calling compliance check for ${request.transactionId}")

        // ── TODO 4b ──────────────────────────────────────────────────────────────
        // On the line below, change `complianceActivity` to the stub you just created
        // in TODO 4a. One word. Everything else on the line stays: same method name,
        // same input, same result type.
        //
        // That one word is the entire difference between running Compliance code in
        // this process and calling another team's service across a durable boundary.
        val compliance = complianceActivity.checkCompliance(compReq)

        logger.info("Compliance result: ${compliance.riskLevel} | approved=${compliance.approved}")

        // A declined payment is a business outcome, not a failure. The Workflow
        // completes successfully and reports the decision.
        if (!compliance.approved) {
            return PaymentResult(
                success = false,
                transactionId = request.transactionId,
                status = "DECLINED_COMPLIANCE",
                riskLevel = compliance.riskLevel,
                explanation = compliance.explanation,
            )
        }

        // Step 3: Execute payment, only if compliance approved
        logger.info("Step 3: executing payment for ${request.transactionId}")
        val confirmation = paymentActivity.executePayment(request)

        return PaymentResult(
            success = true,
            transactionId = request.transactionId,
            status = "COMPLETED",
            riskLevel = compliance.riskLevel,
            explanation = compliance.explanation,
            confirmationNumber = confirmation,
        )
    }
}
