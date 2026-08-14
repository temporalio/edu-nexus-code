<!--
  ABOUTME: Persistent footer for content layouts: deck title (left) + page indicator (right).
  ABOUTME: Left text reads from `themeConfig.footer` in deck frontmatter; empty string hides it.

  Frontmatter example:

    ---
    theme: temporal
    themeConfig:
      footer: "My Deck Title"
    ---

  Positioned via flex (margin-top: auto) rather than absolute so it never overlaps.
-->
<script setup lang="ts">
import { useNav } from '@slidev/client'
import { computed } from 'vue'

const { currentPage, total } = useNav()
const pageLabel = computed(() =>
  `${String(currentPage.value).padStart(2, '0')} / ${String(total.value).padStart(2, '0')}`
)
</script>

<template>
  <div class="temporal-footer">
    <span>{{ $slidev.themeConfigs?.footer ?? '' }}</span>
    <span class="page">{{ pageLabel }}</span>
  </div>
</template>

<style scoped>
.page {
  font-variant-numeric: tabular-nums;
  color: var(--temporal-green);
  opacity: 0.9;
}
</style>
