<!--
  ABOUTME: OSS vs Cloud (or any two-card) comparison. PPT slides 50–53 equivalent.
  ABOUTME: Each card has eyebrow, title, body, and a checklist of features.

  Frontmatter usage:
    ---
    layout: comparison
    leftEyebrow: Open source
    leftTitle: Temporal OSS
    leftFeatures:
      - Self-host on your infra
      - Full source access
    rightEyebrow: Temporal Cloud
    rightTitle: Everything you love without the infra
    rightFeatures:
      - Availability SLA
      - 24/7/365 on-call support
      - RBAC, SSO, SCIM/IdP, Private Link
    ---

    Optional shared intro paragraph.

    ::right::

    Optional right-card body paragraph (renders above the right feature list).
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import Checkmark from '../components/Checkmark.vue'

defineProps<{
  leftEyebrow?: string
  leftTitle: string
  leftFeatures?: string[]
  rightEyebrow?: string
  rightTitle: string
  rightFeatures?: string[]
}>()
</script>

<template>
  <div class="slidev-layout comparison bg-grid">
    <div class="comparison-grid">
      <section class="card card-left">
        <p v-if="leftEyebrow" class="eyebrow">{{ leftEyebrow }}</p>
        <h3 class="card-title">{{ leftTitle }}</h3>
        <div class="card-body"><slot /></div>
        <ul v-if="leftFeatures?.length" class="features">
          <li v-for="(f, i) in leftFeatures" :key="i"><Checkmark /> <span>{{ f }}</span></li>
        </ul>
      </section>
      <section class="card card-right">
        <p v-if="rightEyebrow" class="eyebrow">{{ rightEyebrow }}</p>
        <h3 class="card-title">{{ rightTitle }}</h3>
        <div class="card-body"><slot name="right" /></div>
        <ul v-if="rightFeatures?.length" class="features">
          <li v-for="(f, i) in rightFeatures" :key="i"><Checkmark /> <span>{{ f }}</span></li>
        </ul>
      </section>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.comparison {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.comparison-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.card {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 1.3rem;
  border: 1px solid var(--temporal-border-hi);
  border-radius: 0.6rem;
  background: rgba(11, 16, 32, 0.45);
}
.card-right {
  border-color: rgba(89, 253, 160, 0.45);
  box-shadow: 0 0 0 1px rgba(89, 253, 160, 0.15) inset;
}
.eyebrow {
  margin: 0;
  font-size: 0.75rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.card-title {
  margin: 0;
  color: var(--temporal-text-strong);
  font-weight: 400;
  font-size: 1.3rem;
  line-height: 1.2;
}
.card-body :deep(p) {
  margin: 0.3rem 0;
  color: var(--temporal-text);
  font-size: 0.95rem;
}
.features {
  list-style: none;
  margin: 0.5rem 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.features li {
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
  color: var(--temporal-text);
  font-size: 0.95rem;
}
.features :deep(.checkmark) {
  color: var(--temporal-green);
  flex-shrink: 0;
}
</style>
