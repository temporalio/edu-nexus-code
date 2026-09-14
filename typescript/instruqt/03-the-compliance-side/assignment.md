---
slug: the-compliance-side
id: lwb2jpje3u96
type: challenge
title: 3. The compliance side
teaser: Implement both handlers, register them on a Worker, and create the Endpoint.
notes:
- type: text
  contents: |-
    # A compliance check can wait on a human. How do you put that behind an RPC?

    Some checks clear in milliseconds. A $12,000 international transfer waits
    for an officer to click approve, and that can take an afternoon.

    A synchronous handler has ten seconds.
- type: text
  contents: |-
    # This is the hardest challenge in the lab

    Two handlers in two different shapes, plus the Worker registration and the
    Endpoint.

    The finished code sits in solution/ in the same file tree. Use it if you
    stall.
tabs:
- id: gok4feicejlo
  title: Exercise
  type: service
  hostname: workshop
  path: /?folder=/root/workshop
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

# Everything Compliance owns

Three pieces, all on this team's side of the boundary: the handlers that answer Nexus
calls, the Worker that runs them, and the Endpoint that tells Temporal where to route.

# Read the worked example first

Click the [button label="Exercise" background="#444CE7"](tab-0) tab and open
`exercise/src/compliance/nexus-handler.ts`. `checkCompliance` is written for you, and the
Operation you are about to write is the other half of the same pattern.

| Operation | Shape | Why |
|---|---|---|
| `checkCompliance` *(given)* | `new temporalNexus.WorkflowRunOperationHandler(...)` | Slow. Starts a Workflow and returns a reference to it. |
| `submitReview` *(yours)* | a plain `async (ctx, input) => {...}` | Fast. Answers inside the call, in milliseconds. |

Written as a plain async function, `checkCompliance` would be a synchronous Operation,
cut off at the ten second handler deadline, and every retry would start a second
compliance check for the same payment.

# Write the other handler

Follow TODO 2 in the same file. Once you have written it, the `TS2345` error from
challenge 2 disappears.

If you get `TS7006: Parameter '_ctx' implicitly has an 'any' type` instead, TODO 1 from
challenge 2 is unfinished. Handler parameter types are inferred from the contract, so an
empty Service leaves nothing to infer from.

# Register the handler

Open `exercise/src/compliance/worker.ts` and follow TODO 3. The Worker already knows its
Workflows and Activities. One option is missing, the one that makes this team callable by
Payments at all.

# Typecheck

Click the [button label="Terminal" background="#444CE7"](tab-2) tab:

```bash,run
npx tsc --noEmit
```

Silence means it passed. The Payments side still calls the Activity proxy, which is
valid. You change that in challenge 4.

# Create the Endpoint

The Endpoint is the routing rule: a name, a target Namespace, and a target Task Queue. It
is the one piece of Nexus that lives outside your code.

```bash,run
temporal operator nexus endpoint create --name compliance-endpoint --target-namespace compliance-namespace --target-task-queue compliance-risk
```

`--target-task-queue` must match `COMPLIANCE_TASK_QUEUE` in
`exercise/src/shared/types.ts` exactly. Point it at a queue no Worker polls and the calls
are never answered.

# Start the Worker

Click the [button label="Compliance Worker" background="#444CE7"](tab-4) tab:

```bash,run
npm run compliance-worker
```

The startup banner prints whether or not you registered anything, so it proves nothing.
Two things do. First, scroll the Worker output and look for a line that should be absent:

```bash,nocopy
[INFO] No Nexus services registered, not polling for Nexus tasks
```

If it is there, your handler is not registered even though the Worker started happily.
With `nexusServices` set, that line does not appear at all.

Second, click the [button label="Temporal UI" background="#444CE7"](tab-1) tab, switch
the Namespace selector to `compliance-namespace`, and open **Workers**. Yours is the
TypeScript Worker on `compliance-risk`, listed as **Running**. The Go one on
`temporal-sys-per-ns-tq` is Temporal Server's own and shows `Tasks Processed: 0` all day.
In challenge 1 no Worker of yours was here at all.

| What you see | What it means |
|---|---|
| `TS2345 ... missing ... checkCompliance` | A handler is missing from the object. |
| `TS7006 ... '_ctx' implicitly has an 'any' type` | TODO 1 is unfinished. |
| `No Nexus services registered` | You skipped `nexusServices` on `Worker.create`. |
| Worker starts, calls never answered | The Task Queue does not match the Endpoint's target. |

Click **Check** once it is up. Challenge 4 starts both Workers fresh, so you can stop
this one with **Ctrl+C** afterwards.

# What you know now

- Slow work starts a Workflow and returns. Fast work answers in the call.
- `WorkflowRunOperationHandler` makes a retry re-attach instead of starting a duplicate.
- A Worker starts fine with nothing registered, so a clean banner proves nothing.
- The Endpoint maps a name to a Namespace and a Task Queue.
- Compliance now runs in its own Namespace, with its own Worker.

---

Tell us what worked in the **Feedback** tab. It takes a few seconds.
