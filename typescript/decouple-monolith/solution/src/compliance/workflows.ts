// [GIVEN] Compliance Workflow. Pre-built for this exercise.
//
// Runs an automated compliance check and, for MEDIUM-risk transactions, waits for a
// human reviewer to approve or deny via a Workflow Update.
//
//   LOW risk  -> auto-approved, returns immediately
//   HIGH risk -> auto-denied, returns immediately
//   MEDIUM    -> pauses, waits for the review Update
//
// The 10-second sleep is intentional. It gives you a window to stop the Compliance
// Worker with Ctrl+C and restart it, so you can watch the Nexus Operation survive the
// outage. The timer runs on the server, so nothing is lost when the Worker goes away.
import * as wf from '@temporalio/workflow';
import type * as activities from './activities';
import { ComplianceRequest, ComplianceResult } from '../shared/types';

const { checkCompliance } = wf.proxyActivities<typeof activities>({
  startToCloseTimeout: '30 seconds',
});

/** Update sent by the Compliance team's sync Nexus handler when a human decides. */
export const reviewUpdate = wf.defineUpdate<ComplianceResult, [boolean, string]>('review');

export async function complianceWorkflow(request: ComplianceRequest): Promise<ComplianceResult> {
  let reviewResult: ComplianceResult | undefined;
  let autoResult: ComplianceResult | undefined;

  wf.setHandler(
    reviewUpdate,
    (approved, explanation) => {
      const result: ComplianceResult = {
        transactionId: request.transactionId,
        approved,
        riskLevel: 'MEDIUM',
        explanation,
      };
      reviewResult = result;
      return result;
    },
    {
      validator: (_approved: boolean, _explanation: string) => {
        if (autoResult?.riskLevel !== 'MEDIUM') throw new Error('Workflow is not awaiting review');
        if (reviewResult !== undefined) throw new Error('Review already submitted');
      },
    },
  );

  // Step 1: automated compliance check.
  const auto = await checkCompliance(request);
  autoResult = auto;

  // Durable delay. Stop the Compliance Worker mid-sleep, restart it, and the Workflow
  // resumes on its own.
  await wf.sleep('10 seconds');

  // Step 2: LOW or HIGH risk returns immediately.
  if (auto.riskLevel !== 'MEDIUM') return auto;

  // Step 3: MEDIUM risk waits for human review.
  //
  // Bounded on purpose. An unbounded wait would leave this Workflow Running forever
  // when nobody reviews. Eight minutes is deliberately shorter than the caller's
  // 10-minute scheduleToCloseTimeout, so the handler resolves and reports a decision
  // rather than letting the caller time out first.
  const reviewed = await wf.condition(() => reviewResult !== undefined, '8 minutes');
  if (!reviewed) {
    return {
      transactionId: request.transactionId,
      approved: false,
      riskLevel: 'MEDIUM',
      explanation: 'No reviewer responded within 8 minutes. Not approved.',
    };
  }
  return reviewResult!;
}
