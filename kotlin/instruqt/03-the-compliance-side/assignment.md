---
slug: the-compliance-side
id: io30tn0s5cxy
type: challenge
title: 3. The Compliance Side
teaser: Implement the handler, register it on a Worker, and create the Endpoint.
notes:
- type: text
  contents: |-
    # A compliance check can wait on a human. How do you put that behind an RPC?

    Some checks clear in milliseconds. A $12,000 international transfer waits for
    an officer to click approve, and that officer is at lunch.

    A synchronous handler has ten seconds. Lunch takes longer.
- type: text
  contents: |-
    # This is the hardest challenge in the lab

    Two handlers, two different shapes, plus the Worker registrations and the
    Endpoint.

    The Solution tab is one click away. Use it if you stall. Learning the shape
    beats staring at a blank method.
tabs:
- id: wxvp1e3jvhdl
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: vyijrbhaj7it
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src/main/kotlin
  port: 8080
- id: bnzko3euv8md
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: 17sts5ty9ul4
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xyroncnetgx9
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: 22ayvjxk0rju
  title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src/main/kotlin
  port: 8081
difficulty: intermediate
timelimit: 1800
enhanced_loading: null
---

# Everything Compliance Owns

Three pieces, all on this team's side of the boundary.

1. The handler that answers Nexus calls.
2. The Worker that runs it.
3. The Endpoint that tells Temporal where to route.

# Write the Handler

Click the [button label="Exercise" background="#444CE7"](tab-1) tab, open
`compliance/temporal/ComplianceNexusServiceImpl.kt`, and follow the TODO comments.

You write two methods in this file. They look similar. They are not.

**`checkCompliance` is the slow one.** A $12,000 transfer has to be approved by a
person, and that person might be at lunch. So this call cannot sit there waiting for
an answer. It starts a Workflow and returns immediately, handing back something like
a claim ticket. Temporal uses that ticket to find the Workflow later and collect the
result whenever it finishes, minutes or hours from now.

**`submitReview` is the fast one.** By the time it runs, the compliance check is
already going and is parked waiting for a decision. All this method does is find that
Workflow and hand it the yes or no. That takes milliseconds, so it can do the work and
return the answer in the same call.

Nexus gives you a different tool for each, and the file tells you which is which.

If you use the fast tool for the slow job, two things break. The call is cut off after
10 seconds, so it fails while the human is still deciding. And because Temporal retries
a failed call, each retry starts **another** compliance check for the same payment.

# Register the Handler

Open `compliance/temporal/ComplianceWorkerApp.kt` and follow the TODO comment.

A Worker only handles work it has been told about. This one owns three things, and
leaving any of them out fails in a different way.

# Create the Endpoint

The Endpoint is the routing rule: a name, a target Namespace, and a target Task Queue.
It is the one piece of Nexus that lives outside your code.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
temporal operator nexus endpoint create --name compliance-endpoint --target-namespace compliance-namespace --target-task-queue compliance-risk
```

`--target-task-queue` must match the Task Queue in `ComplianceWorkerApp.kt` exactly.
If it points at a queue no Worker is polling, the calls are never answered.

# Start the Worker

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
./gradlew complianceWorker
```

Scroll up past the log lines and you should see the banner:

```bash,nocopy
=========================================================
  Compliance Worker started on: compliance-risk
  Namespace: compliance-namespace
=========================================================
```

That banner only means the process is alive. It prints whether or not you registered
anything, so it is not proof. Two things are.

**One: the Nexus Poller line.** Look above the banner for:

```bash,nocopy
start: MultiThreadedPoller{name=Nexus Poller taskQueue="compliance-risk", ...}
```

There are three pollers, one per kind of work: Workflow, Activity, and Nexus. The
Nexus one appears only when a Nexus Service is registered. If it is missing, your
handler is not wired even though the Worker started.

**Two: the Temporal UI.** Click the
[button label="Temporal UI" background="#444CE7"](tab-0) tab, switch the Namespace
selector to `compliance-namespace`, and open **Workers** in the left menu. Your Worker
is listed as **Running**.

That is Compliance appearing in its own Namespace for the first time. In challenge 1
this Namespace was empty.

If the Worker did not start at all:

| What you see | What it means |
|---|---|
| `Missing @ServiceImpl annotation` | The handler class is not annotated. |
| `Missing handlers for service operations` | A method is missing `@OperationImpl`. |
| Banner prints, no `Nexus Poller` | You skipped `registerNexusServiceImplementation`. |

Click **Check** once it is up. Challenge 4 starts both Workers fresh, so you can
stop this one with **Ctrl+C** afterwards.

# What You Know Now

- Slow work starts a Workflow and returns. Fast work answers in the call.
- `WorkflowRunOperation` makes a retry re-attach instead of starting a duplicate.
- A Worker starts fine with nothing registered. A clean banner proves nothing.
- The Endpoint maps a name to a Namespace and a Task Queue.
- Compliance now runs in its own Namespace, with its own Worker.
