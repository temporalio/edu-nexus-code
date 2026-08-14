<!--
  ABOUTME: Image slot on the left, text on the right. PPT slide 18 equivalent.
  ABOUTME: Use the `image` named slot for the figure.

  Frontmatter usage:
    ---
    layout: image-left
    ---

    ::image::

    ![alt](/path/to/image.png)

    ::default::

    ## Headline

    Body copy on the right column.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
</script>

<template>
  <div class="slidev-layout image-left bg-grid">
    <div class="image-left-grid">
      <div class="col col-image"><slot name="image" /></div>
      <div class="col col-text"><slot /></div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.image-left {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.image-left-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1.05fr;
  gap: 2.5rem;
  align-items: center;
  overflow: hidden;
}
.col-image {
  display: flex;
  align-items: center;
  justify-content: center;
}
.col-image :deep(img) {
  max-width: 100%;
  max-height: 100%;
  border-radius: 0.4rem;
}
</style>
