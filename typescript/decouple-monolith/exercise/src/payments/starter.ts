// [GIVEN] Starts three payment Workflows.
//
// All three start before any result is collected. That matters once Nexus is in play:
// TXN-B is MEDIUM risk, so its handler Workflow pauses for human review and would
// otherwise block TXN-C behind it. Starting concurrently lets TXN-A and TXN-C finish
// while TXN-B sits visibly pending, which is the point of the human-in-the-loop path.
//
// The starter does not know or care whether compliance runs locally or via Nexus.
import { Client, Connection } from '@temporalio/client';
import { paymentProcessingWorkflow } from './workflows';
import { PAYMENTS_NAMESPACE, PAYMENTS_TASK_QUEUE, PaymentRequest, PaymentResult } from '../shared/types';

// How long to wait before calling a Workflow "pending review". TXN-A and TXN-C finish in
// roughly 12 seconds (the handler Workflow sleeps for 10 of those).
const COLLECT_DEADLINE_MS = 25_000;

const TRANSACTIONS: PaymentRequest[] = [
  {
    transactionId: 'TXN-A', amount: 250.0, currency: 'USD',
    senderCountry: 'US', receiverCountry: 'US',
    description: 'Routine supplier payment', senderAccount: 'ACC-001', receiverAccount: 'ACC-002',
  },
  {
    transactionId: 'TXN-B', amount: 12_000.0, currency: 'USD',
    senderCountry: 'US', receiverCountry: 'UK',
    description: 'International consulting fee', senderAccount: 'ACC-003', receiverAccount: 'ACC-004',
  },
  {
    transactionId: 'TXN-C', amount: 75_000.0, currency: 'USD',
    senderCountry: 'US', receiverCountry: 'US',
    description: 'Large capital transfer', senderAccount: 'ACC-005', receiverAccount: 'ACC-006',
  },
];

type Outcome =
  | { state: 'done'; result: PaymentResult }
  | { state: 'failed'; error: string }
  | { state: 'pending' };

async function run() {
  console.log('==========================================================');
  console.log('  PAYMENT STARTER - Decouple Monolith');
  console.log('  Running 3 transactions through Temporal');
  console.log('==========================================================\n');

  const connection = await Connection.connect({ address: 'localhost:7233' });
  const client = new Client({ connection, namespace: PAYMENTS_NAMESPACE });

  // Start all three without awaiting results, so a pause in one cannot hold up the others.
  const started = await Promise.all(
    TRANSACTIONS.map(async (txn) => {
      const handle = await client.workflow.start(paymentProcessingWorkflow, {
        taskQueue: PAYMENTS_TASK_QUEUE,
        workflowId: `payment-${txn.transactionId}`,
        args: [txn],
      });
      console.log(`  Started: payment-${txn.transactionId}`);
      console.log(
        `    Amount: $${txn.amount.toFixed(2)} | Route: ${txn.senderCountry} -> ${txn.receiverCountry}`,
      );
      return { txn, handle };
    }),
  );

  console.log('\n  All 3 started. Collecting results...\n');

  // Wait on all three against a single shared deadline. Waiting one at a time would let
  // TXN-B's review pause burn the whole budget, so TXN-C would be reported as pending
  // even though it finished seconds earlier.
  const outcomes = new Map<string, Outcome>(started.map(({ txn }) => [txn.transactionId, { state: 'pending' }]));
  await Promise.race([
    Promise.allSettled(
      started.map(async ({ txn, handle }) => {
        try {
          outcomes.set(txn.transactionId, { state: 'done', result: await handle.result() });
        } catch (err) {
          outcomes.set(txn.transactionId, { state: 'failed', error: (err as Error).message });
        }
      }),
    ),
    new Promise((resolve) => setTimeout(resolve, COLLECT_DEADLINE_MS)),
  ]);

  let pendingReview = 0;
  for (const { txn } of started) {
    const outcome = outcomes.get(txn.transactionId)!;
    console.log('----------------------------------------------------');
    console.log(`  ${txn.transactionId}`);

    if (outcome.state === 'pending') {
      pendingReview++;
      console.log('  Result: STILL RUNNING');
      console.log('  Reason: either it is parked for human review (MEDIUM risk), or the');
      console.log('          Compliance handler is not answering right now.');
      console.log('  Action: check the Temporal UI. If it is parked for review, run');
      console.log("          'npm run review-starter'.");
    } else if (outcome.state === 'failed') {
      console.log('  Result: FAILED');
      console.log(`  Error:  ${outcome.error}`);
    } else {
      const r = outcome.result;
      console.log(`  Result: ${r.status}`);
      console.log(`  Risk:   ${r.riskLevel ?? 'N/A'}`);
      console.log(`  Reason: ${r.explanation ?? 'N/A'}`);
      if (r.confirmationNumber) console.log(`  Conf#:  ${r.confirmationNumber}`);
      if (r.error) console.log(`  Error:  ${r.error}`);
    }
    console.log();
  }

  console.log('==========================================================');
  if (pendingReview > 0) {
    console.log(`  ${pendingReview} transaction(s) have not resolved yet.`);
    console.log("  Parked for review? Run 'npm run review-starter'.");
    console.log('  Compliance Worker down? Start it and they resume on their own.');
  } else {
    console.log('  All 3 transactions resolved!');
  }
  console.log('  Check Temporal UI: http://localhost:8233');
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
