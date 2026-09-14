---
slug: the-payments-side
id: xbklygolip1u
type: challenge
title: 4. The payments side
teaser: Swap the Activity proxy for a Nexus client, point it at the Endpoint, and
  delete the coupling.
notes:
- type: text
  contents: |-
    # How much code changes when you cross a team boundary?

    The compliance check is about to leave this process entirely. Different
    Namespace, different Task Queue, different deployment, different team.

    Count the lines that change at the call site.
- type: text
  contents: |-
    # One deletion is the whole point

    Somewhere in the Payments Worker, one spread registers Compliance code
    inside the Payments process. Deleting it is the decoupling. Everything else
    is wiring.
tabs:
- id: jt0nbvtyxv2o
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop
  port: 8080
- id: xrdqvojuovik
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: ak56yyflojys
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: auuddyhhaflq
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: gmgtqjw8yg6r
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: slczt2wyuij1
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: intermediate
timelimit: 1800
enhanced_loading: null
---

# Swap the proxy for a Nexus client

Click the [button label="Exercise" background="#444CE7"](tab-0) tab, open
`exercise/src/payments/workflows.ts`, and follow TODO 4a, 4b and 4c. Each one sits on the
code it changes: the proxy to delete, the call to replace, and the review caller to write.

Same operation name, same input, same result type. Underneath, the compliance check
leaves this process entirely.

Two Operations, two timeouts. `checkCompliance` is asynchronous, so its budget covers the
whole call including retries, which is what lets it outlive the Compliance Worker going
away in challenge 5. `submitReview` is synchronous, so the handler answers inside the
Nexus handler deadline.

The Workflow names a contract and an Endpoint, never a Namespace, a Task Queue, a
hostname, or a port. Move Compliance tomorrow, update the Endpoint, and this Workflow does
not change. The name is one constant, `COMPLIANCE_ENDPOINT` in
`exercise/src/shared/types.ts`.

# Delete the coupling

Open `exercise/src/payments/worker.ts` and follow TODO 5. Once it is gone the Payments
Worker has no way to run the Compliance team's code, even by accident.

# Run it decoupled

Two Workers now, one per team. Start Compliance first.

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
npm run payments-worker
```

Its banner now reads `payment activities only - compliance is remote`. This time the
Nexus line on the Payments side is correct:

```bash,nocopy
[INFO] No Nexus services registered, not polling for Nexus tasks
```

Payments is the caller. It makes Nexus calls and answers none. Only the Compliance Worker
should be missing that line.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
```

# Read the result carefully

```bash,nocopy
  TXN-A   Result: COMPLETED             Risk: LOW
  TXN-B   Result: STILL RUNNING         MEDIUM risk, waiting on a human
  TXN-C   Result: DECLINED_COMPLIANCE   Risk: HIGH
```

TXN-B changed. In the monolith a plain Activity returned a MEDIUM verdict and moved on.
Now the check runs inside a real Workflow that parks for human review. Same transaction,
same business rules.

TXN-A and TXN-C are slower too, because the handler Workflow sleeps ten seconds before
returning. That is the window you use in challenge 5. If the starter reports TXN-A as
still running, give it a moment and check the UI.

Leave TXN-B parked. Challenge 5 releases it.

# If it hangs

A payment Workflow stuck in `Running` means the Endpoint name does not match. Check the
Payments Worker tab for:

```bash,nocopy
INVALID_ARGUMENT: BadScheduleNexusOperationAttributes: endpoint "..." not found
```

A wrong name does not fail the Workflow. The server rejects the command and the Workflow
task retries forever, so you get silence instead of an error.

```bash,run
temporal operator nexus endpoint list
```

# See the boundary

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab. In
`payments-namespace`, open the newest `paymentProcessingWorkflow` and find **Nexus
Operation Scheduled** and **Nexus Operation Completed** in the Event History.

Switch the Namespace selector to `compliance-namespace`. Three `complianceWorkflow`
executions are there. That Namespace was empty in challenge 1.

Click **Check**.

# What you know now

- Swapping an Activity proxy for a Nexus client is a one call change at the call site.
- The Workflow names the contract and the Endpoint. The Registry resolves that name into
  a Namespace and a Task Queue.
- The work moved to another Namespace and the business logic never changed.

---

Tell us what worked in the **Feedback** tab. It takes a few seconds.
