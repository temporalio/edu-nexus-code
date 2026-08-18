<!--
  ABOUTME: Single-action CTA banner. PPT slide 48 ("$6,000 in Cloud Credits") equivalent.
  ABOUTME: Big headline, optional subhead, prominent action link.

  Frontmatter usage:
    ---
    layout: cta-banner
    eyebrow: FOR STARTUPS
    headline: $6,000 in free Cloud Credits
    subhead: with less than 30 Million in funding
    cta: apply at temporal.io/startup
    href: https://temporal.io/startup
    ---
-->
<script setup lang="ts">
import TemporalFooter from '../components/TemporalFooter.vue'

defineProps<{
  eyebrow?: string
  headline: string
  subhead?: string
  cta?: string
  href?: string
}>()
</script>

<template>
  <div class="slidev-layout cta-banner bg-glow">
    <div class="banner-inner">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1 class="headline">{{ headline }}</h1>
      <p v-if="subhead" class="subhead">{{ subhead }}</p>
      <a v-if="cta" class="cta" :href="href ?? '#'">{{ cta }}</a>
      <slot />
    </div>
    <TemporalFooter />
  </div>
</template>

<style scoped>
.cta-banner {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.banner-inner {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 0.8rem;
  min-height: 0;
}
.eyebrow {
  margin: 0;
  font-size: 0.9rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--temporal-green);
  font-weight: 500;
}
.headline {
  margin: 0;
  font-size: 4rem;
  font-weight: 200;
  letter-spacing: -0.02em;
  color: var(--temporal-text-strong);
  line-height: 1.05;
  max-width: 22ch;
}
.subhead {
  margin: 0;
  color: var(--temporal-text);
  font-size: 1.25rem;
  font-weight: 300;
}
.cta {
  margin-top: 1rem;
  padding: 0.7rem 1.4rem;
  border: 1px solid var(--temporal-green);
  border-bottom-color: var(--temporal-green);
  border-radius: 999px;
  color: var(--temporal-green);
  background: rgba(89, 253, 160, 0.05);
  font-weight: 500;
  letter-spacing: 0.04em;
}
.cta:hover {
  background: rgba(89, 253, 160, 0.12);
}
</style>
