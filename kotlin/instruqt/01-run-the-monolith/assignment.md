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
- id: 0zbvnbeb8mif
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: pau5q3wwpsps
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8443
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
  port: 8444
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

TXN-C is $75,000. Compliance blocked it, so the payment never executed. The system
works exactly as intended.

# Find the Coupling

Click the [button label="Exercise" background="#444CE7"](tab-1) tab and open
`payments/temporal/PaymentsWorkerApp.kt`.

Look at what this one Worker registers. `PaymentActivityImpl` belongs to Payments.
`ComplianceActivityImpl` belongs to Compliance. Same process. Same deployment. Same
blast radius.

Now open `payments/temporal/PaymentProcessingWorkflowImpl.kt` and find step 2:

```kotlin,nocopy
val compliance = complianceActivity.checkCompliance(compReq)
```

An in-process Activity call across a team boundary that should not be in-process.

# Look at the Namespaces

Click the [button label="Temporal UI" background="#444CE7"](tab-0) tab. Use the
Namespace selector at the top and switch to `compliance-namespace`.

Empty. Compliance has its own Namespace and nothing runs in it, because Compliance
code is running inside the Payments Worker.

Switch back to `payments-namespace` and click your newest Workflow at the top of the
list. Click refresh if you do not see it yet. Open the Event History and look for
`ActivityTaskScheduled` for the compliance check. That Activity is what you are going
to replace.

Click **Check** when your three transactions have finished.

# What You Know Now

- One Worker runs both teams. One bug takes down both.
- The compliance check is an ordinary Activity call.
- Compliance has a Namespace, and it is empty.
