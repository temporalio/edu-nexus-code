package compliance.temporal.workflow

import compliance.domain.ComplianceRequest
import compliance.domain.ComplianceResult
import io.temporal.workflow.UpdateMethod
import io.temporal.workflow.UpdateValidatorMethod
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

/**
 * [GIVEN] Compliance Workflow. Pre-built for this exercise.
 *
 * Runs an automated compliance check and, for MEDIUM-risk transactions, waits for a
 * human reviewer to approve or deny via a Workflow Update.
 *
 * LOW risk  -> auto-approved, returns immediately
 * HIGH risk -> auto-denied, returns immediately
 * MEDIUM    -> pauses, waits for the review() Update
 */
@WorkflowInterface
interface ComplianceWorkflow {

    @WorkflowMethod
    fun run(request: ComplianceRequest): ComplianceResult

    @UpdateMethod
    fun review(approved: Boolean, explanation: String): ComplianceResult

    @UpdateValidatorMethod(updateName = "review")
    fun validateReview(approved: Boolean, explanation: String)
}
