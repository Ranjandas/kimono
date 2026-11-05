// Renders Kimono's banner and Play Store art from art.html with Playwright's Chromium.
// Run from the repo root: node scripts/store-art/render.js
const path = require('path');
const { chromium } = require('playwright');

const out = {
  banner: [640, 360, 'app/src/main/res/drawable-xhdpi/banner.png'],
  tv: [1280, 720, 'docs/store/tv-banner-1280x720.png'],
  icon: [512, 512, 'docs/store/icon-512.png'],
  feature: [1024, 500, 'docs/store/feature-graphic-1024x500.png'],
};

(async () => {
  const browser = await chromium.launch({ args: ['--allow-file-access-from-files'] });
  for (const [kind, [width, height, file]] of Object.entries(out)) {
    const page = await browser.newPage({ viewport: { width, height } });
    await page.goto(`file://${path.join(__dirname, 'art.html')}?k=${kind}`);
    await page.waitForTimeout(400);
    await page.screenshot({ path: file, clip: { x: 0, y: 0, width, height } });
  }
  await browser.close();
})();
