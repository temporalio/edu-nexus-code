<!--
  TOC layout. Two columns: short heading on the left, full section list
  on the right. Background is the plain perspective grid.
  Use before each section divider with:
    ---
    layout: toc
    current: <section-id>
    ---
-->
<script setup lang="ts">
import WorkshopToc from '../components/WorkshopToc.vue'

defineProps({
  current: { type: String, default: '' },
})
</script>

<template>
  <div class="slidev-layout toc bg-grid">
    <div class="toc-grid">
      <div class="lead">
        <div class="eyebrow">Workshop</div>
      </div>
      <WorkshopToc :current="current" />
    </div>
  </div>
</template>

<style scoped>
.toc {
  display: flex;
  align-items: center;
}
.toc-grid {
  width: 100%;
  display: grid;
  grid-template-columns: 10rem 1fr;
  gap: 3rem;
  align-items: center;
}
.lead {
  align-self: start;
  padding-top: 0.25rem;
}
.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 0.8rem;
  color: var(--temporal-green);
  margin-bottom: 0.5rem;
}
.lead h1 {
  font-size: 2rem;
  font-weight: 200;
  color: #ffffff;
  margin: 0;
  line-height: 1.1;
}
</style>
