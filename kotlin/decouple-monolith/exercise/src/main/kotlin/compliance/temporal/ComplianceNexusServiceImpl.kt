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
 * The Compliance team's Nexus handler. This is the kitchen behind the menu: Payments
 * depends on the contract, only this team owns the implementation.
 *
 * This is the hardest file in the lab. Rather than guessing, ask:
 *   "Ask AI" on https://docs.temporal.io ->
 *     "how do I start a Workflow from a Nexus Operation handler in Java?"
 *     "when should a Nexus Operation be synchronous instead of asynchronous?"
 *   Or read https://docs.temporal.io/develop/java/nexus/feature-guide, which shows both
 *   handler shapes side by side. Remember: Java docs, Kotlin syntax.
 *
 * Two things you will need in both handlers:
 *   - Nexus.getOperationContext().workflowClient gives you a Client for the Namespace
 *     this Worker is connected to.
 *   - The handler Workflow ID is "compliance-{transactionId}", and the Task Queue is
 *     "compliance-risk", matching the Endpoint's --target-task-queue.
 */
// ── TODO 2a ──────────────────────────────────────────────────────────────────────
// Annotate this class so Temporal can route incoming Operations to it. The annotation
// has to name the contract interface this class implements.
class ComplianceNexusServiceImpl {

    // ── TODO 2b ──────────────────────────────────────────────────────────────────
    // Mark this method as the handler for the checkCompliance Operation. Miss it and
    // the Worker refuses to start with "Missing handlers for service operations".
    //
    // ── TODO 2c ──────────────────────────────────────────────────────────────────
    // Return an ASYNC handler that starts a ComplianceWorkflow.
    //
    // Reach for WorkflowRunOperation, not OperationHandler.sync. It returns a handle
    // that binds this Operation to a Workflow ID, so a retry re-attaches to the
    // Workflow already running instead of starting a duplicate. A sync handler would
    // start a fresh Workflow on every retry, and would blow past the 10-second sync
    // deadline anyway, because a compliance check can wait on a human.
    //
    // Two pieces to assemble: a ComplianceWorkflow stub configured with WorkflowOptions
    // (Task Queue and Workflow ID), and a WorkflowHandle built from that stub's Workflow
    // method. The imports above are exactly the ones you need, and nothing more.
    fun checkCompliance(): OperationHandler<ComplianceRequest, ComplianceResult> {
        TODO("TODO 2c: return an async handler backed by ComplianceWorkflow")
    }

    // ── TODO 2d ──────────────────────────────────────────────────────────────────
    // Mark this method as the handler for the submitReview Operation, then return a
    // SYNC handler that forwards a review decision.
    //
    // This one interacts with a Workflow that is already running rather than starting
    // one, so OperationHandler.sync is correct here. Look up the existing
    // "compliance-{transactionId}" Workflow by ID and call its review() Update, which
    // returns immediately and so fits inside the 10-second sync handler deadline.
    //
    // One Kotlin wrinkle: OperationHandler.sync declares its input @Nullable, so you
    // will need a non-null assertion on it.
    fun submitReview(): OperationHandler<ReviewRequest, ComplianceResult> {
        TODO("TODO 2d: return a sync handler that sends the review Update")
    }
}
