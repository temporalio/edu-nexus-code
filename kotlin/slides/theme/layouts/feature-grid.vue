<!--
  ABOUTME: N-column feature grid. PPT slide 58 (Enterprise Readiness: SECURITY / SUPPORT / SCALE).
  ABOUTME: `items` prop drives the columns; default 3-up.

  Frontmatter usage:
    ---
    layout: feature-grid
    eyebrow: TEMPORAL CLOUD
    title: Enterprise Readiness
    columns: 3
    items:
      - title: SECURITY
        body: AWS Private Link · RBAC, SSO, SCIM/IdP · Dedicated Security Officer
      - title: SUPPORT
        body: 24x7x365 globally staffed support · 15 min response · Design sessions
      - title: SCALE
        body: +1,000,000 updates/sec · 99.999% trailing availability · Enterprise SLA
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import FeatureBlock from '../components/FeatureBlock.vue'

withDefaults(defineProps<{
  eyebrow?: string
  title?: string
  columns?: 2 | 3 | 4
  items: Array<{ title: string; body: string; eyebrow?: string }>
}>(), {
  columns: 3,
})
</script>

<template>
  <div class="slidev-layout feature-grid bg-grid">
    <header v-if="eyebrow || title" class="grid-header">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1 v-if="title" class="title">{{ title }}</h1>
    </header>
    <div class="grid" :style="{ gridTemplateColumns: `repeat(${columns}, 1fr)` }">
      <FeatureBlock
        v-for="(item, i) in items"
        :key="i"
        :title="item.title"
        :eyebrow="item.eyebrow"
      >
        <p>{{ item.body }}</p>
      </FeatureBlock>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.feature-grid {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 1.4rem;
}
.eyebrow {
  margin: 0 0 0.3rem;
  font-size: 0.85rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.title {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  gap: 1.5rem;
}
.grid > :deep(.feature-block) {
  padding-top: 0.7rem;
  border-top: 1px solid var(--temporal-border-hi);
}
</style>
