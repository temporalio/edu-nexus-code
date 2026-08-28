// The Compliance team's Nexus handler. Payments depends on the contract in
// shared/nexus-service.ts. This file is the code that answers those calls, and only
// the Compliance team owns it.
//
// This is the hardest file in the lab. Two things you will need:
//
//   temporalNexus.getClient()
//     A Temporal Client, already connected to this team's Namespace.
//
//   `compliance-${transactionId}`
//     The Workflow ID to use. Same ID both times: one Operation creates that Workflow,
//     the other looks up the same one.
//
// Stuck? "Ask AI" on https://docs.temporal.io:
//   "how do I start a Workflow from a Nexus Operation handler in TypeScript?"
import * as nexus from 'nexus-rpc';
import * as temporalNexus from '@temporalio/nexus';
import { complianceService } from '../shared/nexus-service';
import { ComplianceRequest, ComplianceResult, ReviewRequest } from '../shared/types';
import { complianceWorkflow, reviewUpdate } from './workflows';

/** Same ID both times: one Operation creates the Workflow, the other looks it up. */
const workflowIdFor = (transactionId: string) => `compliance-${transactionId}`;

export const complianceServiceHandler = nexus.serviceHandler(complianceService, {
  // ── TODO 2 ──────────────────────────────────────────────────────────────────────
  // Implement checkCompliance.
  //
  // This is the SLOW call. A risky payment has to be approved by a person, so an answer
  // can be minutes or hours away. This Operation must not sit and wait for it.
  //
  // Instead: start a complianceWorkflow and return immediately, handing back a reference
  // to it. Temporal holds that reference and collects the result whenever the Workflow
  // finishes. Use `new temporalNexus.WorkflowRunOperationHandler<I, O>(async (ctx, input) => ...)`
  // and call `temporalNexus.startWorkflow(ctx, complianceWorkflow, { args, workflowId })`
  // inside it.
  //
  // Do NOT write a plain async function here. That is a synchronous Operation: it is cut
  // off after 10 seconds, and when Temporal retries the failed call it would start a
  // SECOND compliance check for the same payment.

  // ── TODO 3 ──────────────────────────────────────────────────────────────────────
  // Implement submitReview.
  //
  // This is the FAST call. By now the compliance check is already running and is parked
  // waiting for a decision. All you do is find that Workflow and hand it the yes or no.
  // That takes milliseconds, so a plain `async (ctx, input) => { ... }` is the right
  // shape here.
  //
  // Get a client with temporalNexus.getClient(), get a handle to the Workflow by its ID,
  // then call executeUpdate with the `reviewUpdate` definition imported above.
});
