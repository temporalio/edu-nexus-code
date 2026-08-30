---
slug: the-compliance-side
id: lwb2jpje3u96
type: challenge
title: 3. The Compliance Side
teaser: Implement both handlers, register them on a Worker, and create the Endpoint.
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

    Two handlers, two different shapes, plus the Worker registration and the
    Endpoint.

    The Solution tab is one click away. Use it if you stall. Learning the shape
    beats staring at a blank function.
tabs:
- id: gok4feicejlo
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/exercise/src
  port: 8080
- id: rhie9uzgyhfq
  title: Temporal UI
  type: service
  hostname: workshop
  path: /
  port: 8233
- id: hcqbwck0rvo9
  title: Terminal
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: jjlm3enbxo4t
  title: Payments Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xy2tk6mpbv0t
  title: Compliance Worker
  type: terminal
  hostname: workshop
  workdir: /root/workshop/exercise
- id: xp7aoiijy8bj
  title: Solution
  type: service
  hostname: workshop
  path: /?folder=/root/workshop/solution/src
  port: 8080
- id: ujvrlheqjxnv
  title: Monolith Architecture
  type: service
  hostname: workshop
  path: /monolith-architecture.html
  port: 8090
difficulty: intermediate
timelimit: 1800
enhanced_loading: null
---

# Everything Compliance Owns

Three pieces, all on this team's side of the boundary.

1. The handlers that answer Nexus calls.
2. The Worker that runs them.
3. The Endpoint that tells Temporal where to route.

# Read the Worked Example First

Click the [button label="Exercise" background="#444CE7"](tab-0) tab and open
`compliance/nexus-handler.ts`.

`checkCompliance` is already written. Read it before you write anything, because the
Operation you are about to write is the other half of the same pattern, and the contrast
between the two is the whole point of this challenge.

| Operation | Shape | Why |
|---|---|---|
| `checkCompliance` *(given)* | `new temporalNexus.WorkflowRunOperationHandler(...)` | Slow. Starts a Workflow and returns a reference to it. |
| `submitReview` *(yours)* | a plain `async (ctx, input) => {...}` | Fast. Answers inside the call, in milliseconds. |

Notice why the given one has to be that shape. Written as a plain async function it would
be a synchronous Operation: cut off at the ten second handler deadline, and every retry
would start a **second** compliance check for the same payment.

# Write the Other Handler

Follow TODO 2 in the same file.

`submitReview` is the fast path, so it is the plain async function. It gets a Client,
finds the Workflow the example started, and hands it the decision. Slow work starts a
Workflow and returns a reference; fast work answers in the call.

When you have written it, the `TS2345` error from challenge 2 disappears.

# Register the Handler

Open `compliance/worker.ts` and follow TODO 3.

The Worker already knows its Workflows and Activities. One option is missing: the one
that makes this team callable by Payments at all.

# Typecheck

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npx tsc --noEmit
```

Silence means it passed. The Payments side still calls the Activity proxy, which is
perfectly valid — you change that in challenge 4.

# Create the Endpoint

The Endpoint is the routing rule: a name, a target Namespace, and a target Task Queue.
It is the one piece of Nexus that lives outside your code.

```bash,run
temporal operator nexus endpoint create --name compliance-endpoint --target-namespace compliance-namespace --target-task-queue compliance-risk
```

`--target-task-queue` must match `COMPLIANCE_TASK_QUEUE` in `shared/types.ts` exactly.
If it points at a queue no Worker is polling, the calls are never answered.

# Start the Worker

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

```bash,nocopy
=========================================================
  Compliance Worker started on: compliance-risk
  Namespace: compliance-namespace
=========================================================
[INFO] Worker state changed { state: 'RUNNING' }
```

The banner only tells you the process is alive. It prints whether or not you registered
anything, so it is not proof. Two things are.

**One: a line that should NOT be there.** Scroll the Worker output and look for this:

```bash,nocopy
[INFO] No Nexus services registered, not polling for Nexus tasks
```

If you see it, your handler is not registered even though the Worker started happily.
When `nexusServices` is set, that line does not appear at all. Absence is the signal here,
which is exactly the kind of thing that is easy to miss — so look for it deliberately.

**Two: the Temporal UI.** Click the
[button label="Temporal UI" background="#444CE7"](tab-1) tab, switch the Namespace
selector to `compliance-namespace`, and open **Workers** in the left menu. Your Worker
is listed as **Running**.

That is Compliance appearing in its own Namespace for the first time. In challenge 1
this Namespace was empty.

If something went wrong:

| What you see | What it means |
|---|---|
| `TS2345 ... missing ... checkCompliance` | A handler is missing from the object. |
| `No Nexus services registered` | You skipped `nexusServices` on `Worker.create`. |
| Worker starts, calls never answered | The Task Queue does not match the Endpoint's target. |

Click **Check** once it is up. Challenge 4 starts both Workers fresh, so you can
stop this one with **Ctrl+C** afterwards.

# What You Know Now

- Slow work starts a Workflow and returns. Fast work answers in the call.
- `WorkflowRunOperationHandler` makes a retry re-attach instead of starting a duplicate.
- A Worker starts fine with nothing registered. A clean banner proves nothing.
- The Endpoint maps a name to a Namespace and a Task Queue.
- Compliance now runs in its own Namespace, with its own Worker.
