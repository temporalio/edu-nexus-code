---
slug: the-compliance-side
id: io30tn0s5cxy
type: challenge
title: 3. The Compliance Side
teaser: Implement the handler, register it, and put the Endpoint on the map.
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
  port: 8443
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
  port: 8444
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

Click the [button label="Exercise" background="#444CE7"](tab-1) tab and open
`compliance/temporal/ComplianceNexusServiceImpl.kt`.

Two handlers, two shapes, and picking the wrong shape is the interesting mistake.

**`checkCompliance` is async.** Use `WorkflowRunOperation`. It returns a handle bound
to a Workflow ID, so a retried Operation re-attaches to the Workflow already running.

**`submitReview` is sync.** Use `OperationHandler.sync`. It talks to a Workflow that
is already running rather than starting one, and an Update returns immediately.

The trap: back a long-running check with `OperationHandler.sync` and every retry
starts a **duplicate Workflow**. Sync handlers also get ten seconds total, and a
compliance check can wait on a human.

One Kotlin wrinkle: `OperationHandler.sync` declares its input `@Nullable`, so Kotlin
sees `ReviewRequest?` and you need `!!` on it. The async handler does not.

# Register the Handler

Open `compliance/temporal/ComplianceWorkerApp.kt`.

**TODO 3** is one blank block. A Worker only handles work it has been told about, and
this one owns three things: the Workflow, the Activity, and the Nexus handler. Each is
its own call on `worker`, and each call name starts with `register`.

One of the three takes a constructed instance rather than a class, and that instance
needs a collaborator passed to its constructor. The imports already at the top of the
file tell you which classes you need.

The editor gives you Kotlin highlighting but no autocomplete, so look the names up
rather than waiting for a dropdown. "Ask AI" on https://docs.temporal.io:
"what do I register on a Java Worker that handles Nexus Operations?"

# Create the Endpoint

The Endpoint is the routing rule: a name, a target Namespace, and a target Task Queue.
It is the one piece of Nexus that lives outside your code.

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
temporal operator nexus endpoint create --name compliance-endpoint --target-namespace compliance-namespace --target-task-queue compliance-risk
```

`--target-task-queue` must match the Task Queue in `ComplianceWorkerApp.kt` exactly.
Point it at a queue nobody polls and calls vanish into it.

# Start the Worker

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
./gradlew complianceWorker
```

This is the checkpoint. Look for `Nexus Poller` in the startup lines:

```bash,nocopy
start: MultiThreadedPoller{name=Nexus Poller taskQueue="compliance-risk", ...}
```

That line only appears when a Nexus Service is registered. No `Nexus Poller` means
the handler is not wired, even though the Worker started.

Two failures worth recognizing:

| What you see | What it means |
|---|---|
| `Missing @ServiceImpl annotation` | The class is not annotated. TODO 2a. |
| `Missing handlers for service operations` | A method is missing `@OperationImpl`. |
| Worker starts, no `Nexus Poller` | You skipped `registerNexusServiceImplementation`. |

Click **Check** once it is up. Challenge 4 starts both Workers fresh, so you can
stop this one with **Ctrl+C** afterwards.

# What You Know Now

- Async handlers start Workflows. Sync handlers talk to running ones.
- `WorkflowRunOperation` makes retries re-attach instead of duplicating work.
- A Worker starts fine with nothing registered. Silence is a failure mode.
- The Endpoint maps a name to a Namespace and a Task Queue.
