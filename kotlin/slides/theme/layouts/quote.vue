<!--
  ABOUTME: Pull-quote card. PPT slides 34–35.
  ABOUTME: Large quote body + attribution + optional avatar.

  Frontmatter usage:
    ---
    layout: quote
    name: Carl Sagan
    role: Astronomer
    avatar: /path/to/photo.jpg
    initials: CS
    ---

    The universe is a pretty big place. If it's just us, seems like an awful waste of space.
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import ProfileAvatar from '../components/ProfileAvatar.vue'
import QuoteAttribution from '../components/QuoteAttribution.vue'

defineProps<{
  name: string
  role?: string
  avatar?: string
  initials?: string
}>()
</script>

<template>
  <div class="slidev-layout quote bg-grid">
    <figure class="quote-inner">
      <blockquote class="quote-body">
        <span class="open-quote" aria-hidden="true">"</span>
        <slot />
      </blockquote>
      <figcaption class="quote-source">
        <ProfileAvatar :src="avatar" :initials="initials" size="md" />
        <QuoteAttribution :name="name" :role="role" />
      </figcaption>
    </figure>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.quote {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.quote-inner {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.5rem;
  margin: 0;
  min-height: 0;
}
.quote-body {
  margin: 0;
  font-size: 1.7rem;
  line-height: 1.35;
  font-weight: 200;
  color: var(--temporal-text-strong);
  border-left: 0;
  padding-left: 0;
  position: relative;
}
.open-quote {
  color: var(--temporal-green);
  font-size: 2.5rem;
  font-weight: 300;
  margin-right: 0.2rem;
  line-height: 0;
  position: relative;
  top: 0.2rem;
}
.quote-source {
  display: flex;
  align-items: center;
  gap: 1rem;
}
</style>
