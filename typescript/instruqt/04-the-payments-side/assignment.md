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
`exercise/src/payments/workflows.ts`, and follow TODO 4a, 4b and 4c. Each sits on the
code it changes: the proxy to delete, the call to replace, and the review caller to
write.

The call site barely changes — same operation name, same input, same result type. What
changes is everything underneath it: the compliance check leaves this process entirely.

What is worth your attention is the pair of timeouts the two parts use.
`checkCompliance` is **asynchronous** — its budget covers the entire call including
retries, which is what lets it outlive the Compliance Worker going away, as you prove in
challenge 5. `submitReview` is **synchronous** — the handler must answer inside the Nexus
handler deadline. One Service, two Operations, two very different budgets.

# Where the Endpoint Name Lives

Look at what you just wrote, in both places. The Workflow names two things: a **contract**
and an **Endpoint**. It does not name a Namespace, a Task Queue, a hostname, or a port.

That is the boundary. An Endpoint is a name the Registry resolves into an address. Move
Compliance to a different Namespace or a different Task Queue tomorrow, update the
Endpoint, and not one line of this Workflow changes.

The name itself is a single constant, `COMPLIANCE_ENDPOINT` in
`exercise/src/shared/types.ts`, so there is exactly one place to edit if it is renamed.

# Delete the Coupling

Open `exercise/src/payments/worker.ts` and follow TODO 5.

That deletion is the decoupling. Everything else was wiring: once it is gone the Payments
Worker has no way to run the Compliance team's code, even by accident.

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
