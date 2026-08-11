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
    // Replace this Activity stub with a Nexus Service stub for ComplianceNexusService.
    //
    // Use Workflow.newNexusServiceStub, and give its NexusOperationOptions a
    // scheduleToCloseTimeout of 10 minutes. That timeout is what lets the Operation
    // survive a handler Worker outage instead of failing the moment the handler
    // goes away. You prove that in challenge 05.
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
        // Call the Nexus stub you created above instead of the Activity stub.
        // Same method name, same input, same output. Only the architecture changes.
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
