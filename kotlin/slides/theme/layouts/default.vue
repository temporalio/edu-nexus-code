<!--
  Default content layout. Subtle perspective-grid background, Temporal footer.
  Flex column so the footer is always pinned to the bottom of the slide canvas.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
</script>

<template>
  <div class="slidev-layout default bg-grid">
    <div class="slide-content"><slot /></div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.default {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.slide-content {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}
.slide-content :deep(br) {
  display: block;
  content: '';
  margin-top: 0.25rem;
  line-height: 0;
}
</style>
