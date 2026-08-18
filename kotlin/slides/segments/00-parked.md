
<!--
PARKED SLIDES. This file is deliberately NOT imported by slides.md, so
  nothing here renders.

  These two slides were written before the Nexus overview pages were added.
  Those pages now tell the before-and-after story in more detail, so these
  became redundant rather than wrong. Kept in case you want either back:
  add an src: ./segments/00-parked.md entry to slides.md, or paste a block
  into 00-open.md.
-->

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
