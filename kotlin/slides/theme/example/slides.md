---
theme: ../
title: Temporal Slidey Slides — Reference Deck
info: |
  Reference deck for slidev-theme-temporal.
  One slide per layout, in PPT order. Copy any slide into your own deck.
themeConfig:
  footer: "Temporal Slidey Slides · 2026"
  toc:
    - id: arch
      label: Architecture
    - id: tour
      label: Layout Tour
    - id: cloud
      label: Temporal Cloud
    - id: wrap
      label: Wrap-Up
layout: cover
variant: planet-teal
---

# Temporal Slidey Slides

## Reference deck for slidev-theme-temporal

Month YYYY · Confidential

---
layout: cover
variant: planet-purple
---

# Cover — planet-purple

## Warm-toned planet on the perspective grid

Month YYYY

---
layout: cover
variant: rex
---

# Cover — rex

## T-Rex constellation backdrop

Month YYYY

---
layout: cover
variant: grid
---

# Cover — grid

## Perspective grid with corner glows

Month YYYY

---
layout: cover
variant: glow
---

# Cover — glow

## Subtle gradient glow only — minimal

Month YYYY

---
layout: toc
current: tour
---

<!-- Workshop / agenda TOC. Section list is driven by themeConfig.toc above. -->

---
layout: section
---

# Section title

---
layout: subsection
eyebrow: SECTION
---

# Subsection title

---
layout: default
---

# Simple content

Yeah, but your scientists were so preoccupied with whether or not they could,
they didn't stop to think if they should. Eventually, you do plan to have
dinosaurs on your dinosaur tour, right?

---
layout: image-right
---

## Headline with image on the right

Body copy on the left column. Eventually, you do plan to have dinosaurs on
your dinosaur tour, right?

::image::

<div class="asset-pending">Right-column figure</div>

---
layout: image-left
---

::image::

<div class="asset-pending">Left-column figure</div>

::default::

## Headline with image on the left

Body copy on the right column.

---
layout: two-cols
---

## Left column

Look again at that dot. That's here. That's home. That's us.

::right::

## Right column

The aggregate of our joy and suffering, thousands of confident religions,
ideologies, and economic doctrines.

---
layout: two-cols-header
title: Science as a Candle in the Dark
eyebrowLeft: COLUMN HEADER
eyebrowRight: COLUMN HEADER
---

Look again at that dot. That's here. That's home. That's us. On it everyone
you love, everyone you know, everyone you ever heard of lived out their lives.

::right::

The Earth is the only world known so far to harbor life. There is nowhere
else, at least in the near future, to which our species could migrate.

---
layout: eyebrow-hero
eyebrow: TEMPORAL CLOUD
---

# Cost Effectiveness

Optional supporting copy that runs underneath the headline.

---
layout: checklist
eyebrow: Temporal for
title: Add-ons
subtitle: Here's a neat list of add-ons
items:
  - Availability SLA
  - Design sessions and training
  - 24/7/365 on-call support
  - Multi-regional and multi-cloud
  - Audit logging · Export
  - RBAC, SSO, SCIM/IdP, Private Link
  - Service Accounts & API Keys
---

---
layout: feature-card
featureTheme: FEATURE THEME
featureName: Feature Name
stage: pre-release
target: Public Preview target H1 2025
---

::big-picture::

Describes the problem this feature solves, or the use case.

::what-youll-get::

Concisely describe the what's-in-it-for-me.

::later::

Concisely outline what's planned to come in future releases.

---
layout: feature-card
featureName: Same Feature
stage: public-preview
target: Public Preview target H1 2025
---

::big-picture::

Same card shape with a different release-stage chip.

::what-youll-get::

What changes when a feature moves to Public Preview.

::later::

What's still on the roadmap.

---
layout: feature-card
featureName: Same Feature
stage: general-availability
target: GA July 2025
---

::big-picture::

GA variant of the feature card.

::what-youll-get::

Stable production SLA, full documentation, all language SDKs.

::later::

Future enhancements outside the GA scope.

---
layout: big-stat
value: "99.9"
unit: "%"
caption: 60% of the time, it works every time.
---

---
layout: profile
initials: ZS
name: Ziggy Stardust
role: Job Title
company: Company
---

---
layout: quote
name: Carl Sagan
role: Astronomer
initials: CS
---

The universe is a pretty big place. If it's just us, seems like an awful
waste of space.

---
layout: quote
name: Guillermo Rauch
role: Founder & CEO
initials: GR
---

One of the most interesting pieces of tech I've seen in years. Temporal does
to backend and infra what React did to frontend — the surface exposed to the
developer is a beautiful render() function to organize your backend workflows.

---
layout: default
---

# Table example

| Tier | Codename | Cost | Volume | Margin |
|------|----------|------|--------|--------|
| Tier 1 | Alpha   | $10/GB | 200ml | 50% |
| Tier 2 | Beta    | $10/GB | 200ml | 50% |
| Tier 3 | Charlie | $10/GB | 200ml | 50% |

---
layout: chart
title: Chart #1
caption: Replace the placeholder with a chart image
---

::image::

<div class="asset-pending">Chart artwork</div>

---
layout: timeline
title: Timeline
points:
  - date: "2022"
    label: Founding
  - date: "2023"
    label: Series A
  - date: "2024"
    label: Cloud GA
  - date: "2025"
    label: Multi-region
  - date: "2026"
    label: Today
---

---
layout: release-stages
title: How we release capabilities
---

::pre-release::

Self-Hosted: Enable env or Labs mode. Cloud: invite only for Design Partners.

::public-preview::

New features in Public Preview are available to everyone. Documentation is
available. May undergo further development.

::general-availability::

Fully developed, tested, and available for use without further anticipated
changes.

---
layout: code-explain
title: Durable Execution
---

::code::

```python
def process_order(order):
    check_fraud(order.order_id, order.payment_info)
    prepare_shipment(order)
    charge_confirm = charge(order.order_id, order.payment_info)
    shipment_confirmation = ship(order)
```

::default::

**Durable Execution** is a new abstraction. It is a fault-oblivious
development model that preserves full application state in the case of any
host or software failure. It ensures your apps and end-to-end services are
*correct and reliable*.

---
layout: architecture
eyebrow: ARCHITECTURE
title: Before Temporal
variant: before
---

::image::

<!-- Designer ships the SVG; in the meantime the asset-pending placeholder shows. -->

::default::

Multi-service tangle: Order Service orchestrates Payment, Shipping, Inventory,
Notifications, plus cron jobs and a dead-letter queue.

---
layout: architecture
eyebrow: ARCHITECTURE
title: After Temporal
variant: after
---

::image::

::default::

Single Order Workflow drives Payment, Shipping, Inventory, Notifications with
automatic retries and rollback.

---
layout: cta-icons
title: Join the Conversation
columns: 3
items:
  - title: Ask questions
    body: Ask in Zoom Q&A — we'll answer live or async in Q&A, or after the call in the Slack community.
    action: Zoom Q&A
  - title: Slack community
    body: Ask questions in #events-live.
    action: t.mp/slack
    href: https://t.mp/slack
  - title: Newsletter
    body: Monthly community newsletter with product updates and releases.
    action: temporal.io/community
    href: https://temporal.io/community
---

---
layout: cta-banner
eyebrow: FOR STARTUPS
headline: $6,000 in free Cloud Credits
subhead: with less than 30 Million in funding
cta: apply at temporal.io/startup
href: https://temporal.io/startup
---

---
layout: checklist
eyebrow: Start a POC
title: What makes a great POC
subtitle: Valuable processes that can be feature-flagged, measured easily, and demonstrate immediate ROI without disrupting core systems.
items:
  - Background jobs
  - File processing
  - API integrations
  - Scheduled tasks
  - Simple Workflows
  - Microservices
  - Event-Driven systems
---

---
layout: comparison
leftEyebrow: Open source
leftTitle: Temporal OSS
leftFeatures:
  - Self-host on your own infra
  - Full source access
  - Good for teams with deep Cassandra expertise
rightEyebrow: Temporal Cloud
rightTitle: Everything you love without the infra
rightFeatures:
  - Availability SLA
  - Design sessions and training
  - 24/7/365 on-call support
  - Multi-regional and multi-cloud
  - Audit logging · Export
  - RBAC, SSO, SCIM/IdP, Private Link
---

Temporal is committed to the open-source community and the self-hosted
project is proven and well documented.

::right::

Everything you love about the Temporal open-source project without the
infrastructure overhead.

---
layout: model-diagram
title: The Temporal Model
caption: Developers / Productivity / Experience / Cash / Applications
---

::image::

<!-- Designer ships the SVG circle diagram; asset-pending placeholder until then. -->

---
layout: success-story
eyebrow: SUCCESS STORY
companyName: Dust
headline: AI assistant platform building complex, long-running agentic workflows
---

Building complex, long-running agentic workflows as part of the core
platform. Agents listen for changes in Slack, GitHub, or Notion and act on
them.

::challenges::

- Building complex agentic workflows on traditional queues was too complex
- Real-time observability gaps slowed troubleshooting
- Scaling needed to be straightforward

::solution::

- Adopted Temporal from day 0 for orchestration
- Easy scaling by adding more Workers
- Real-time observability for faster troubleshooting

---
layout: success-story
eyebrow: SUCCESS STORY
companyName: ZoomInfo
headline: AI-driven sales product — predict the next best customer
---

::challenges::

- **High scale:** input billions of signals across many data sources
- **High impact:** help marketing & sales leaders predict who their next best customers are going to be
- Complex, fragile architecture in the previous system

::solution::

- Temporal orchestrates RAG for "Account Overviews" and "Account Summaries"
- **Reduced LLM costs:** only send necessary context
- **More reliable:** complex pipeline simplified

---
layout: eyebrow-hero
eyebrow: TEMPORAL CLOUD
---

# Built for the enterprise

Hero intro before the enterprise-readiness feature grid.

---
layout: feature-grid
eyebrow: TEMPORAL CLOUD
title: Enterprise Readiness
columns: 3
items:
  - title: SECURITY
    body: AWS Private Link · RBAC, SSO, SCIM/IdP · Dedicated Security Officer · Audit Logging · Data Encoder.
  - title: SUPPORT
    body: 24x7x365 globally staffed support · 15 min response time for production · Design sessions and training.
  - title: SCALE
    body: +1,000,000 updates per second · 99.999% trailing service availability · Enterprise SLA.
---

---
layout: qa
---

# Q&A

---
layout: end
---

# Thank you

Questions? **temporal.io** · `community.temporal.io` · `temporal.io/slack`
