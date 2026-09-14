---
slug: durability-and-human-review
id: hcijwjx63wn2
type: challenge
title: 5. Break it, then finish it
teaser: Take the Compliance Worker down mid-payment. Watch the payment wait instead
  of fail.
notes:
- type: text
  contents: |-
    # The Compliance team is deploying. What happens to payments in flight?

    You just moved compliance into another team's process, and that team ships
    on Fridays.

    An HTTP call would return a connection error and leave you writing retry
    logic. This is not an HTTP call.
- type: text
  contents: |-
    # Ziggy the tardigrade survives being frozen, boiled, and shot into space

    It parks its metabolism and picks up where it left off. Your payment
    Workflow is about to do the same.
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

# Start both teams

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab:

```bash,run
npm run payments-worker
```

# Take Compliance down

Go back to the [button label="Compliance Worker" background="#444CE7"](tab-4) tab and
press **Ctrl+C**. Compliance is offline and Payments has not noticed.

# Send payments anyway

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
```

Nothing resolves. The starter reports every transaction as still running and exits after
25 seconds.

# Look at what did not happen

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab. In
`payments-namespace`, open the newest payment Workflow.

Status is **Running**, not Failed. The Event History shows **Nexus Operation Scheduled**
with no completion, waiting for a handler that is not there.

No connection error, and no retry loop you had to write. The caller's
`scheduleToCloseTimeout` of ten minutes is the entire outage budget, and you set it in one
line in challenge 4.

# Bring Compliance back

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

Watch the Temporal UI. The pending Operations get picked up, TXN-A completes, and TXN-C
is declined for HIGH risk. This can take up to a minute, because the Operation retries
with backoff while the handler is down.

No payment failed. Each one waited.

# Release the parked payment

TXN-B is still parked. MEDIUM risk needs a human, and you are the human.

Wait until TXN-A shows **Completed** first. The review is a sync Operation with a ten
second budget and it fails if `compliance-TXN-B` has not started yet. Run it again if that
happens.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run review-starter
```

```bash,nocopy
  Review result: APPROVED
  Risk level:    MEDIUM
  Explanation:   Approved after manual review
```

That went over Nexus through `submitReview`, the handler you wrote in challenge 3 and the
caller you wrote in challenge 4. The async Operation starts new work. The sync one sends a
message to work already running.

Open `payment-TXN-B` in the UI. It is **Completed**, with a confirmation number and the
explanation you typed in.

Click **Check**.

# What you built

| Piece | What it did |
|---|---|
| `nexus.service()` / `nexus.operation<I, O>()` | The contract both teams compile against |
| `nexus.serviceHandler()` | The handler only Compliance owns |
| `WorkflowRunOperationHandler` | Backed a long check with a Workflow, retry safe |
| a plain `async` handler | Steered a running Workflow across the boundary |
| `wf.createNexusServiceClient()` | Replaced the Activity proxy at the call site |
| `nexusServices: [...]` on the Worker | Made Compliance answerable at all |
| Nexus Endpoint | The routing rule, the only piece outside the code |

One Worker running both teams' code became two services that deploy independently, and
Payments kept working through an outage. The business logic never changed.

Nexus is generally available in the TypeScript SDK as of v1.23.0, for calling Operations
from Workflows and for Workflow-backed Operation handlers.

---

Tell us what worked in the **Feedback** tab. It takes a few seconds.
