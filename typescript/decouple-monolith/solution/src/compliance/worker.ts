// The Compliance team's Worker. This process is the only place their code runs.
import { NativeConnection, Worker } from '@temporalio/worker';
import * as activities from './activities';
import { complianceServiceHandler } from './nexus-handler';
import { COMPLIANCE_NAMESPACE, COMPLIANCE_TASK_QUEUE } from '../shared/types';

async function run() {
  const connection = await NativeConnection.connect({ address: 'localhost:7233' });
  try {
    const worker = await Worker.create({
      connection,
      namespace: COMPLIANCE_NAMESPACE,
      // MUST match the --target-task-queue given to the Nexus Endpoint, or the Endpoint
      // routes work to a queue nobody is polling.
      taskQueue: COMPLIANCE_TASK_QUEUE,
      workflowsPath: require.resolve('./workflows'),
      activities,
      // Leave this out and the Worker still starts — Payments' calls just go unanswered
      // forever. That is the failure this guards against.
      nexusServices: [complianceServiceHandler],
    });

    console.log('=========================================================');
    console.log(`  Compliance Worker started on: ${COMPLIANCE_TASK_QUEUE}`);
    console.log(`  Namespace: ${COMPLIANCE_NAMESPACE}`);
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
