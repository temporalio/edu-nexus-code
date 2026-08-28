// DECOUPLED VERSION. This Worker runs Payments code and nothing else.
//
// Compare against the monolith it replaced, which also registered the Compliance team's
// Activity — meaning a bug in compliance code at 3 AM took payments down with it.
import { NativeConnection, Worker } from '@temporalio/worker';
import * as activities from './activities';
import { PAYMENTS_NAMESPACE, PAYMENTS_TASK_QUEUE } from '../shared/types';

async function run() {
  const connection = await NativeConnection.connect({ address: 'localhost:7233' });
  try {
    const worker = await Worker.create({
      connection,
      namespace: PAYMENTS_NAMESPACE,
      taskQueue: PAYMENTS_TASK_QUEUE,
      workflowsPath: require.resolve('./workflows'),
      activities,
    });

    console.log('=========================================================');
    console.log(`  Payments Worker started on: ${PAYMENTS_TASK_QUEUE}`);
    console.log(`  Namespace: ${PAYMENTS_NAMESPACE}`);
    console.log('  Registered: paymentProcessingWorkflow, reviewCallerWorkflow');
    console.log('              payment activities only — compliance is remote');
    console.log('=========================================================');

    await worker.run();
  } finally {
    await connection.close();
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
