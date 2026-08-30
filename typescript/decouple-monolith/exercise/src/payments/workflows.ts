// MONOLITH VERSION. This already works — you run it in challenge 01.
//
// Three steps, all on one Worker:
//   Step 1: validatePayment  (payment Activity)
//   Step 2: checkCompliance  (compliance Activity)  <- this one crosses a team boundary
//   Step 3: executePayment   (payment Activity)
import * as wf from '@temporalio/workflow';
import type * as paymentActivities from './activities';
import type * as complianceActivities from '../compliance/activities';
import {
  ComplianceRequest,
  ComplianceResult,
  PaymentRequest,
  PaymentResult,
  ReviewRequest,
} from '../shared/types';

const { validatePayment, executePayment } = wf.proxyActivities<typeof paymentActivities>({
  startToCloseTimeout: '30 seconds',
  retry: { initialInterval: '1 second', backoffCoefficient: 2 },
});

// ── TODO 5 ──────────────────────────────────────────────────────────────────────────
// Delete the `checkCompliance` proxy below and build a Nexus Service client instead.
//
// What that proxy is: a fake object you call like a normal function. It does not run the
// code — it tells Temporal to schedule an Activity, and Activities run on THIS Worker.
// That is the coupling you are removing.
//
// A Nexus Service client is the same idea, but calls on it go out through an Endpoint to
// whichever Worker owns the Service. Build it INSIDE the Workflow function, like this:
//
//     const compliance = wf.createNexusServiceClient({
//       service: complianceService,          // import from '../shared/nexus-service'
//       endpoint: COMPLIANCE_ENDPOINT,       // import from '../shared/types'
//     });
//
// then in Step 2, replace the call with:
//
//     await compliance.executeOperation('checkCompliance', compReq, {
//       scheduleToCloseTimeout: '10 minutes',
//     })
//
// The 10 minutes is the budget for the whole call, retries included. It is what lets the
// Operation survive the Compliance Worker going away, instead of failing the moment it
// does. You prove that in challenge 05.
//
// Note what the Workflow names: a contract and an Endpoint, and nothing else. No
// Namespace, no Task Queue, no address. The Registry resolves the Endpoint name into
// those, which is what keeps this Workflow unchanged when Compliance moves.
//
// Stuck? "Ask AI" on https://docs.temporal.io:
//   "how do I call a Nexus Operation from a Workflow in TypeScript?"
const { checkCompliance } = wf.proxyActivities<typeof complianceActivities>({
  startToCloseTimeout: '30 seconds',
});

export async function paymentProcessingWorkflow(request: PaymentRequest): Promise<PaymentResult> {
  const logger = wf.log;

  // Step 1: validate (Payments team).
  if (!(await validatePayment(request))) {
    return {
      success: false,
      transactionId: request.transactionId,
      status: 'REJECTED',
      error: 'Payment validation failed',
    };
  }
  logger.info(`Step 1 passed: validation OK for ${request.transactionId}`);

  // Step 2: compliance check.
  const compReq: ComplianceRequest = {
    transactionId: request.transactionId,
    amount: request.amount,
    senderCountry: request.senderCountry,
    receiverCountry: request.receiverCountry,
    description: request.description,
  };

  logger.info(`Step 2: calling compliance check for ${request.transactionId}`);

  // This one line is the entire difference between running Compliance code in this
  // process and calling another team's service across a durable boundary.
  const result: ComplianceResult = await checkCompliance(compReq);

  logger.info(`Compliance result: ${result.riskLevel} | approved=${result.approved}`);

  // A declined payment is a business outcome, not a failure. The Workflow completes
  // successfully and reports the decision.
  if (!result.approved) {
    return {
      success: false,
      transactionId: request.transactionId,
      status: 'DECLINED_COMPLIANCE',
      riskLevel: result.riskLevel,
      explanation: result.explanation,
    };
  }

  // Step 3: execute, only if compliance approved.
  logger.info(`Step 3: executing payment for ${request.transactionId}`);
  const confirmationNumber = await executePayment(request);

  return {
    success: true,
    transactionId: request.transactionId,
    status: 'COMPLETED',
    riskLevel: result.riskLevel,
    explanation: result.explanation,
    confirmationNumber,
  };
}

// ── TODO 6 ──────────────────────────────────────────────────────────────────────────
// You come back to this one in challenge 05, once the Nexus path works end to end.
//
// Submitting a human review through the Endpoint respects the team boundary: neither
// team needs to know the other's Workflow IDs or internal method names.
//
// submitReview is a SYNCHRONOUS Operation, so the Compliance handler must finish inside
// the 10-second handler deadline. Use a 10-second scheduleToCloseTimeout here, not the
// 10 minutes used for the async check.
export async function reviewCallerWorkflow(_request: ReviewRequest): Promise<ComplianceResult> {
  throw new Error('TODO 6: submit the review decision through the Nexus Endpoint');
}
