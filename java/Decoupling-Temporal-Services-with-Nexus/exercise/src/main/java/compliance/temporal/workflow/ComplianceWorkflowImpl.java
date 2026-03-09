package compliance.temporal.workflow;

// ═══════════════════════════════════════════════════════════════════
//  TODO 2: Implement the ComplianceWorkflow
// ═══════════════════════════════════════════════════════════════════
//
// This workflow:
//   1. Runs ComplianceActivity.checkCompliance() on the request
//   2. If LOW or HIGH risk → returns immediately
//   3. If MEDIUM risk → waits for a human review() Update
//
// ── What to implement: ──────────────────────────────────────────
//
//   Fields:
//     - ComplianceRequest request (store the input)
//     - ComplianceResult autoResult (store the automated check result)
//     - ComplianceResult reviewResult = null (set by review Update)
//     - An activity stub for ComplianceActivity (30s startToCloseTimeout)
//
//   run(ComplianceRequest request):
//     1. Store this.request = request
//     2. Call complianceActivity.checkCompliance(request) → autoResult
//     3. If autoResult risk is NOT "MEDIUM" → return autoResult
//     4. Otherwise: Workflow.await(() -> reviewResult != null)
//     5. Return reviewResult
//
//   review(boolean approved, String explanation):
//     - Create a new ComplianceResult with the review decision
//     - Store it in reviewResult (this unblocks run())
//     - Return reviewResult
//
//   validateReview(boolean approved, String explanation):
//     - Throw IllegalStateException if not awaiting review
//     - Throw IllegalStateException if review already submitted

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import compliance.temporal.activity.ComplianceActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class ComplianceWorkflowImpl implements ComplianceWorkflow {

    private ComplianceRequest request;
    private ComplianceResult autoResult;
    private ComplianceResult reviewResult = null;

    private final ComplianceActivity complianceActivity = Workflow.newActivityStub(
            ComplianceActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .build());

    @Override
    public ComplianceResult run(ComplianceRequest request) {
        // TODO: Store the request, run the activity, check risk level
        //       If MEDIUM → Workflow.await(() -> reviewResult != null)
        //       Otherwise → return autoResult directly
        return null;
    }

    @Override
    public ComplianceResult review(boolean approved, String explanation) {
        // TODO: Create ComplianceResult from the review decision, store in reviewResult
        return null;
    }

    @Override
    public void validateReview(boolean approved, String explanation) {
        // TODO: Throw IllegalStateException if not awaiting review or already reviewed
    }
}
