---
slug: durability-and-human-review
id: hcijwjx63wn2
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
- id: uxvwrjemsoea
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop
  port: 8080
- id: kirogybfummw
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: gsmserb7horg
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: fxetrm2uuagt
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xcwthyvjsofb
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: dwrcskq7wzji
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: basic
timelimit: 900
enhanced_loading: null
---

# Start Both Teams

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
npm run payments-worker
```

# Take Compliance Down

Go back to the [button label="Compliance Worker" background="#444CE7"](tab-4) tab and
press **Ctrl+C**.

Compliance is now offline. Payments has no idea.

# Send Payments Anyway

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
```

Nothing resolves. The starter reports every transaction as still running and exits after
25 seconds.

# Look at What Did Not Happen

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab. In
`payments-namespace`, click the newest payment Workflow.

Status is **Running**, not Failed. The Event History shows **Nexus Operation Scheduled**
with no completion. The Operation is waiting for a handler that does not exist yet.

No connection error. No retry loop you had to write. The caller's
`scheduleToCloseTimeout` of ten minutes is the entire outage budget, and you set it in
one line back in challenge 4.

# Bring Compliance Back

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Watch the Temporal UI. The pending Operations get picked up and TXN-A completes,
TXN-C is declined for HIGH risk, exactly as if nothing had happened.

This can take up to a minute. The Operation retries with backoff while the handler
is down, so it does not resume the instant the Worker returns.

**The payments never failed. They waited.**

# Release the Parked Payment

TXN-B is still parked. MEDIUM risk needs a human, and you are the human.

Wait until TXN-A shows **Completed** before running this. The review is a sync
Operation with a ten second budget, and it fails if `compliance-TXN-B` has not started
yet. Run it again if it does.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run review-starter
```

```bash,nocopy
  Review result: APPROVED
  Risk level:    MEDIUM
  Explanation:   Approved after manual review
```

That call went out over Nexus too, through `submitReview`, the sync handler you wrote
in challenge 3 and the caller you wrote in challenge 4. Sync because it talks to a
Workflow already running and returns immediately. The async Operation starts new work;
the sync one sends a message to work that is already running.

Open `payment-TXN-B` in the UI one more time. It is **Completed**, with a confirmation
number, and the explanation is the one you just typed in.

Click **Check**.

# What You Built

You started with one Worker running both teams' code and ended with two services that
deploy independently.

| Piece | What it did |
|---|---|
| `nexus.service()` / `nexus.operation<I, O>()` | The contract both teams compile against |
| `nexus.serviceHandler()` | The handler only Compliance owns |
| `WorkflowRunOperationHandler` | Backed a long check with a Workflow, retry safe |
| a plain `async` handler | Steered a running Workflow through the boundary |
| `wf.createNexusServiceClient()` | Replaced the Activity proxy at the call site |
| `nexusServices: [...]` on the Worker | Made Compliance answerable at all |
| Nexus Endpoint | The routing rule, the only piece outside the code |

The business logic never changed. The call site barely changed. Compliance moved to
its own Namespace, its own Task Queue, and its own deployment schedule, and Payments
kept working through an outage while it happened.

TypeScript SDK support for Nexus is at Pre-release and its APIs are marked experimental,
so expect the surface to move before it is stable.
