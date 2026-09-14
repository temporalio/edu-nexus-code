---
slug: run-the-monolith
id: nwdcc4mlob8f
type: challenge
title: 1. Run the monolith
teaser: Run three payments through one Worker, then find the coupling in the code.
notes:
- type: text
  contents: |-
    # Two teams, one program

    A payment cannot go through until the Compliance team clears it. Today that
    check is an ordinary function call, because both teams' code is bundled into
    one program and runs as one process.

    What that costs:

    - A bug in Compliance's code stops Payments too.
    - Compliance cannot ship a fix unless Payments ships at the same time.
    - Nothing in the code marks where one team ends and the other begins.

    Temporal gives each team a Namespace of their own to run in. Compliance has
    one, and it is empty, because all of their code runs inside Payments'.

    **Author:** [Nikolay Advolodkin](https://www.linkedin.com/in/nikolayadvolodkin/), Staff Developer Advocate
- type: text
  contents: |-
    # Ziggy is warming up your sandbox

    A Temporal dev server is starting, and two Namespaces are being created,
    one for Payments and one for Compliance. Only one of them has anything in it.
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

# The setup

You work at a bank. Every payment takes three steps.

1. Validate the payment. Payments team.
2. Check compliance. Compliance team.
3. Execute the payment. Payments team.

Nothing moves until Compliance says yes.

The blue buttons below are clickable. Click one to jump to that tab.

# Start the Worker

Click the [button label="Payments Worker" background="#444CE7"](tab-3) tab and start it:

```bash,run
npm run payments-worker
```

Webpack bundles your Workflow code on startup, so the first run is the slow one. Read the
banner it prints:

```bash,nocopy
  Registered: paymentProcessingWorkflow, payment activities
              compliance activities (monolith, will decouple)
```

One Worker, both teams' code. That last line is the coupling.

# Run three payments

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npm run starter
```

```bash,nocopy
  TXN-A   Result: COMPLETED             Risk: LOW
  TXN-B   Result: COMPLETED             Risk: MEDIUM
  TXN-C   Result: DECLINED_COMPLIANCE   Risk: HIGH
```

# Check the Temporal UI

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab. The Namespace
selector at the top should read `payments-namespace`.

Three Workflows, every one of them **Completed**. Refresh if you do not see them yet.

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

`success` is `false`, there is no `error` field, and the Workflow status is **Completed**
rather than Failed. A declined payment is a business outcome, so the Workflow returned it
and finished normally. A failed Workflow is a defect. This result should look identical
after you move the check across a team boundary in challenge 4.

# Walk the architecture diagram

Click the [button label="Monolith Architecture" background="#444CE7"](tab-5) tab, click
**Walk the flow**, and move with the left and right arrows. Two of the eight steps carry
this challenge:

- **Step 5** is the Workflow calling Compliance's Activity. The arrow crosses from the
  blue zone into the amber one without leaving the process.
- **Step 8** is what that costs. Compliance owns a Namespace with nothing in it.

# Find the coupling

Click the [button label="Exercise" background="#444CE7"](tab-0) tab and open
`exercise/src/payments/worker.ts`. One object, two teams:

```ts,nocopy
activities: { ...paymentActivities, ...complianceActivities },
```

Same process, same deployment. Compliance cannot ship a fix unless Payments ships too.
You delete that second spread in challenge 4, and that deletion is the decoupling.

Now open `exercise/src/payments/workflows.ts` and find step 2:

```ts,nocopy
const result: ComplianceResult = await checkCompliance(compReq);
```

An in-process Activity call across a team boundary.

# See the empty Namespace

Click the [button label="Temporal UI" background="#444CE7"](tab-1) tab and switch the
Namespace selector to `compliance-namespace`.

No Workflows. Compliance's code is executing inside the Payments Worker. Under
**Workers** you will see one Go Worker on a `temporal-sys-` Task Queue, which is Temporal
Server's own and runs in every Namespace. You start Compliance's Worker in challenge 3.

Switch back to `payments-namespace`, open `payment-TXN-C`, and find **Activity Task
Scheduled** for the compliance check in the Event History. That event is the one you
spend the rest of this lab replacing.

Click **Check** when your three transactions have finished.

# What you know now

- One Worker runs both teams, so one bug stops both.
- The compliance check is an ordinary Activity call.
- A declined payment completes. It does not fail.
- Compliance has a Namespace and it is empty.

---

Tell us what worked in the **Feedback** tab. It takes a few seconds.
