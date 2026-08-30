// The Compliance team's Nexus handler. Payments depends on the contract in
// shared/nexus-service.ts. This file is the code that answers those calls, and only
// the Compliance team owns it.
//
// One Operation is written for you as a worked example. The other one is yours, and it
// is the opposite shape. Read the example first — the contrast between them is the
// whole point of this challenge.
//
// Two things you will need:
//
//   temporalNexus.getClient()
//     A Temporal Client, already connected to this team's Namespace.
//
//   workflowIdFor(transactionId)
//     The Workflow ID. The SAME id in both Operations: one creates that Workflow, the
//     other looks up the one already running.
//
// Stuck? "Ask AI" on https://docs.temporal.io:
//   "how do I send a Workflow Update from a Nexus Operation handler in TypeScript?"
import * as nexus from 'nexus-rpc';
import * as temporalNexus from '@temporalio/nexus';
import { complianceService } from '../shared/nexus-service';
import { ComplianceRequest, ComplianceResult, ReviewRequest } from '../shared/types';
import { complianceWorkflow, reviewUpdate } from './workflows';

/** Same ID both times: one Operation creates the Workflow, the other looks it up. */
const workflowIdFor = (transactionId: string) => `compliance-${transactionId}`;

export const complianceServiceHandler = nexus.serviceHandler(complianceService, {
  // ── WORKED EXAMPLE — read this, then write TODO 2 below ──────────────────────────
  //
  // checkCompliance is the SLOW call. A risky payment has to be approved by a person,
  // so an answer can be minutes or hours away. This Operation must not sit and wait.
  //
  // So it does not return an answer at all. It starts a Workflow and returns a
  // REFERENCE to it. Temporal holds that reference and collects the result whenever the
  // Workflow finishes, however long that takes.
  //
  // That is what WorkflowRunOperationHandler is for. Note the shape:
  //
  //   - it wraps a function of (ctx, input)
  //   - that function calls temporalNexus.startWorkflow(ctx, <workflow>, { ... })
  //   - the Workflow ID is business-meaningful, so a retried Operation re-attaches to
  //     the same compliance check instead of starting a second one
  //
  // A plain async function would NOT work here. That is a synchronous Operation: it is
  // cut off at the 10-second handler deadline, and each retry would start another
  // compliance check for the same payment.
  checkCompliance: new temporalNexus.WorkflowRunOperationHandler<ComplianceRequest, ComplianceResult>(
    async (ctx, input: ComplianceRequest) =>
      temporalNexus.startWorkflow(ctx, complianceWorkflow, {
        args: [input],
        workflowId: workflowIdFor(input.transactionId),
        // Task queue defaults to the queue this Operation is handled on.
      }),
  ),

  // ── TODO 2 ──────────────────────────────────────────────────────────────────────
  // Implement submitReview, the other half of the pattern.
  //
  // This is the FAST call, and it is the opposite shape to the example above. By the
  // time it arrives, the compliance check is already running and parked waiting for a
  // decision. All this Operation does is find that Workflow and hand it the yes or no.
  // That takes milliseconds, so it can answer inside the call.
  //
  // Which means: NOT a WorkflowRunOperationHandler. A plain async function is right
  // here. Slow work starts a Workflow and returns a reference; fast work answers in the
  // call. Same Service, two Operations, two shapes.
  //
  //     submitReview: async (_ctx, input: ReviewRequest): Promise<ComplianceResult> => {
  //       // 1. get a Client with temporalNexus.getClient()
  //       // 2. get a handle to the running Workflow, using workflowIdFor(...)
  //       // 3. return await handle.executeUpdate(reviewUpdate, { args: [...] })
  //     },
  //
  // `reviewUpdate` is already imported at the top of this file. It takes two arguments,
  // in this order: whether the reviewer approved, and their explanation.
  //
  // Until you write this, the build stays red — the contract in challenge 2 declared
  // two Operations and this handler only answers one of them.
});
