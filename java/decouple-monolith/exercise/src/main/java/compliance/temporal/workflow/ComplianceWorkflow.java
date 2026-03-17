package compliance.temporal.workflow;

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * [GIVEN] Compliance workflow — pre-built for this exercise.
 *
 * Runs an automated compliance check and, for MEDIUM-risk transactions,
 * waits for a human reviewer to approve or deny via Workflow Update.
 *
 * LOW risk  → auto-approved, returns immediately
 * HIGH risk → auto-denied, returns immediately
 * MEDIUM    → pauses, waits for review() Update
 *
 * Your work starts at TODO 3. No changes needed here.
 */
@WorkflowInterface
public interface ComplianceWorkflow {

    @WorkflowMethod
    ComplianceResult run(ComplianceRequest request);

    @UpdateMethod
    ComplianceResult review(boolean approved, String explanation);

    @UpdateValidatorMethod(updateName = "review")
    void validateReview(boolean approved, String explanation);
}
