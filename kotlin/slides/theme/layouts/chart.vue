<!--
  ABOUTME: Chart placeholder. PPT slides 38–40 equivalent.
  ABOUTME: `image` named slot for the chart artwork; `caption` prop for the description.

  Frontmatter usage:
    ---
    layout: chart
    title: Chart #1
    caption: Optional caption explaining the chart
    ---

    ::image::

    ![alt](/path/to/chart.png)
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  title?: string
  caption?: string
}>()
</script>

<template>
  <div class="slidev-layout chart bg-grid">
    <h2 v-if="title" class="title">{{ title }}</h2>
    <figure class="chart-figure">
      <div class="chart-image"><slot name="image" /></div>
      <figcaption v-if="caption">{{ caption }}</figcaption>
    </figure>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.chart {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  margin: 0 0 0.8rem;
  font-size: 1.8rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.chart-figure {
  margin: 0;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  min-height: 0;
}
.chart-image {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1 1 auto;
  min-height: 0;
}
.chart-image :deep(img) {
  max-width: 100%;
  max-height: 100%;
}
figcaption {
  color: var(--temporal-text-muted);
  font-size: 0.95rem;
}
</style>
