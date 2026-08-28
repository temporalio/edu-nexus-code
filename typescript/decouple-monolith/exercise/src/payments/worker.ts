// MONOLITH VERSION. One Worker, one deployment, one blast radius.
//
// Right now this single process runs everything:
//   - paymentProcessingWorkflow   (Payments team)
//   - payment activities          (Payments team)
//   - compliance activities       (Compliance team's code, running in Payments' process)
//
// That last line is the problem. A bug in compliance code takes payments down with it.
//
// Do TODO 6 last, after TODO 5 has moved the Workflow onto a Nexus client.
import { NativeConnection, Worker } from '@temporalio/worker';
import * as paymentActivities from './activities';
import * as complianceActivities from '../compliance/activities';
import { PAYMENTS_NAMESPACE, PAYMENTS_TASK_QUEUE } from '../shared/types';

async function run() {
  const connection = await NativeConnection.connect({ address: 'localhost:7233' });
  try {
    const worker = await Worker.create({
      connection,
      namespace: PAYMENTS_NAMESPACE,
      taskQueue: PAYMENTS_TASK_QUEUE,
      workflowsPath: require.resolve('./workflows'),

      // ── TODO 6 ────────────────────────────────────────────────────────────────
      // Once the Workflow calls Compliance over Nexus, this Worker has no reason to run
      // the Compliance team's code. Remove `...complianceActivities` from the object
      // below, and delete the import at the top of this file.
      //
      // That deletion is the decoupling. Everything before it was wiring; this is the
      // line where the two teams stop sharing a process.
      activities: { ...paymentActivities, ...complianceActivities },
    });

    console.log('=========================================================');
    console.log(`  Payments Worker started on: ${PAYMENTS_TASK_QUEUE}`);
    console.log(`  Namespace: ${PAYMENTS_NAMESPACE}`);
    console.log('  Registered: paymentProcessingWorkflow, payment activities');
    console.log('              compliance activities (monolith, will decouple)');
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
