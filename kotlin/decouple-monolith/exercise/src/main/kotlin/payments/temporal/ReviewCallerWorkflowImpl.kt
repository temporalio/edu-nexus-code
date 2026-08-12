package payments.temporal

import compliance.domain.ComplianceResult
import io.temporal.workflow.NexusOperationOptions
import io.temporal.workflow.NexusServiceOptions
import io.temporal.workflow.Workflow
import shared.domain.ReviewRequest
import shared.nexus.ComplianceNexusService
import java.time.Duration

/**
 * [GIVEN] Sends a compliance review decision through Nexus.
 *
 * Calls submitReview as a sync Nexus Operation. The Compliance team's handler looks up
 * the compliance-{transactionId} Workflow and sends it an Update, all inside the
 * 10-second sync handler deadline. That short deadline is why the timeout here is 10
 * seconds, not the 10 minutes used for the async check.
 */
class ReviewCallerWorkflowImpl : ReviewCallerWorkflow {

    private val complianceService: ComplianceNexusService = Workflow.newNexusServiceStub(
        ComplianceNexusService::class.java,
        NexusServiceOptions.newBuilder()
            .setOperationOptions(
                NexusOperationOptions.newBuilder()
                    .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                    .build()
            )
            .build(),
    )

    override fun submitReview(request: ReviewRequest): ComplianceResult =
        complianceService.submitReview(request)
}
