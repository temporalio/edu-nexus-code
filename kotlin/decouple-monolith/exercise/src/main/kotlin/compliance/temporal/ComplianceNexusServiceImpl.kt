package compliance.temporal

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import compliance.temporal.workflow.ComplianceWorkflow
import io.nexusrpc.handler.OperationHandler
import io.temporal.client.WorkflowOptions
import io.temporal.nexus.Nexus
import io.temporal.nexus.WorkflowHandle
import io.temporal.nexus.WorkflowRunOperation
import shared.domain.ReviewRequest
import shared.nexus.ComplianceNexusService

/**
 * The Compliance team's Nexus handler. Payments depends on the contract in
 * ComplianceNexusService. This file is the code that answers those calls, and only
 * the Compliance team owns it.
 *
 * This is the hardest file in the lab. Two things you need in both methods:
 *
 *   Nexus.getOperationContext().workflowClient
 *     A Temporal Client, already connected to this team's Namespace.
 *
 *   "compliance-{transactionId}"
 *     The Workflow ID to use. Same ID both times: one method creates that Workflow,
 *     the other looks up the same one. The Task Queue is "compliance-risk".
 *
 * Stuck? "Ask AI" on https://docs.temporal.io:
 *   "how do I start a Workflow from a Nexus Operation handler in Java?"
 * Remember to search the Java docs, not Kotlin.
 */
// ── TODO 4 ──────────────────────────────────────────────────────────────────────
// Annotate this class so Temporal knows it answers calls for the contract. The
// annotation has to name the interface this class implements.
class ComplianceNexusServiceImpl {

    // ── TODO 5 ──────────────────────────────────────────────────────────────────
    // Two things here: annotate this method as the code that runs when Payments calls
    // checkCompliance, then write its body. Miss the annotation and the Worker refuses
    // to start with "Missing handlers for service operations".
    //
    // This is the SLOW call. A risky payment has to be approved by a person, so an
    // answer can be minutes or hours away. This method must not sit and wait for it.
    //
    // Instead: start a ComplianceWorkflow and return immediately, handing back a
    // reference to it. Temporal holds onto that reference and collects the result
    // whenever the Workflow finishes. Use WorkflowRunOperation for this.
    //
    // Do NOT use OperationHandler.sync here. That one waits for the answer, and it
    // gets cut off after 10 seconds. Worse, when Temporal retries the failed call it
    // would start a SECOND compliance check for the same payment.
    //
    // You need two pieces:
    //   1. a ComplianceWorkflow stub, built with WorkflowOptions that set the Task
    //      Queue and the Workflow ID
    //   2. a WorkflowHandle built from that stub's Workflow method
    // The imports at the top are exactly the ones you need, and nothing more.
    fun checkCompliance(): OperationHandler<ComplianceRequest, ComplianceResult> {
        TODO("TODO 5: start a ComplianceWorkflow and return a reference to it")
    }

    // ── TODO 6 ──────────────────────────────────────────────────────────────────
    // Mark this method as the code that runs when Payments calls submitReview, then
    // fill it in.
    //
    // This is the FAST call. By now the compliance check is already running and is
    // parked waiting for a decision. All you do is find that Workflow and hand it the
    // yes or no. That takes milliseconds, so this method can wait for the answer and
    // return it in the same call. Use OperationHandler.sync.
    //
    // Look the Workflow up by its ID, then call review() on it.
    //
    // One Kotlin wrinkle: OperationHandler.sync says its input can be null, so Kotlin
    // sees ReviewRequest? and you need a non-null assertion (!!) before using it.
    fun submitReview(): OperationHandler<ReviewRequest, ComplianceResult> {
        TODO("TODO 6: find the running Workflow and give it the review decision")
    }
}
