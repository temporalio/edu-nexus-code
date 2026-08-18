<!--
  ABOUTME: Multi-column CTA with icon-led blocks. PPT slide 47 ("Join the Conversation").
  ABOUTME: Asset-blocked on the icon set per spec §5.11 — icons render as `.asset-pending` placeholders.

  Frontmatter usage:
    ---
    layout: cta-icons
    title: Join the Conversation
    columns: 3
    items:
      - title: Ask questions
        body: Ask in Zoom Q&A — we'll answer live or async.
        action: Zoom Q&A
      - title: Slack community
        body: Ask questions in #events-live.
        action: t.mp/slack
        href: https://t.mp/slack
      - title: Newsletter
        body: Monthly community newsletter.
        action: temporal.io/community
        href: https://temporal.io/community
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

withDefaults(defineProps<{
  title?: string
  columns?: 2 | 3 | 4
  items: Array<{
    title: string
    body: string
    action?: string
    href?: string
    icon?: string
  }>
}>(), {
  columns: 3,
})
</script>

<template>
  <div class="slidev-layout cta-icons bg-grid">
    <h1 v-if="title" class="title">{{ title }}</h1>
    <div class="cta-grid" :style="{ gridTemplateColumns: `repeat(${columns}, 1fr)` }">
      <section v-for="(item, i) in items" :key="i" class="cta-cell">
        <div class="icon-slot">
          <img v-if="item.icon" :src="item.icon" :alt="item.title" />
          <div v-else class="asset-pending">Icon · spec §5.11</div>
        </div>
        <h3>{{ item.title }}</h3>
        <p class="body">{{ item.body }}</p>
        <a v-if="item.action" class="action" :href="item.href ?? '#'">{{ item.action }}</a>
      </section>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.cta-icons {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 1.3rem;
}
.title {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.cta-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  gap: 1.5rem;
}
.cta-cell {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  align-items: flex-start;
}
.icon-slot {
  width: 100%;
  min-height: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-slot img {
  max-height: 4rem;
}
.icon-slot .asset-pending {
  width: 100%;
  min-height: 4rem;
  font-size: 0.75rem;
}
.cta-cell h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--temporal-text-strong);
}
.body {
  margin: 0;
  color: var(--temporal-text);
  font-size: 0.95rem;
  line-height: 1.45;
}
.action {
  color: var(--temporal-green);
  border-bottom: 1px solid rgba(89, 253, 160, 0.35);
  font-weight: 500;
  font-size: 0.95rem;
  margin-top: auto;
}
</style>
