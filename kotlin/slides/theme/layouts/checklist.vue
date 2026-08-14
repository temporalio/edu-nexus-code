<!--
  ABOUTME: "Temporal for X" tick-list grid. PPT slides 25, 26, 49 equivalent.
  ABOUTME: Items prop renders each row with a mint Checkmark. Two-column grid when >5 items.

  Frontmatter usage:
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
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import Checkmark from '../components/Checkmark.vue'

const props = defineProps<{
  eyebrow?: string
  title: string
  subtitle?: string
  items: string[]
}>()

const twoCol = props.items?.length > 5
</script>

<template>
  <div class="slidev-layout checklist bg-grid">
    <div class="checklist-inner">
      <header class="checklist-header">
        <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
        <h1 class="title">{{ title }}</h1>
        <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
      </header>
      <ul class="items" :class="{ 'two-col': twoCol }">
        <li v-for="(item, i) in items" :key="i">
          <Checkmark /> <span>{{ item }}</span>
        </li>
      </ul>
      <slot />
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.checklist {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.checklist-inner {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.checklist-header {
  margin-bottom: 1.6rem;
}
.eyebrow {
  margin: 0 0 0.3rem;
  font-size: 0.85rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.title {
  margin: 0;
  font-size: 3rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
  line-height: 1.05;
}
.subtitle {
  margin: 0.6rem 0 0;
  color: var(--temporal-text);
  font-size: 1.05rem;
}
.items {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}
.items.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 2.5rem;
  row-gap: 0.55rem;
}
.items li {
  display: flex;
  align-items: baseline;
  gap: 0.65rem;
  color: var(--temporal-text);
  font-size: 1.1rem;
  font-weight: 300;
}
.items li :deep(.checkmark) {
  color: var(--temporal-green);
  flex-shrink: 0;
}
</style>
