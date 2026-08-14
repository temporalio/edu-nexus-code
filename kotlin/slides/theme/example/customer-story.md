---
theme: ../
title: Customer Story
info: Themed mini-deck for customer success stories.
themeConfig:
  footer: "Customer Story · 2026"
layout: cover
variant: grid
---

# How Acme scaled to a billion workflows

## A customer story

Replay 2026

---
layout: success-story
eyebrow: CUSTOMER STORY
companyName: Acme
headline: AI-native ops platform serving billions of events per day
---

::challenges::

- **High scale:** input billions of signals across many data sources
- Custom orchestrator written in Go was hitting maintenance ceiling
- Bursty traffic broke the queue-based system

::solution::

- Adopted Temporal in production within 90 days
- Replaced custom orchestrator with `OpsWorkflow` family
- Auto-scaling Workers handle bursty load

---
layout: big-stat
value: "10x"
caption: Throughput vs. the previous queue-based system
---

---
layout: quote
name: Jane Doe
role: VP Engineering, Acme
initials: JD
---

Temporal's durable execution model let us delete an entire category of
"queue went sideways at 3am" alerts. We sleep better.

---
layout: profile
initials: JD
name: Jane Doe
role: VP Engineering
company: Acme
---

---
layout: end
---

# Thank you

Acme is hiring · `acme.example/careers`
