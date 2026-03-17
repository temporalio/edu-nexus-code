package compliance.temporal;

// ═══════════════════════════════════════════════════════════════════
//  TODO 2: Implement the checkCompliance Nexus handler (async)
//  TODO 6b: Implement the submitReview Nexus handler (sync)
// ═══════════════════════════════════════════════════════════════════

// ── TODO 2: checkCompliance (async — fromWorkflowHandle) ────────────
//
// This handler receives compliance check requests from the Payments team and
// starts a ComplianceWorkflow to process them.
//
// ⚠️  Use fromWorkflowHandle — NOT OperationHandler.sync()
//     fromWorkflowHandle ensures retries reuse the same workflow instead of
//     creating duplicate workflows. This is the correct pattern for long-running work.
//
//   1. Add @ServiceImpl(service = ComplianceNexusService.class) to the class
//   2. Annotate checkCompliance() with @OperationImpl
//   3. Return WorkflowRunOperation.fromWorkflowHandle(...)
//
//   return WorkflowRunOperation.fromWorkflowHandle((ctx, details, input) -> {
//       WorkflowClient client = Nexus.getOperationContext().getWorkflowClient();
//       ComplianceWorkflow wf = client.newWorkflowStub(
//               ComplianceWorkflow.class,
//               WorkflowOptions.newBuilder()
//                       .setTaskQueue("compliance-risk")
//                       .setWorkflowId("compliance-" + input.getTransactionId())
//                       .build());
//       return WorkflowHandle.fromWorkflowMethod(wf::run, input);
//   });
//
// ── TODO 6b: submitReview (sync — OperationHandler.sync) ────────────
//
// This handler receives a review decision (approve/deny) and forwards it to the
// already-running ComplianceWorkflow as a Workflow Update.
//
// Use OperationHandler.sync because the Update returns immediately (the workflow
// just stores the result). This completes well within the 10-second handler deadline.
//
// Key difference from TODO 2:
//   - TODO 2 uses fromWorkflowHandle because it STARTS a new long-running workflow
//   - TODO 6b uses OperationHandler.sync because it INTERACTS with an existing workflow
//
//   1. Annotate submitReview() with @OperationImpl
//   2. Return OperationHandler.sync(...)
//   3. Get the Temporal client via Nexus.getOperationContext().getWorkflowClient()
//   4. Create a stub to the already-running workflow using its ID
//   5. Call wf.review(...) to send the Update and return the result
//
//   return OperationHandler.sync((ctx, details, input) -> {
//       WorkflowClient client = Nexus.getOperationContext().getWorkflowClient();
//       ComplianceWorkflow wf = client.newWorkflowStub(
//               ComplianceWorkflow.class,
//               "compliance-" + input.getTransactionId());
//       return wf.review(input.isApproved(), input.getExplanation());
//   });

import compliance.domain.ComplianceRequest;
import compliance.domain.ComplianceResult;
import compliance.temporal.workflow.ComplianceWorkflow;
import io.nexusrpc.handler.OperationHandler;
import io.nexusrpc.handler.OperationImpl;
import io.nexusrpc.handler.ServiceImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.nexus.Nexus;
import io.temporal.nexus.WorkflowRunOperation;
import io.temporal.nexus.WorkflowHandle;
import shared.domain.ReviewRequest;
import shared.nexus.ComplianceNexusService;

// TODO: Add @ServiceImpl(service = ComplianceNexusService.class) annotation
public class ComplianceNexusServiceImpl {

    // TODO 2: Add @OperationImpl and implement checkCompliance() using fromWorkflowHandle
    public OperationHandler<ComplianceRequest, ComplianceResult> checkCompliance() {
        return null;
    }

    // TODO 6b: Add @OperationImpl and implement submitReview() using OperationHandler.sync
    public OperationHandler<ReviewRequest, ComplianceResult> submitReview() {
        return null;
    }
}
