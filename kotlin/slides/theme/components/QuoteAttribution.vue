<!--
  ABOUTME: Quote source block: name + role/company. Used by `quote` and `profile` layouts.
  ABOUTME: Role line is omitted entirely when not provided.
-->
<script setup lang="ts">
defineProps<{
  name: string
  role?: string
}>()
</script>

<template>
  <div class="attribution">
    <p class="name">{{ name }}</p>
    <p v-if="role" class="role">{{ role }}</p>
  </div>
</template>

<style scoped>
.attribution {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}
.name {
  margin: 0;
  color: var(--temporal-text-strong);
  font-weight: 500;
  font-size: 1.05rem;
}
.role {
  margin: 0;
  color: var(--temporal-text-muted);
  font-size: 0.85rem;
  letter-spacing: 0.04em;
}
</style>
