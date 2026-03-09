package compliance.temporal.workflow;

// ═══════════════════════════════════════════════════════════════════
//  TODO 1: Define the ComplianceWorkflow interface
// ═══════════════════════════════════════════════════════════════════
//
// This workflow runs an automated compliance check and, for MEDIUM-risk
// transactions, waits for a human reviewer to approve or deny via Update.
//
// ── What to add: ────────────────────────────────────────────────
//
//   1. Add @WorkflowInterface annotation
//   2. Add @WorkflowMethod: ComplianceResult run(ComplianceRequest request)
//   3. Add @UpdateMethod:   ComplianceResult review(boolean approved, String explanation)
//   4. Add @UpdateValidatorMethod(updateName = "review"):
//                           void validateReview(boolean approved, String explanation)
//
// ── Template: ───────────────────────────────────────────────────
//
//   @WorkflowInterface
//   public interface ComplianceWorkflow {
//       @WorkflowMethod
//       ComplianceResult run(ComplianceRequest request);
//
//       @UpdateMethod
//       ComplianceResult review(boolean approved, String explanation);
//
//       @UpdateValidatorMethod(updateName = "review")
//       void validateReview(boolean approved, String explanation);
//   }

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

// TODO: Add @WorkflowInterface annotation
public interface ComplianceWorkflow {

    // TODO: Add @WorkflowMethod annotation
    ComplianceResult run(ComplianceRequest request);

    // TODO: Add @UpdateMethod annotation
    ComplianceResult review(boolean approved, String explanation);

    // TODO: Add @UpdateValidatorMethod(updateName = "review") annotation
    void validateReview(boolean approved, String explanation);
}
