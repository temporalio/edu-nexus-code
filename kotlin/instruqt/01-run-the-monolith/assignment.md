---
slug: run-the-monolith
id: 1py82qohbrww
type: challenge
title: 1. Run the Monolith
teaser: Three payments, two teams, one Worker. Find the coupling before you fix it.
notes:
- type: text
  contents: |-
    # What breaks when two teams share one Worker?

    A payment cannot execute until Compliance clears it. Both teams' code runs in
    the same process, on the same Task Queue, in the same Namespace.

    It works. That is the problem. Nobody notices the coupling until the night
    a compliance bug takes payments down with it.
- type: text
  contents: |-
    # Ziggy is warming up your sandbox

    A Temporal dev server is starting, and two Namespaces are being created:
    one for Payments, one for Compliance.

    Right now only one of them has anything in it.
tabs:
- id: pau5q3wwpsps
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8080
- id: 0zbvnbeb8mif
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: kzygtdxjoavt
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: bewxfc6rylob
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xvgyorzzelr3
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: zgznlhi8iup5
  title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src/main/kotlin
  port: 8080
- id: yyvrkebl6muk
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: basic
timelimit: 900
enhanced_loading: null
---

# The Setup

You work at a bank. Every payment takes three steps.

1. Validate the payment. Payments team.
2. Check compliance. Compliance team.
3. Execute the payment. Payments team.

Step 2 is not optional. Nothing moves until Compliance says yes.

The blue buttons below are clickable. Click any one to jump to that tab.

# Start the Worker

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab and start it:

```bash,run
./gradlew paymentsWorker
```

Read the banner it prints:

```bash,nocopy
  Registered: PaymentProcessingWorkflow, PaymentActivity
              ComplianceActivity (monolith, will decouple)
```

One Worker. Both teams' code. That last line is the coupling, written down.

# Run Three Payments

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
./gradlew starter
```

Three transactions go through. Watch what comes back:

```bash,nocopy
  TXN-A   Result: COMPLETED             Risk: LOW
  TXN-B   Result: COMPLETED             Risk: MEDIUM
  TXN-C   Result: DECLINED_COMPLIANCE   Risk: HIGH
```

# See It in the Temporal UI

The terminal gave you three lines. The server has the whole story.

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab. Check that the
Namespace selector at the top reads `payments-namespace`.

Three Workflows, every one of them **Completed**. Click refresh if you do not see them yet.

| Workflow ID | Amount | What happened |
|---|---|---|
| `payment-TXN-A` | $250 | paid |
| `payment-TXN-B` | $12,000 | paid |
| `payment-TXN-C` | $75,000 | declined by Compliance |

Open `payment-TXN-C`, find the **Input and Results** panel, and open **Results**:

```json,nocopy
{
  "success": false,
  "transactionId": "TXN-C",
  "status": "DECLINED_COMPLIANCE",
  "riskLevel": "HIGH",
  "explanation": "Transaction amount exceeds $50,000 threshold. Requires enhanced due diligence review.",
  "confirmationNumber": null,
  "error": null
}
```

Read that closely. `success` is `false`, `error` is `null`, and the Workflow status is
**Completed** rather than Failed.

Compliance looked at a $75,000 payment and said no. That is the system working. A declined
payment is an answer, so the Workflow returned it and finished normally. Keep the two ideas
apart: a declined payment is a business outcome, a failed Workflow is a defect. You will
watch this same result travel across a team boundary in challenge 4, and it should still
look exactly like this.

# Review the Monolith Architecture

Before you hunt for the coupling in code, look at the shape of what you just ran.

Click the [button label="Monolith Architecture" background="#444CE7"](tab-6) tab.

Two shaded zones, one per team. Payments is blue, Compliance is amber. Every box carries
its file name and the method that matters.

Click **Walk the flow**, then move with the **left** and **right** arrows on screen or on
your keyboard. Eight steps.

Two of them are the point of this challenge:

- **Step 5 of 8** is the Workflow calling the Compliance team's Activity. Watch the arrow
  cross from the blue zone into the amber one while staying inside a single process.
- **Step 8 of 8** is the consequence. Compliance owns a Namespace with nothing in it.

**Fit** brings everything back on screen. **Reset view** undoes any zooming.

# Find the Coupling

Click the [button label="Exercise" background="#444CE7"](tab-0) tab and open
`payments/temporal/PaymentsWorkerApp.kt`.

Look at what this one Worker registers. `PaymentActivityImpl` belongs to Payments.
`ComplianceActivityImpl` belongs to Compliance. Same process. Same deployment. Same
blast radius.

Now open `payments/temporal/PaymentProcessingWorkflowImpl.kt` and find step 2:

```kotlin,nocopy
val compliance = complianceActivity.checkCompliance(compReq)
```

An in-process Activity call across a team boundary that should not be in-process.

# See the Empty Namespace

The diagram claims Compliance owns a Namespace with nothing in it. Confirm it.

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab and switch the
Namespace selector to `compliance-namespace`.

Empty. Compliance has a Namespace of its own, and nothing runs there, because their code
is executing inside the Payments Worker.

Switch back to `payments-namespace`, open `payment-TXN-C` again, and read the Event
History. Find `ActivityTaskScheduled` for the compliance check. That single event is what
you spend the rest of this lab replacing.

Click **Check** when your three transactions have finished.

# What You Know Now

- One Worker runs both teams. One bug takes down both.
- The compliance check is an ordinary Activity call.
- A declined payment completes. It does not fail.
- Compliance has a Namespace, and it is empty.
