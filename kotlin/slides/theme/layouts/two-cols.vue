<!--
  Two-column layout with Temporal spacing.
  Markdown:
    ---
    layout: two-cols
    ---
    ## Left column
    ::right::
    ## Right column
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
</script>

<template>
  <div class="slidev-layout two-cols bg-grid">
    <div class="two-cols-grid">
      <div class="col col-left"><slot /></div>
      <div class="col col-right"><slot name="right" /></div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.two-cols {
  display: flex;
  flex-direction: column;
  height: 100%;
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
</style>
