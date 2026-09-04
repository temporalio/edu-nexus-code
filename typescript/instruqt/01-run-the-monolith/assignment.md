---
slug: run-the-monolith
id: nwdcc4mlob8f
type: challenge
title: 1. Run the Monolith
teaser: Three payments, two teams, one program. Find the coupling before you fix it.
notes:
- type: text
  contents: |-
    # Two teams, one program

    A payment cannot go through until the Compliance team clears it. Today that
    check is an ordinary function call: both teams' code is bundled into one
    program and runs as one process.

    It works, which is why nobody has touched it. What it costs:

    - A bug in Compliance's code takes Payments down with it.
    - Compliance cannot ship a fix unless Payments ships at the same time.
    - Nothing in the code marks where one team ends and the other begins.

    Temporal gives each team a **Namespace** - a walled-off space of their own to
    run in. Compliance has one. It is empty, because all of their code is running
    inside Payments'.

    **Author:** [Nikolay Advolodkin](https://www.linkedin.com/in/nikolayadvolodkin/), Staff Developer Advocate
- type: text
  contents: |-
    # Ziggy is warming up your sandbox

    A Temporal dev server is starting, and two Namespaces are being created:
    one for Payments, one for Compliance.

    Right now only one of them has anything in it.
tabs:
- id: jy7gspk7rzja
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop
  port: 8080
- id: osmynelncext
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: bhx6tjsdyrva
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: utwhvn6xykly
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xcqorbmjolyq
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: kaxawqn9yd2g
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
npm run payments-worker
```

The Worker bundles your Workflow code with webpack on startup, so the first run takes a
second longer than you might expect. Read the banner it prints:

```bash,nocopy
  Registered: paymentProcessingWorkflow, payment activities
              compliance activities (monolith, will decouple)
```

One Worker. Both teams' code. That last line is the coupling, written down.

# Run Three Payments

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
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
  "explanation": "Transaction amount exceeds $50,000 threshold. Requires enhanced due diligence review.",
  "riskLevel": "HIGH",
  "status": "DECLINED_COMPLIANCE",
  "success": false,
  "transactionId": "TXN-C"
}
```

Read that closely. `success` is `false`, there is no `error` field at all, and the Workflow
status is **Completed** rather than Failed.

Compliance looked at a $75,000 payment and said no. That is the system working. A declined
payment is an answer, so the Workflow returned it and finished normally. Keep the two ideas
apart: a declined payment is a business outcome, a failed Workflow is a defect. You will
watch this same result travel across a team boundary in challenge 4, and it should still
look exactly like this.

# Review the Monolith Architecture

Before you hunt for the coupling in code, look at the shape of what you just ran.

Click the [button label="Monolith Architecture" background="#444CE7"](tab-5) tab.

Two shaded zones, one per team. Payments is blue, Compliance is amber. Every box carries
its file name and the function that matters.

Click **Walk the flow**, then move with the **left** and **right** arrows on screen or on
your keyboard. Eight steps.

Two of them are the point of this challenge:

- **Step 5 of 8** is the Workflow calling the Compliance team's Activity. Watch the arrow
  cross from the blue zone into the amber one while staying inside a single process.
- **Step 8 of 8** is the consequence. Compliance owns a Namespace with nothing in it.

Then read the dashed outline that wraps both zones. It is labeled
`payments/worker.ts · one Node process · one Task Queue payments-processing`. That is the
coupling stated as plainly as it can be: both teams' code, one process, one queue. Every
box inside that outline is polled by the same Worker.

**Fit** brings everything back on screen. **Reset view** undoes any zooming.

# Find the Coupling

Click the [button label="Exercise" background="#444CE7"](tab-0) tab and open
`exercise/src/payments/worker.ts`.

Look at what this one Worker registers. One object, two different teams:

```ts,nocopy
activities: { ...paymentActivities, ...complianceActivities },
```

`paymentActivities` belongs to Payments. `complianceActivities` belongs to Compliance.
They sit on one line in one file, which means the same process, the same deployment, and
the same blast radius. Compliance cannot ship a fix without Payments shipping too.

That second spread is the one you delete in challenge 4. That deletion is the decoupling.

Now open `exercise/src/payments/workflows.ts` and find step 2:

```ts,nocopy
const result: ComplianceResult = await checkCompliance(compReq);
```

An in-process Activity call across a team boundary that should not be in-process.

# See the Empty Namespace

The diagram claims Compliance owns a Namespace with nothing in it. Confirm it.

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab and switch the
Namespace selector to `compliance-namespace`.

No Workflows. Compliance has a Namespace of its own and nothing of theirs runs in it,
because their code is executing inside the Payments Worker.

If you look under **Workers** you will see one Go Worker on a `temporal-sys-` Task Queue.
Ignore it — Temporal Server is written in Go and runs an internal Worker in every
Namespace for its own background work. No Worker of the Compliance team's exists yet.
You start one in challenge 3.

Switch back to `payments-namespace`, open `payment-TXN-C` again, and read the Event
History. Find **Activity Task Scheduled** for the compliance check. That single event is
what you spend the rest of this lab replacing.

Click **Check** when your three transactions have finished.

# What You Know Now

- One Worker runs both teams. One bug takes down both.
- The compliance check is an ordinary Activity call.
- A declined payment completes. It does not fail.
- Compliance has a Namespace, and it is empty.

---

**Please share your feedback so we can make better content for you.** The **Feedback**
tab takes a few seconds, and it is the only way we find out which parts of this landed.
