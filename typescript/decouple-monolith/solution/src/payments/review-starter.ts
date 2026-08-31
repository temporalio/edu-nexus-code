// [GIVEN] Starts a reviewCallerWorkflow to approve TXN-B via Nexus.
//
// Rather than calling 'temporal workflow update execute' directly, this triggers a caller
// Workflow that invokes the submitReview Nexus Operation. The Compliance team's sync
// handler forwards the decision to the running complianceWorkflow as an Update.
//
// Flip `approved` to false below to see the denial path.
import { Client, Connection } from '@temporalio/client';
import { reviewCallerWorkflow } from './workflows';
import { PAYMENTS_NAMESPACE, PAYMENTS_TASK_QUEUE, ReviewRequest } from '../shared/types';

async function run() {
  console.log('==========================================================');
  console.log('  REVIEW STARTER - Submitting review for TXN-B via Nexus');
  console.log('==========================================================\n');

  const connection = await Connection.connect({ address: 'localhost:7233' });
  const client = new Client({ connection, namespace: PAYMENTS_NAMESPACE });

  const request: ReviewRequest = {
    transactionId: 'TXN-B',
    approved: true,
    explanation: 'Approved after manual review',
  };

  console.log('  Submitting review for TXN-B via Nexus...');
  console.log(`  Approved: ${request.approved}`);
  console.log(`  Explanation: ${request.explanation}\n`);

  const result = await client.workflow.execute(reviewCallerWorkflow, {
    taskQueue: PAYMENTS_TASK_QUEUE,
    workflowId: `review-${request.transactionId}`,
    args: [request],
  });

  console.log(`  Review result: ${result.approved ? 'APPROVED' : 'DENIED'}`);
  console.log(`  Risk level:    ${result.riskLevel}`);
  console.log(`  Explanation:   ${result.explanation}\n`);
  console.log('  TXN-B review submitted. The payment Workflow will now complete.');
  console.log('==========================================================');

  await connection.close();
}

run().then(
  () => process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  },
);
