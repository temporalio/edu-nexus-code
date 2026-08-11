package payments.temporal

import compliance.domain.ComplianceResult
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import shared.domain.ReviewRequest

/**
 * [GIVEN] Caller Workflow that submits a compliance review decision through Nexus.
 *
 * Routing the review through the Endpoint respects the team boundary: neither team
 * needs to know the other's Workflow IDs or internal method names.
 */
@WorkflowInterface
interface ReviewCallerWorkflow {

    @WorkflowMethod
    fun submitReview(request: ReviewRequest): ComplianceResult
}
