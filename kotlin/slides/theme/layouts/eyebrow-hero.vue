<!--
  ABOUTME: Eyebrow tag above a giant heading. PPT slides 27–28 ("TEMPORAL CLOUD / Cost Effectiveness").
  ABOUTME: Use for product-category intro slides.

  Frontmatter usage:
    ---
    layout: eyebrow-hero
    eyebrow: TEMPORAL CLOUD
    ---

    # Cost Effectiveness

    Optional supporting copy goes here.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{ eyebrow?: string }>()
</script>

<template>
  <div class="slidev-layout eyebrow-hero bg-grid">
    <div class="hero-inner">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <slot />
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.eyebrow-hero {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.hero-inner {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 0;
}
.eyebrow {
  margin: 0 0 1rem;
  font-size: 1rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--temporal-green);
  font-weight: 500;
}
.eyebrow-hero :deep(h1) {
  font-size: 4.2rem;
  font-weight: 200;
  letter-spacing: -0.025em;
  line-height: 1.02;
  color: var(--temporal-text-strong);
  margin: 0;
  max-width: 16ch;
}
.eyebrow-hero :deep(p) {
  margin-top: 1.2rem;
  color: var(--temporal-text);
  max-width: 48ch;
}
</style>
