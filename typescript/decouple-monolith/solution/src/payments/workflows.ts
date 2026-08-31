// DECOUPLED VERSION. Compliance is now called across a Nexus boundary.
//
// Three steps:
//   Step 1: validatePayment  (Activity, this Worker)
//   Step 2: checkCompliance  (Nexus Operation, the Compliance team's Worker)
//   Step 3: executePayment   (Activity, this Worker)
import * as wf from '@temporalio/workflow';
import type * as activities from './activities';
import { complianceService } from '../shared/nexus-service';
import {
  COMPLIANCE_ENDPOINT,
  ComplianceRequest,
  ComplianceResult,
  PaymentRequest,
  PaymentResult,
  ReviewRequest,
} from '../shared/types';

const { validatePayment, executePayment } = wf.proxyActivities<typeof activities>({
  startToCloseTimeout: '30 seconds',
  retry: { initialInterval: '1 second', backoffCoefficient: 2 },
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

  // Step 2: compliance check, across the team boundary.
  //
  // This client is what replaced the Activity stub. Calls on it do not run code in this
  // process — they go out through the Endpoint to whichever Worker owns the Service.
  //
  // The 10 minutes is the budget for the whole call, retries included. It is what lets
  // the Operation survive the Compliance Worker going away instead of failing the moment
  // it does.
  const compliance = wf.createNexusServiceClient({
    service: complianceService,
    endpoint: COMPLIANCE_ENDPOINT,
  });

  const compReq: ComplianceRequest = {
    transactionId: request.transactionId,
    amount: request.amount,
    senderCountry: request.senderCountry,
    receiverCountry: request.receiverCountry,
    description: request.description,
  };

  logger.info(`Step 2: calling compliance check for ${request.transactionId}`);
  const result: ComplianceResult = await compliance.executeOperation('checkCompliance', compReq, {
    scheduleToCloseTimeout: '10 minutes',
  });

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

/**
 * [GIVEN] Sends a compliance review decision through Nexus.
 *
 * Routing the review through the Endpoint respects the team boundary: neither team needs
 * to know the other's Workflow IDs or internal method names.
 *
 * submitReview is a synchronous Operation, so the Compliance handler must finish inside
 * the 10-second handler deadline. That is why the timeout here is 10 seconds, not the
 * 10 minutes used for the async check.
 */
export async function reviewCallerWorkflow(request: ReviewRequest): Promise<ComplianceResult> {
  const compliance = wf.createNexusServiceClient({
    service: complianceService,
    endpoint: COMPLIANCE_ENDPOINT,
  });
  return await compliance.executeOperation('submitReview', request, {
    scheduleToCloseTimeout: '10 seconds',
  });
}
