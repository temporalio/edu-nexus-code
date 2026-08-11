package payments.temporal

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import payments.Shared
import shared.domain.ReviewRequest
import kotlin.system.exitProcess

/**
 * [GIVEN] Starts a ReviewCallerWorkflow to approve TXN-B via Nexus.
 *
 * Rather than calling 'temporal workflow update execute' directly, this triggers a
 * caller Workflow that invokes the submitReview Nexus Operation. The Compliance team's
 * sync handler forwards the decision to the running ComplianceWorkflow as an Update.
 *
 * Flip approved to false below to see the denial path.
 */
fun main() {
    println("==========================================================")
    println("  REVIEW STARTER - Submitting review for TXN-B via Nexus")
    println("==========================================================")
    println()

    val service = WorkflowServiceStubs.newLocalServiceStubs()
    val client = WorkflowClient.newInstance(
        service,
        WorkflowClientOptions.newBuilder()
            .setNamespace("payments-namespace")
            .build(),
    )

    val request = ReviewRequest("TXN-B", true, "Approved after manual review")

    val workflow = client.newWorkflowStub(
        ReviewCallerWorkflow::class.java,
        WorkflowOptions.newBuilder()
            .setTaskQueue(Shared.TASK_QUEUE)
            .setWorkflowId("review-TXN-B")
            .build(),
    )

    println("  Submitting review for TXN-B via Nexus...")
    println("  Approved: ${request.approved}")
    println("  Explanation: ${request.explanation}")
    println()

    val result = workflow.submitReview(request)

    println("  Review result: ${if (result.approved) "APPROVED" else "DENIED"}")
    println("  Risk level:    ${result.riskLevel}")
    println("  Explanation:   ${result.explanation}")
    println()
    println("  TXN-B review submitted. The payment Workflow will now complete.")
    println("==========================================================")

    exitProcess(0)
}
