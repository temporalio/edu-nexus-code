---
slug: the-payments-side
id: jbyk8xzlaioc
type: challenge
title: 4. The Payments Side
teaser: Swap the Activity stub for a Nexus stub, point it at the Endpoint, and delete
  the coupling.
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

    Somewhere in the Payments Worker, a line registers Compliance code inside the
    Payments process.

    Deleting that line is the decoupling. Everything else is wiring.
tabs:
- id: z4z6ycnuq0gr
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8080
- id: nhtcw92vsc1e
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: y3hvqceqamea
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: kzwxxptmyl1v
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: adr4epmge4jz
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: prerkoiew6ai
  title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src/main/kotlin
  port: 8080
- id: mfuvzxqwzl5a
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: intermediate
timelimit: 1800
enhanced_loading: null
---

# Swap the Stub

Click the [button label="Exercise" background="#444CE7"](tab-0) tab, open
`payments/temporal/PaymentProcessingWorkflowImpl.kt`, and follow the TODO comments.

The call site barely changes. Same method name, same input, same output. What changes
is everything underneath it.

# Point the Contract at the Endpoint

Open `payments/temporal/PaymentsWorkerApp.kt` and follow the TODO comments.

The last one deletes a single registration. That deletion is the decoupling.
Everything before it was wiring.

# Run It Decoupled

Two Workers now, one per team. Start Compliance first.

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
./gradlew complianceWorker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
./gradlew paymentsWorker
```

Read its banner. `ComplianceActivity` is gone and a Nexus line replaced it:

```bash,nocopy
  Registered: PaymentProcessingWorkflow, ReviewCallerWorkflow, PaymentActivity
  Nexus: ComplianceNexusService -> compliance-endpoint
```

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
./gradlew starter
```

# Read the Result Carefully

```bash,nocopy
  TXN-A   Result: COMPLETED             Risk: LOW
  TXN-B   Result: PENDING REVIEW        MEDIUM risk, waiting on a human
  TXN-C   Result: DECLINED_COMPLIANCE   Risk: HIGH
```

TXN-B changed. In the monolith it completed instantly, because compliance was a plain
Activity that returned a MEDIUM verdict and moved on. Now the check runs inside a real
Workflow that parks for human review. Same transaction. Same business rules. The
durable boundary made the wait visible.

Leave TXN-B parked. Challenge 5 releases it.

# If It Hangs

A payment Workflow stuck in `Running` with nothing happening means the Endpoint name
does not match. Check the Payments Worker tab for:

```bash,nocopy
INVALID_ARGUMENT: BadScheduleNexusOperationAttributes: endpoint "..." not found
```

A wrong Endpoint name does not fail the Workflow. The server rejects the command and
the Workflow task retries forever. Silence, not an error.

# See the Boundary

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab.

In `payments-namespace`, open the newest `PaymentProcessingWorkflow` and find
`NexusOperationScheduled` and `NexusOperationCompleted` in the Event History. Click
refresh if the list looks stale.

Now switch the Namespace selector to `compliance-namespace`. Three
`ComplianceWorkflow` executions are running there. That Namespace was empty in
challenge 1.

Click **Check**.

# What You Know Now

- Swapping an Activity stub for a Nexus stub is a one line change at the call site.
- The Workflow names the contract. The Worker names the Endpoint.
- The work moved to another Namespace, and the caller never learned its address.
