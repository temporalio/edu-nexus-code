// The Compliance team's Nexus handler. Payments depends on the contract in
// shared/nexus-service.ts. This file is the code that answers those calls, and only
// the Compliance team owns it.
import * as nexus from 'nexus-rpc';
import * as temporalNexus from '@temporalio/nexus';
import { complianceService } from '../shared/nexus-service';
import { ComplianceRequest, ComplianceResult, ReviewRequest } from '../shared/types';
import { complianceWorkflow, reviewUpdate } from './workflows';

/** Same ID both times: one Operation creates the Workflow, the other looks it up. */
const workflowIdFor = (transactionId: string) => `compliance-${transactionId}`;

export const complianceServiceHandler = nexus.serviceHandler(complianceService, {
  // The SLOW call. A risky payment may need a person to approve it, so an answer can be
  // minutes or hours away. This Operation must not sit and wait for it.
  //
  // WorkflowRunOperationHandler starts a Workflow and returns immediately, handing back
  // a reference. Temporal holds that reference and collects the result whenever the
  // Workflow finishes.
  //
  // A plain async function would NOT work here: a synchronous Operation is cut off at
  // the 10-second handler deadline, and when Temporal retried the failed call it would
  // start a SECOND compliance check for the same payment.
  checkCompliance: new temporalNexus.WorkflowRunOperationHandler<ComplianceRequest, ComplianceResult>(
    async (ctx, input: ComplianceRequest) =>
      temporalNexus.startWorkflow(ctx, complianceWorkflow, {
        args: [input],
        // Business-meaningful and deduped: a retried Operation attaches to the same
        // compliance check rather than starting a second one.
        workflowId: workflowIdFor(input.transactionId),
        // Task queue defaults to the queue this Operation is handled on.
      }),
  ),

  // The FAST call. By now the compliance check is running and parked waiting for a
  // decision. All this does is find that Workflow and hand it the yes or no — a few
  // milliseconds — so a synchronous handler is the right shape.
  submitReview: async (_ctx, input: ReviewRequest): Promise<ComplianceResult> => {
    const client = temporalNexus.getClient();
    const handle = client.workflow.getHandle(workflowIdFor(input.transactionId));
    return await handle.executeUpdate(reviewUpdate, {
      args: [input.approved, input.explanation],
    });
  },
});
