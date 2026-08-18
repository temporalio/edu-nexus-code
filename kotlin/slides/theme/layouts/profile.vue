<!--
  ABOUTME: Person/profile card. PPT slide 33 ("Ziggy Stardust / Job Title / Company").
  ABOUTME: Avatar on the left, name + role + company on the right.

  Frontmatter usage:
    ---
    layout: profile
    avatar: /path/to/photo.jpg     # optional; falls back to gradient placeholder
    initials: ZS                    # shown in placeholder if avatar absent
    name: Ziggy Stardust
    role: Job Title
    company: Company
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'
import ProfileAvatar from '../components/ProfileAvatar.vue'
import QuoteAttribution from '../components/QuoteAttribution.vue'

defineProps<{
  avatar?: string
  initials?: string
  name: string
  role?: string
  company?: string
}>()
</script>

<template>
  <div class="slidev-layout profile bg-grid">
    <div class="profile-inner">
      <ProfileAvatar :src="avatar" :initials="initials" size="lg" />
      <div class="profile-text">
        <p v-if="company" class="company">{{ company }}</p>
        <h1 class="name">{{ name }}</h1>
        <p v-if="role" class="role">{{ role }}</p>
        <div class="extra"><slot /></div>
      </div>
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.profile {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.profile-inner {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  gap: 2rem;
  min-height: 0;
}
.profile-text {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}
.company {
  margin: 0;
  font-size: 0.85rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--temporal-green);
}
.name {
  margin: 0;
  font-size: 2.6rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
}
.role {
  margin: 0;
  color: var(--temporal-lavender);
  font-size: 1.1rem;
}
.extra :deep(p) {
  margin-top: 0.8rem;
  color: var(--temporal-text);
}
</style>
