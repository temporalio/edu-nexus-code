// Checks every code-stack slide for overflow after the layout's auto-fit settles.
// Walks the whole deck rather than hardcoding slide numbers, so it survives
// slides being added or removed.
//
// Run with the dev server up:  node scripts/probe-fit.mjs
import { chromium } from "playwright-chromium";

const BASE = process.env.SLIDEV_URL || "http://localhost:3030";

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1600, height: 900 } });

// Slide 1 renders the deck's total in the footer as "01 / N".
await page.goto(`${BASE}/1`, { waitUntil: "networkidle" });
await page.waitForTimeout(800);
const total = await page.evaluate(() => {
  const m = document.body.innerText.match(/\b\d{1,3}\s*\/\s*(\d{1,3})\b/);
  return m ? Number(m[1]) : 0;
});
if (!total) {
  console.error("Could not read the slide count. Is the dev server up?");
  process.exit(2);
}

let checked = 0;
let bad = 0;

for (let n = 1; n <= total; n++) {
  await page.goto(`${BASE}/${n}`, { waitUntil: "networkidle" });
  await page.waitForTimeout(900);

  const r = await page.evaluate(() => {
    const visible = (sel) =>
      [...document.querySelectorAll(sel)].find((e) => e.clientHeight > 0);
    const code = visible(".code-pane");
    if (!code) return null; // not a code-stack slide
    const prose = visible(".prose-pane");
    const stat = (el) =>
      el
        ? { over: el.scrollHeight - el.clientHeight, h: el.clientHeight }
        : null;
    return {
      heading: visible(".heading")?.textContent?.trim() ?? "(no heading)",
      codeSize: code.style.getPropertyValue("--cs-code-size"),
      proseSize: prose?.style.getPropertyValue("--cs-prose-size"),
      code: stat(code),
      prose: stat(prose),
    };
  });

  if (!r) continue;
  checked++;

  const codeOver = r.code.over > 1;
  const proseOver = r.prose ? r.prose.over > 1 : false;
  if (codeOver || proseOver) bad++;

  console.log(
    `${codeOver || proseOver ? "CLIPPED" : "ok     "} ${String(n).padStart(2)} ` +
      `${r.heading.padEnd(14)} code ${r.codeSize} h=${r.code.h} over=${r.code.over}  ` +
      `prose ${r.proseSize} h=${r.prose?.h} over=${r.prose?.over}`,
  );
}

console.log(
  bad === 0
    ? `\nAll ${checked} code-stack slides fit.`
    : `\n${bad} of ${checked} code-stack slides clipped.`,
);
await browser.close();
process.exit(bad === 0 ? 0 : 1);
