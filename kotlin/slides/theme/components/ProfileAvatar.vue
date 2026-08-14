<!--
  ABOUTME: Circular avatar for `profile` and `quote` layouts.
  ABOUTME: Falls back to a gradient placeholder when `src` is omitted (asset-blocked per spec §5.15).
-->
<script setup lang="ts">
withDefaults(defineProps<{
  src?: string
  alt?: string
  size?: 'sm' | 'md' | 'lg'
  initials?: string
}>(), {
  size: 'md',
  alt: '',
})
</script>

<template>
  <span class="avatar" :class="`avatar-${size}`">
    <img v-if="src" :src="src" :alt="alt" />
    <span v-else class="placeholder">{{ initials ?? '' }}</span>
  </span>
</template>

<style scoped>
.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.4), rgba(89, 253, 160, 0.25));
  border: 1px solid var(--temporal-border-hi);
  color: var(--temporal-text-strong);
  font-weight: 400;
  letter-spacing: 0.04em;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-sm { width: 2rem;   height: 2rem;   font-size: 0.75rem; }
.avatar-md { width: 3.5rem; height: 3.5rem; font-size: 1rem; }
.avatar-lg { width: 5rem;   height: 5rem;   font-size: 1.4rem; }
.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  user-select: none;
}
</style>
