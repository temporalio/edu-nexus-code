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

// TODO 4(a) deletes this proxy. Until then it is what couples the two teams: a call on it
// schedules an Activity, and Activities run on THIS Worker.
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

  // ── TODO 4 ────────────────────────────────────────────────────────────────────
  // Two calls, one client. Do both — (b) is at the bottom of this file.
  //
  // (a) HERE. Replace this Activity call, then delete the proxy above and its
  //     `complianceActivities` import:
  //
  //       const compliance = wf.createNexusServiceClient({
  //         service: complianceService,      // '../shared/nexus-service'
  //         endpoint: COMPLIANCE_ENDPOINT,   // '../shared/types'
  //       });
  //       const result = await compliance.executeOperation('checkCompliance', compReq, {
  //         scheduleToCloseTimeout: '10 minutes',
  //       });
  //
  // (b) reviewCallerWorkflow. Same client, but 'submitReview' at '10 seconds'.
  //
  // The two numbers are the lesson. checkCompliance is asynchronous: ten minutes covers
  // the whole call including retries, which is what lets it outlive the Compliance
  // Worker going away. submitReview is synchronous: the handler must answer in ten.
  //
  // Note what the Workflow names — a contract and an Endpoint. No Namespace, no Task
  // Queue, no address. The Registry resolves those, so this Workflow does not change
  // when Compliance moves.
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

// ── TODO 4 (b) ──────────────────────────────────────────────────────────────────────
// The second half. Same client as above; 'submitReview' at '10 seconds'.
//
// Routing the review through the Endpoint is what keeps the boundary: neither team
// learns the other's Workflow IDs.
export async function reviewCallerWorkflow(_request: ReviewRequest): Promise<ComplianceResult> {
  throw new Error('TODO 4(b): submit the review decision through the Nexus Endpoint');
}
