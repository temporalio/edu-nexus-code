<!--
  ABOUTME: Text on the left, image slot on the right. PPT slide 17 equivalent.
  ABOUTME: Use the `image` named slot for the figure.

  Frontmatter usage:
    ---
    layout: image-right
    ---

    ## Headline

    Body copy on the left column.

    ::image::

    ![alt](/path/to/image.png)
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
</script>

<template>
  <div class="slidev-layout image-right bg-grid">
    <div class="image-right-grid">
      <div class="col col-text"><slot /></div>
      <div class="col col-image"><slot name="image" /></div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.image-right {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.image-right-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
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
