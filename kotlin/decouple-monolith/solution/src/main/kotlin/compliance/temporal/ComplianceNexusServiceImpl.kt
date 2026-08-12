package compliance.temporal

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import compliance.temporal.workflow.ComplianceWorkflow
import io.nexusrpc.handler.OperationHandler
import io.nexusrpc.handler.OperationImpl
import io.nexusrpc.handler.ServiceImpl
import io.temporal.client.WorkflowOptions
import io.temporal.nexus.Nexus
import io.temporal.nexus.WorkflowHandle
import io.temporal.nexus.WorkflowRunOperation
import shared.domain.ReviewRequest
import shared.nexus.ComplianceNexusService

/**
 * Nexus Service handler. Receives cross-team calls from Payments.
 *
 * @ServiceImpl links this class to the contract so Temporal can route Operations to it.
 */
@ServiceImpl(service = ComplianceNexusService::class)
class ComplianceNexusServiceImpl {

    /**
     * Async handler. fromWorkflowHandle returns a handle bound to the Workflow ID, so a
     * retried Operation re-attaches to the existing Workflow instead of starting a duplicate.
     */
    @OperationImpl
    fun checkCompliance(): OperationHandler<ComplianceRequest, ComplianceResult> =
        WorkflowRunOperation.fromWorkflowHandle { _, _, input ->
            val client = Nexus.getOperationContext().workflowClient
            val wf = client.newWorkflowStub(
                ComplianceWorkflow::class.java,
                WorkflowOptions.newBuilder()
                    .setTaskQueue("compliance-risk")
                    .setWorkflowId("compliance-${input.transactionId}")
                    .build(),
            )
            WorkflowHandle.fromWorkflowMethod(wf::run, input)
        }

    /**
     * Sync handler. Interacts with an already-running Workflow rather than starting one,
     * and must finish inside the 10-second sync handler deadline.
     */
    @OperationImpl
    fun submitReview(): OperationHandler<ReviewRequest, ComplianceResult> =
        OperationHandler.sync { _, _, input ->
            // OperationHandler.sync declares its input @Nullable, so Kotlin sees
            // ReviewRequest? here and needs the assertion. The async handler above does not.
            val review = input!!
            val client = Nexus.getOperationContext().workflowClient
            val wf = client.newWorkflowStub(
                ComplianceWorkflow::class.java,
                "compliance-${review.transactionId}",
            )
            wf.review(review.approved, review.explanation)
        }
}
