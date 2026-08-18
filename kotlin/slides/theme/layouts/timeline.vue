<!--
  ABOUTME: Horizontal timeline rail. PPT slides 41 and 43 equivalent.
  ABOUTME: Each point: { label, date }. Auto-spaces across the slide.

  Frontmatter usage:
    ---
    layout: timeline
    title: Timeline
    points:
      - date: 2022
        label: Founding
      - date: 2023
        label: Series A
      - date: 2024
        label: Cloud GA
      - date: 2025
        label: Multi-region
      - date: 2026
        label: Today
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import TimelineRail from '../components/TimelineRail.vue'

defineProps<{
  title?: string
  points: Array<{ label: string; date: string }>
}>()
</script>

<template>
  <div class="slidev-layout timeline bg-grid">
    <h2 v-if="title" class="title">{{ title }}</h2>
    <div class="rail-wrap">
      <TimelineRail :points="points" />
    </div>
    <div class="extra"><slot /></div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  margin: 0 0 1rem;
  font-size: 2rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.rail-wrap {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  min-height: 0;
}
.extra :deep(p) {
  margin: 0.4rem 0;
}
</style>
