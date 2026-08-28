// The Compliance team's Worker. This process is the only place their code runs.
//
// The connection and the Worker itself are set up for you. What is missing is the part
// that decides what this Worker can actually do: see TODO 4.
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
      // MUST match the --target-task-queue you give the Nexus Endpoint, or the Endpoint
      // routes work to a queue nobody is polling.
      taskQueue: COMPLIANCE_TASK_QUEUE,
      workflowsPath: require.resolve('./workflows'),
      activities,

      // ── TODO 4 ────────────────────────────────────────────────────────────────
      // A Worker only handles work it has been told about. This one already knows about
      // its Workflows (workflowsPath) and its Activities (activities). What it does not
      // yet know about is the Nexus handler — the thing that makes this team callable by
      // Payments at all.
      //
      // Add one more option here, naming `complianceServiceHandler`, which is already
      // imported above.
      //
      // Leave it out and the Worker still starts. Payments' calls just go unanswered
      // forever. That is the failure you are guarding against.
      //
      // Stuck? "Ask AI" on https://docs.temporal.io:
      //   "what do I register on a TypeScript Worker that handles Nexus Operations?"
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
