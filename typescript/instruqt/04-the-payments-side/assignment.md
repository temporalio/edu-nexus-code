---
slug: the-payments-side
id: xbklygolip1u
type: challenge
title: 4. The Payments Side
teaser: Swap the Activity proxy for a Nexus client, point it at the Endpoint, and
  delete the coupling.
notes:
- type: text
  contents: |-
    # How much code changes when you cross a team boundary?

    The compliance check is about to leave this process entirely. Different
    Namespace, different Task Queue, different deployment, different team.

    Guess how many lines change at the call site.
- type: text
  contents: |-
    # One deletion is the whole point

    Somewhere in the Payments Worker, one spread registers Compliance code inside
    the Payments process.

    Deleting it is the decoupling. Everything else is wiring.
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

# Swap the Proxy for a Nexus Client

Click the [button label="Exercise" background="#444CE7"](tab-0) tab, open
`exercise/src/payments/workflows.ts`, and follow TODO 4.

The call site barely changes. Same operation name, same input, same result type. What
changes is everything underneath it.

```ts,nocopy
// before — runs in this process
const result = await checkCompliance(compReq);

// after — goes out through the Endpoint to another team's Worker
const result = await compliance.executeOperation('checkCompliance', compReq, {
  scheduleToCloseTimeout: '10 minutes',
});
```

The ten minutes is the budget for the whole call, retries included. It is what lets the
Operation survive the Compliance Worker going away rather than failing the moment it does.
You prove that in challenge 5.

# Where the Endpoint Name Lives

Look at what you just wrote. The Workflow names two things: a **contract** and an
**Endpoint**. It does not name a Namespace, a Task Queue, a hostname, or a port.

That is the boundary. An Endpoint is a name the Registry resolves into an address. Move
Compliance to a different Namespace or a different Task Queue tomorrow, update the
Endpoint, and not one line of this Workflow changes.

The name itself is a single constant, `COMPLIANCE_ENDPOINT` in `exercise/src/shared/types.ts`, so there
is exactly one place to edit if it is ever renamed.

# Write the Review Caller

Still in `exercise/src/payments/workflows.ts`, follow TODO 5.

`submitReview` is a **synchronous** Operation, so the Compliance handler must finish inside
the ten second handler deadline. Use a `scheduleToCloseTimeout` of `'10 seconds'` here, not
the ten minutes above. Two Operations on one Service, two very different budgets.

Challenge 5 uses this.

# Delete the Coupling

Open `exercise/src/payments/worker.ts` and follow TODO 6.

Remove `...complianceActivities` from the `activities` object, and delete its import.

That deletion is the decoupling. Everything else is wiring.

# Run It Decoupled

Two Workers now, one per team. Start Compliance first.

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
npm run payments-worker
```

Its banner now reads `payment activities only — compliance is remote`. And this time
you should see the Nexus line on the Payments side:

```bash,nocopy
[INFO] No Nexus services registered, not polling for Nexus tasks
```

That is correct here. Payments is the **caller**, not the handler. It makes Nexus calls;
it answers none. Only the Compliance Worker should be missing that line.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
```

# Read the Result Carefully

```bash,nocopy
  TXN-A   Result: COMPLETED             Risk: LOW
  TXN-B   Result: STILL RUNNING         MEDIUM risk, waiting on a human
  TXN-C   Result: DECLINED_COMPLIANCE   Risk: HIGH
```

TXN-B changed. In the monolith it completed instantly, because compliance was a plain
Activity that returned a MEDIUM verdict and moved on. Now the check runs inside a real
Workflow that parks for human review. Same transaction. Same business rules. The
durable boundary made the wait visible.

TXN-A and TXN-C are also slower than they were in challenge 1: the handler Workflow sleeps
for ten seconds before returning, which is the window you use in challenge 5. If the
starter reports TXN-A as still running, give it a moment and check the UI — it finishes.

Leave TXN-B parked. Challenge 5 releases it.

# If It Hangs

A payment Workflow stuck in `Running` with nothing happening means the Endpoint name
does not match. Check the Payments Worker tab for:

```bash,nocopy
INVALID_ARGUMENT: BadScheduleNexusOperationAttributes: endpoint "..." not found
```

A wrong Endpoint name does not fail the Workflow. The server rejects the command and
the Workflow task retries forever. Silence, not an error.

```bash,run
temporal operator nexus endpoint list
```

# See the Boundary

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab.

In `payments-namespace`, open the newest `paymentProcessingWorkflow` and find
**Nexus Operation Scheduled** and **Nexus Operation Completed** in the Event History. Click
refresh if the list looks stale.

Now switch the Namespace selector to `compliance-namespace`. Three
`complianceWorkflow` executions are there. That Namespace was empty in challenge 1.

Click **Check**.

# What You Know Now

- Swapping an Activity proxy for a Nexus client is a one call change at the call site.
- In TypeScript the Workflow names both the contract and the Endpoint; the Registry
  resolves that name to a Namespace and Task Queue.
- One Service can carry a slow async Operation and a fast sync one, with different budgets.
- The work moved to another Namespace, and the business logic never changed.
