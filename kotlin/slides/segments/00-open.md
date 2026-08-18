---
layout: default
---

# The 3 AM failure

2 Teams. Compliance team ensures payments are legal.
Payments team ensures payments go through successfully.
They're bound together in a single flow.
An issue on one team affects the other team.

<!--
Presenter notes go here.
-->

---
layout: default
---

# Before and after

<div class="flex justify-center mt-2">
  <img
    :src="'/nexus-architecture.svg'"
    alt="Payments and Compliance separated by a Nexus boundary: validatePayment and executePayment on the Payments side, NexusServiceImpl and ComplianceChecker on the Compliance side"
    class="w-full max-h-[62vh]"
  />
</div>

<!--
Your words here. One sentence each for before and after. Name the shape of the
change, not the mechanism — the six concept slides that follow are the mechanism.

The diagram animates: data flows left to right through validate, compliance
check, execute. Let one loop play before you talk over it.

Diagram source, canonical:
temporalio/temporal-learning → docs/tutorials/nexus/ui/architecture-overview.svg
Rendered in the Overview of learn.temporal.io/tutorials/nexus/nexus-sync-tutorial-java
(inlined there as base64). public/nexus-architecture.svg is byte-identical to the
committed file, so re-sync from temporal-learning if the diagram changes.

Sibling diagrams exist in that same ui/ folder if a later segment ever wants one.
-->

---
layout: section
---

# The six words

Everything Nexus adds, before you write any of it.

<!--
Framing line for the block. ~15 seconds. Something to the effect of: six
concepts, one slide each, you'll write all of them today.
-->

---
layout: code-stack
heading: Service
---

::code::

```kotlin {1-2|4-5|all}
@Service
interface ComplianceNexusService {

    @Operation
    fun checkCompliance(request: ComplianceRequest): ComplianceResult
}
```

::default::

Service is a set of Operations that all teams work against. The shared contract.

<!--
Source: shared/nexus/ComplianceNexusService.kt:19-27

- **Build 1 -** @Service marks the contract.
- **Build 2 -** @Operation marks a callable method.
- Do not explain sync vs async here. That is its own slide.
-->

---
layout: code-stack
heading: Operation
---

::code::

```kotlin {3-4|6-7|all}
@Service
interface ComplianceNexusService {
    @Operation
    fun checkCompliance(request: ComplianceRequest): ComplianceResult

    @Operation
    fun submitReview(request: ReviewRequest): ComplianceResult
}
```

::default::

A single, callable method on a Service. Either sync or async.

<!--
Source: shared/nexus/ComplianceNexusService.kt:22-26

- The validation rule is the thing to land. `submitReview` is not called
  until challenge 5 and still needs the annotation today.
- Error they will see: "Missing @Operation annotation"
-->

---
layout: code-stack
heading: Endpoint
proseMax: 58
---

::code::

```bash
temporal operator nexus endpoint create \
  --name compliance-endpoint \
  --target-namespace compliance-namespace \
  --target-task-queue compliance-risk
```

::default::

A reverse proxy that routes Nexus requests from a caller Workflow to a target Namespace and
Task Queue.

- Decouples caller from handler — callers only need to know the Endpoint name; the target Namespace, Task Queue, and internal implementation are hidden.
- Single target — each Endpoint routes to one target Namespace and Task Queue (not multiple backends).
- Immediate availability — adding an Endpoint to the Nexus Registry deploys it instantly for runtime use.
- Serves one or more Nexus Services — multiple Services can run on the same Task Queue behind a single Endpoint.

<!--
- Namespace + Task Queue as the two halves of an address is the reframing
  the room needs, even though they already know both terms.
- The silent failure: point --target-task-queue at a queue nobody polls and
  calls vanish into it. No error. They hit this in challenge 3.
-->

---
layout: code-stack
heading: Caller
---

::code::

```kotlin {1-8|10|all}
//The Caller Workflow
public val NexusCallerWorkflowImpl implements NexusCallerWorkflow {

private val complianceService: ComplianceNexusService =
    //Nexus stub: The delivery app. Use it to place orders
    Workflow.newNexusServiceStub(
        //The menu tells you what you can order
        ComplianceNexusService::class.java,
        NexusServiceOptions.newBuilder()
            .setOperationOptions(/* scheduleToCloseTimeout */)
            .build(),
    )

// ...
//One of the menu items is checkCompliance()
val compliance = complianceService.checkCompliance(compReq)
```

::default::

The Caller **knows the contract, not the address.** The Worker knows the address.

<!--
Source: payments/temporal/PaymentProcessingWorkflowImpl.kt:49-58, 85

- **Build 1 -** the stub replaces an Activity stub.
- **Build 2 -** the call site. Same method name, same input, same output.
- The absence of an Endpoint name is the point of this slide.
-->

---
layout: code-stack
heading: Handler
---

::code::

```kotlin {1-2|4-9|all}
@ServiceImpl(service = ComplianceNexusService::class)
class ComplianceNexusServiceImpl {

    @OperationImpl
    fun checkCompliance():
        OperationHandler<ComplianceRequest, ComplianceResult> =
            WorkflowRunOperation.fromWorkflowHandle { _, _, input ->
                /* start ComplianceWorkflow */
            }
}
```

::default::

The side implementing the Service — runs in a separate Worker, separate deployment.
Different process, different Namespace, different deploy schedule. `@ServiceImpl` links the class
to the contract; `@OperationImpl` marks each answering method.

<!--
Source: compliance/temporal/ComplianceNexusServiceImpl.kt:21-29

- Caller and Handler are the pair the lab uses from challenge 3 onward
  without ever naming. This slide and the last one are where they get named.
- The Worker also needs registerNexusServiceImplementation, or it starts
  fine and silently never answers. Mention, do not put on the slide.
-->

---
layout: code-stack
heading: Sync vs async
---

::code::

```kotlin {1-3|5-8|all}
// async: starts a Workflow, handle bound to a Workflow ID
WorkflowRunOperation.fromWorkflowHandle { _, _, input ->
    WorkflowHandle.fromWorkflowMethod(wf::run, input)
}

// sync: talks to a Workflow already running (Signals, Queries, Updates),
// 10 second budget
OperationHandler.sync { _, _, input ->
    wf.review(input!!.approved, input.explanation)
}
```

::default::

<!--
Source: compliance/temporal/ComplianceNexusServiceImpl.kt:28-58

- The 10 seconds is the maximum time a sync Nexus handler has to fully process a request and return a result — measured from the calling History Service.
- Nexus sync operations complete as part of the start request itself — meaning the caller is holding an open connection waiting for the response.
- If this slide feels cramped, the `comparison` layout is built for exactly
  this shape.
-->

---
layout: default
class: "!p-0"
---

<div class="absolute inset-0 grid grid-cols-2">

  <div class="relative overflow-hidden bg-black">
    <video
      :src="'/mia-programming.mp4'"
      autoplay
      loop
      muted
      playsinline
      class="absolute inset-0 w-full h-full object-cover"
    />
  </div>

  <div class="flex flex-col justify-center items-start gap-5 px-14 bg-glow">
    <p class="eyebrow">TIME TO BUILD</p>
    <h1 class="cta-headline">Open the lab</h1>
    <a class="cta-pill" href="https://t.mp/nexus-kotlin">t.mp/nexus-kotlin</a>
  </div>

</div>

<style scoped>
.cta-headline {
  font-size: 3rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  line-height: 1.05;
  color: var(--temporal-text-strong);
  margin: 0;
}
.cta-pill {
  display: inline-block;
  padding: 0.55rem 1.5rem;
  border: 1px solid var(--temporal-green);
  border-radius: 999px;
  color: var(--temporal-green);
  font-weight: 500;
  font-size: 1.35rem;
  text-decoration: none;
  transition: background 140ms ease;
}
.cta-pill:hover {
  background: rgba(89, 253, 160, 0.14);
}
</style>

<!--
Last slide of segment 0. Leave it up while people get into the sandbox — this is
the only URL they have to type all day, so give them a moment on it.

Ziggy takes a minute to warm the sandbox. Say what is happening while it does:
a Temporal dev server, and two Namespaces, one per team.

This slide opens the lab environment. The per-challenge handoffs are separate,
on the `exercise` layout with its countdown timer, at the end of each segment.
-->
