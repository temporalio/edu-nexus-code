<!--
  ABOUTME: Code block alongside prose explanation. PPT slide 44 equivalent.
  ABOUTME: `code` named slot for the snippet; default slot for the prose.

  Frontmatter usage:
    ---
    layout: code-explain
    title: Durable Execution
    ---

    ::code::

    ```python
    def process_order(order):
        check_fraud(order.order_id, order.payment_info)
        ...
    ```

    ::default::

    **Durable Execution** is a new abstraction. It preserves full application
    state in the case of any host or software failure.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{ title?: string }>()
</script>

<template>
  <div class="slidev-layout code-explain bg-grid">
    <h2 v-if="title" class="title">{{ title }}</h2>
    <div class="ce-grid">
      <div class="code-pane"><slot name="code" /></div>
      <div class="prose-pane"><slot /></div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.code-explain {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  margin: 0 0 1rem;
  font-size: 2rem;
  font-weight: 200;
  color: var(--temporal-text-strong);
}
.ce-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 2rem;
  align-items: start;
  overflow: hidden;
}
.code-pane :deep(pre) {
  margin: 0;
}
.prose-pane :deep(p) {
  font-size: 1.05rem;
  line-height: 1.5;
  margin: 0.5rem 0;
}
</style>
