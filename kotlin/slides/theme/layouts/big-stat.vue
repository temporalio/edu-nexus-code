<!--
  ABOUTME: Big-stat hero. PPT slide 32 ("99.9 / 60% of the time, it works every time").
  ABOUTME: Huge value + caption. Optional ribbon graphic via the `ribbon` slot (asset-blocked §5.10).

  Frontmatter usage:
    ---
    layout: big-stat
    value: 99.9
    unit: "%"
    caption: 60% of the time, it works every time
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  value: string | number
  unit?: string
  caption?: string
}>()
</script>

<template>
  <div class="slidev-layout big-stat bg-grid">
    <div class="stat-inner">
      <div class="ribbon-slot"><slot name="ribbon" /></div>
      <div class="stat-row">
        <span class="value">{{ value }}</span>
        <span v-if="unit" class="unit">{{ unit }}</span>
      </div>
      <p v-if="caption" class="caption">{{ caption }}</p>
      <slot />
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.big-stat {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.stat-inner {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 1.2rem;
  min-height: 0;
}
.ribbon-slot {
  min-height: 0;
}
.ribbon-slot:empty {
  display: none;
}
.stat-row {
  display: flex;
  align-items: baseline;
  gap: 0.3rem;
}
.value {
  font-size: 9rem;
  line-height: 1;
  font-weight: 200;
  letter-spacing: -0.04em;
  color: var(--temporal-text-strong);
  font-variant-numeric: tabular-nums;
}
.unit {
  font-size: 3.5rem;
  font-weight: 200;
  color: var(--temporal-green);
}
.caption {
  margin: 0;
  font-size: 1.3rem;
  color: var(--temporal-text);
  max-width: 36ch;
}
</style>
