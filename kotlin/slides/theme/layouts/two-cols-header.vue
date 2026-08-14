<!--
  ABOUTME: Two-column layout with a shared title row above. Optional uppercase eyebrows per column.
  ABOUTME: PPT slides 19–24 equivalent (CUSTOM_7 family).

  Frontmatter usage:
    ---
    layout: two-cols-header
    title: Science as a Candle in the Dark
    eyebrowLeft: COLUMN HEADER
    eyebrowRight: COLUMN HEADER
    ---

    Left column body…

    ::right::

    Right column body…
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  title?: string
  eyebrowLeft?: string
  eyebrowRight?: string
}>()
</script>

<template>
  <div class="slidev-layout two-cols-header bg-grid">
    <h2 v-if="title" class="overall-title">{{ title }}</h2>
    <div class="two-cols-grid">
      <div class="col col-left">
        <p v-if="eyebrowLeft" class="eyebrow">{{ eyebrowLeft }}</p>
        <slot />
      </div>
      <div class="col col-right">
        <p v-if="eyebrowRight" class="eyebrow">{{ eyebrowRight }}</p>
        <slot name="right" />
      </div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.two-cols-header {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.overall-title {
  margin: 0 0 1.2rem;
  font-size: 2rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.two-cols-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2.5rem;
  align-items: start;
  overflow: hidden;
}
.eyebrow {
  margin: 0 0 0.6rem;
  font-size: 0.75rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--temporal-green);
  font-weight: 500;
}
</style>
