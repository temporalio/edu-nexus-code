package payments.temporal

import compliance.domain.ComplianceRequest
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
 * DECOUPLED VERSION. The compliance check goes through Nexus.
 *
 * The only change from the monolith:
 *   ComplianceActivity stub -> ComplianceNexusService stub
 *
 * Same method name, same input, same output, different architecture.
 *
 * On failures: this Workflow returns a result for business outcomes (validation
 * rejected, compliance declined) and lets infrastructure failures propagate. An
 * ActivityFailure or NexusOperationFailure fails the Workflow, so a broken Endpoint
 * mapping shows up red in the Web UI instead of hiding inside the result of a
 * Workflow that claims to have completed.
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

    // The Nexus Service stub replaces the ComplianceActivity stub. Note there is no
    // endpoint name here: the Workflow only knows the contract. The Worker maps the
    // contract to an Endpoint, which keeps this Workflow portable.
    //
    // scheduleToCloseTimeout is what keeps the Operation alive across a handler outage.
    private val complianceService: ComplianceNexusService = Workflow.newNexusServiceStub(
        ComplianceNexusService::class.java,
        NexusServiceOptions.newBuilder()
            .setOperationOptions(
                NexusOperationOptions.newBuilder()
                    .setScheduleToCloseTimeout(Duration.ofMinutes(10))
                    .build()
            )
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

        // Step 2: Compliance check via Nexus (Compliance team)
        val compReq = ComplianceRequest(
            request.transactionId,
            request.amount,
            request.senderCountry,
            request.receiverCountry,
            request.description,
        )

        logger.info("Step 2: calling compliance check via Nexus for ${request.transactionId}")

        val compliance = complianceService.checkCompliance(compReq)

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
