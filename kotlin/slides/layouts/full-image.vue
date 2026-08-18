<!--
  ABOUTME: One image filling the whole slide, no chrome.
  ABOUTME: For slides imported from another deck as rendered pages.

  The prop is `image`, not `src`: Slidev reserves `src` in frontmatter for
  importing another markdown file as slides.

  Frontmatter usage:
    ---
    layout: full-image
    image: /nexus-intro/04.webp
    alt: Temporal-based services in an application
    ---

  No footer. These pages carry their own page number and logo baked in, so the
  theme's footer would double up on both.
-->
<script setup lang="ts">
defineProps<{
  image: string;
  alt?: string;
}>();
</script>

<template>
  <div class="slidev-layout full-image">
    <img :src="image" :alt="alt ?? ''" class="page" />
    <slot />
  </div>
</template>

<style scoped>
.full-image {
  padding: 0 !important;
  height: 100%;
  width: 100%;
  background: #000;
  overflow: hidden;
}
.page {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
</style>
