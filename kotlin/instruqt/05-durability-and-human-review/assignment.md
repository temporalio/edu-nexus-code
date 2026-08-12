---
slug: durability-and-human-review
id: hhujendmyyqk
type: challenge
title: 5. Break It, Then Finish It
teaser: Take the Compliance Worker down mid-payment. Watch the payment wait instead
  of fail.
notes:
- type: text
  contents: |-
    # The Compliance team is deploying. What happens to payments in flight?

    You just moved compliance into another team's process. That team ships on
    Fridays.

    An HTTP call would return a connection error and you would be writing
    retry logic. This is not an HTTP call.
- type: text
  contents: |-
    # Ziggy the tardigrade survives being frozen, boiled, and shot into space

    It parks its metabolism and picks up where it left off.

    Your payment Workflow is about to do the same thing.
tabs:
- id: fvigy3pejd74
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: 5sn9cnguszxu
  title: Exercise
  type: code
  hostname: workshop
  path: /root/workshop/exercise/src/main/kotlin
- id: k9vuphhzqdqo
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: 0qmtsjfwccen
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: mt0qqw7h1u87
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: gp2jkv9u7ssn
  title: Solution
  type: code
  hostname: workshop
  path: /root/workshop/solution/src/main/kotlin
difficulty: basic
timelimit: 900
enhanced_loading: null
---

# Start Both Teams

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
./gradlew complianceWorker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
./gradlew paymentsWorker
```

# Take Compliance Down

Go back to the [button label="Compliance Worker" background="#444CE7"](tab-4) tab and
press **Ctrl+C**.

Compliance is now offline. Payments has no idea.

# Send Payments Anyway

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
./gradlew starter
```

Nothing resolves. The starter reports every transaction as pending and exits after 25
seconds.

# Look at What Did Not Happen

Click the [button label="Temporal UI" background="#444CE7"](tab-0) tab. In
`payments-namespace`, click the newest payment Workflow.

Status is **Running**, not Failed. The Event History shows `NexusOperationScheduled`
with no completion. The Operation is waiting for a handler that does not exist yet.

No connection error. No retry loop you had to write. The caller's
`scheduleToCloseTimeout` of 10 minutes is the entire outage budget, and you set it in
one line back in challenge 4.

# Bring Compliance Back

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
./gradlew complianceWorker
```

Watch the Temporal UI. The pending Operations get picked up and TXN-A completes,
TXN-C is declined for HIGH risk, exactly as if nothing had happened.

This can take up to a minute. The Operation retries with backoff while the handler
is down, so it does not resume the instant the Worker returns.

**The payments never failed. They waited.**

# Release the Parked Payment

TXN-B is still parked. MEDIUM risk needs a human, and you are the human.

Wait until TXN-A shows **Completed** before running this. The review is a sync
Operation with a 10 second budget, and it fails if `compliance-TXN-B` has not started
yet. Run it again if it does.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
./gradlew reviewStarter
```

```bash,nocopy
  Review result: APPROVED
  Risk level:    MEDIUM
```

That call went out over Nexus too, through `submitReview`, the sync handler you wrote
in challenge 3. Sync because it talks to a Workflow already running and returns
immediately. The async handler starts new work. The sync one sends a message to work
that is already running.

Click **Check**.

# What You Built

You started with one Worker running both teams' code and ended with two services that
deploy independently.

| Piece | What it did |
|---|---|
| `@Service` and `@Operation` | The contract both teams compile against |
| `@ServiceImpl` and `@OperationImpl` | The handler only Compliance owns |
| `WorkflowRunOperation` | Backed a long check with a Workflow, retry safe |
| `OperationHandler.sync` | Steered a running Workflow through the boundary |
| `Workflow.newNexusServiceStub` | Replaced the Activity stub, one line |
| `NexusServiceOptions` | Put the Endpoint name on the Worker, not the Workflow |
| Nexus Endpoint | The routing rule, the only piece outside the code |

The business logic never changed. The call site barely changed. Compliance moved to
its own Namespace, its own Task Queue, and its own deployment schedule, and Payments
kept working through an outage while it happened.
