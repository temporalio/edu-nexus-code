---
theme: ../
title: Feature Launch
info: Themed mini-deck for product feature launches.
themeConfig:
  footer: "Feature Launch · 2026"
layout: cover
variant: planet-purple
---

# Nexus

## Cross-namespace workflow calls

Launching Q3 2026

---
layout: eyebrow-hero
eyebrow: WHAT'S NEW
---

# Nexus

Cross-namespace, cross-cluster workflow calls without the boilerplate.

---
layout: feature-card
featureTheme: NEXUS
featureName: Nexus operations
stage: public-preview
target: GA target Q4 2026
---

::big-picture::

Calling workflows in another namespace currently requires custom RPC plumbing,
auth wiring, and bespoke retries.

::what-youll-get::

A typed `WorkflowHandle` you can pass across namespace boundaries with the
same retry semantics you already get inside one namespace.

::later::

Inline scheduling, multi-cluster routing, and richer ACLs.

---
layout: release-stages
title: How Nexus rolls out
---

::pre-release::

Self-hosted only — `TEMPORAL_NEXUS_ENABLED=1`. Design partners on Cloud.

::public-preview::

Cloud Public Preview, opt-in via UI. Full documentation. Breaking changes possible.

::general-availability::

Stable production SLA in all regions.

---
layout: comparison
leftEyebrow: Before
leftTitle: Cross-namespace today
leftFeatures:
  - Custom RPC stub per service
  - Hand-rolled retries
  - Auth and identity glued by hand
rightEyebrow: With Nexus
rightTitle: Cross-namespace tomorrow
rightFeatures:
  - Typed WorkflowHandle across namespaces
  - Built-in retries and replay
  - Native auth wiring
---

::right::

Same primitives, no infrastructure.

---
layout: cta-banner
headline: Join the design partner program
subhead: We're shaping Nexus with early customers
cta: temporal.io/nexus
href: https://temporal.io/nexus
---

---
layout: end
---

# Questions?

`temporal.io/nexus` · `community.temporal.io`
