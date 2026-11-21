import './style.css';
import {
  SKIES,
  EFFECTS,
  COSMETICS,
  WING_STYLES,
  HALO_STYLES,
  CAPE_STYLES,
  KNIVES,
  FAQ,
} from './data.js';
import { createSkyRenderer, paintSkyThumb } from './sky.js';
import { mountCosmeticDemo } from './cosmetics.js';

const app = document.querySelector('#app');

app.innerHTML = `
  <a class="skip" href="#main">Skip to content</a>

  <header class="nav" data-nav>
    <div class="wrap nav__inner">
      <a class="nav__brand" href="#top" aria-label="Icees, back to top">
        <svg class="nav__mark" viewBox="0 0 64 64" aria-hidden="true">
          <rect width="64" height="64" rx="16" fill="#0c0918"/>
          <path d="M32 10c8 10 14 18 14 28a14 14 0 1 1-28 0c0-10 6-18 14-28z" fill="url(#brand)"/>
          <defs>
            <linearGradient id="brand" x1="18" y1="10" x2="46" y2="54" gradientUnits="userSpaceOnUse">
              <stop stop-color="#eba2ff"/>
              <stop offset="1" stop-color="#5f04eb"/>
            </linearGradient>
          </defs>
        </svg>
        <span>Icees</span>
      </a>
      <nav class="nav__links" aria-label="Main">
        <a href="#visuals">Visuals</a>
        <a href="#skies">Skies</a>
        <a href="#cosmetics">Cosmetics</a>
        <a href="#pricing">Pricing</a>
      </nav>
      <div class="nav__actions">
        <a class="btn btn--primary btn--sm" href="#pricing">Get Icees</a>
        <button class="nav__toggle" type="button" aria-label="Open menu" aria-expanded="false" data-nav-toggle>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 7h16M4 12h16M4 17h16"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="wrap nav__sheet" data-nav-sheet hidden>
      <a href="#visuals">Visuals</a>
      <a href="#skies">Skies</a>
      <a href="#cosmetics">Cosmetics</a>
      <a href="#pricing">Pricing</a>
      <a href="#faq">FAQ</a>
    </div>
  </header>

  <main id="main">
    <section class="hero" id="top">
      <canvas class="hero__sky" data-hero-sky></canvas>
      <div class="hero__veil" aria-hidden="true"></div>
      <div class="wrap hero__content">
        <p class="hero__brand" data-reveal style="--i:0">Icees</p>
        <h1 data-reveal style="--i:1">
          <span class="line">Minecraft,</span><br />
          <span class="grad">in a different light.</span>
        </h1>
        <p class="hero__sub" data-reveal style="--i:2">
          Eight living skies, animated wings, halos and capes, and knife tricks that flow into each other —
          dressed in sakura pink. Fabric mod for 1.21.1.
        </p>
        <div class="hero__cta" data-reveal style="--i:3">
          <a class="btn btn--primary btn--lg" href="#pricing">Get Icees <span aria-hidden="true">→</span></a>
          <a class="btn btn--ghost btn--lg" href="#skies">See the skies</a>
        </div>
        <p class="hero__note" data-reveal style="--i:4">
          From <strong>€9.99</strong> a month, or <strong>€14.99</strong> once.
        </p>
      </div>
    </section>

    <section class="facts" aria-label="Icees in numbers">
      <div class="wrap facts__grid">
        <div class="fact" data-reveal style="--i:0"><b data-count="8">8</b><span>living skies</span></div>
        <div class="fact" data-reveal style="--i:1"><b data-count="5">5</b><span>wing designs</span></div>
        <div class="fact" data-reveal style="--i:2"><b data-count="9">9</b><span>animated capes</span></div>
        <div class="fact" data-reveal style="--i:3"><b data-count="97">97</b><span>knife tricks</span></div>
      </div>
    </section>

    <section class="effects" id="visuals">
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>Visuals</p>
          <h2 data-reveal style="--i:1">Effects you'll stop for.<br /><span class="grad">Alive in every frame.</span></h2>
          <p class="lead" data-reveal style="--i:2">
            Each effect has its own settings, and every one of them is yours to tune.
          </p>
        </div>
        <div class="effects__layout">
          <div class="effects__stage">
            <canvas data-effect-canvas></canvas>
            <div class="effects__caption">
              <strong data-effect-name>${EFFECTS[0].name}</strong>
              <span data-effect-desc>${EFFECTS[0].desc}</span>
            </div>
          </div>
          <ul class="effects__list" data-effect-list>
            ${EFFECTS.map(
              (fx, i) => `
              <li data-effect="${fx.id}" class="${i === 0 ? 'is-on' : ''}" tabindex="0" role="button">
                <b>${fx.name}</b>
                <span>${fx.desc}</span>
              </li>`
            ).join('')}
          </ul>
        </div>
        <p class="effects__more" data-reveal>
          Also in the menu: Afterglow, Radiance, Low Fire, Cute World, Block Outline, Custom Crosshair, Zoom, Freelook, Time, Name Tags and Spotify lyrics that stand in the world.
        </p>
      </div>
    </section>

    <section class="skies" id="skies" data-skies>
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>Skies</p>
          <h2 data-reveal style="--i:1">Eight skies.<br /><span class="grad">Every one of them alive.</span></h2>
          <p class="lead" data-reveal style="--i:2">
            Drawn per pixel, every frame. No skybox images, no seams, nothing stretched.
          </p>
        </div>
        <div class="viewer" data-viewer>
          <div class="viewer__stage">
            <canvas data-sky-canvas></canvas>
            <div class="viewer__meta" data-sky-meta>
              <strong data-sky-name>${SKIES[0].name}</strong>
              <span data-sky-desc>${SKIES[0].desc}</span>
            </div>
          </div>
          <div class="viewer__tabs" role="tablist" aria-label="Sky styles" data-sky-tabs></div>
        </div>
      </div>
    </section>

    <section class="cosmetics" id="cosmetics">
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>Cosmetics</p>
          <h2 data-reveal style="--i:1">Look the part.<br /><span class="grad">In every frame.</span></h2>
          <p class="lead" data-reveal style="--i:2">
            A wardrobe of wings, halos, capes and skins — plus knife animations with 97 tricks that flow into each other.
          </p>
        </div>
        <div class="cosmetics__grid" data-cosmetics-grid>
          ${COSMETICS.map(
            (c) => `
            <article class="card" data-card="${c.demo}">
              <div class="card__media">
                <canvas data-demo="${c.demo}"></canvas>
                <span class="card__badge" data-demo-label="${c.demo}">Live</span>
              </div>
              <div class="card__text">
                <h3>${c.name}</h3>
                <p>${c.desc}</p>
              </div>
            </article>`
          ).join('')}
        </div>

        <div class="featured">
          <div class="featured__panel">
            <canvas data-featured-canvas></canvas>
          </div>
          <div class="featured__copy">
            <p class="eyebrow">Featured</p>
            <h3 data-featured-title>Wings · Angel</h3>
            <p class="lead" style="margin:0" data-featured-desc>
              Pick a wardrobe piece and watch it move — fold, sway, glow, flip.
            </p>
            <div>
              <p style="margin:0 0 0.45rem;color:var(--muted);font-size:0.85rem">Wings</p>
              <div class="style-chips" data-wing-chips>
                ${WING_STYLES.map((s, i) => `<button type="button" data-wing="${i}" class="${i === 0 ? 'is-on' : ''}">${s.name}</button>`).join('')}
              </div>
            </div>
            <div>
              <p style="margin:0 0 0.45rem;color:var(--muted);font-size:0.85rem">Halos</p>
              <div class="style-chips" data-halo-chips>
                ${HALO_STYLES.map((s, i) => `<button type="button" data-halo="${i}" class="${i === 0 ? 'is-on' : ''}">${s.name}</button>`).join('')}
              </div>
            </div>
            <div>
              <p style="margin:0 0 0.45rem;color:var(--muted);font-size:0.85rem">Capes</p>
              <div class="style-chips" data-cape-chips>
                ${CAPE_STYLES.map((s, i) => `<button type="button" data-cape="${i}" class="${i === 0 ? 'is-on' : ''}">${s.name}</button>`).join('')}
              </div>
            </div>
            <div>
              <p style="margin:0 0 0.45rem;color:var(--muted);font-size:0.85rem">Knives</p>
              <div class="style-chips" data-knife-chips>
                ${KNIVES.map((s, i) => `<button type="button" data-knife="${i}" class="${i === 0 ? 'is-on' : ''}">${s.name}</button>`).join('')}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="menu">
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>Interface</p>
          <h2 data-reveal style="--i:1">One calm menu.<br /><span class="grad">Everything in it.</span></h2>
          <p class="lead" data-reveal style="--i:2">
            Every menu, HUD element and effect runs on our own renderer — clean, fast, yours to arrange.
          </p>
        </div>
        <div class="menu-grid">
          <div class="menu-item" data-reveal style="--i:0">
            <strong>Right Shift opens Orbit</strong>
            <span>Every module in one place, with search and a live preview of many effects.</span>
          </div>
          <div class="menu-item" data-reveal style="--i:1">
            <strong>A HUD you arrange yourself</strong>
            <span>Drag every element where you want it, right in the game.</span>
          </div>
          <div class="menu-item" data-reveal style="--i:2">
            <strong>One accent colour</strong>
            <span>Pick a colour, and the menu, the HUD and the effects follow.</span>
          </div>
          <div class="menu-item" data-reveal style="--i:3">
            <strong>Configs you can share</strong>
            <span>Save your setups, share one with a five-digit code, or try what others made.</span>
          </div>
        </div>
      </div>
    </section>

    <section id="pricing">
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>Pricing</p>
          <h2 data-reveal style="--i:1">One client.<br /><span class="grad">Two ways to get it.</span></h2>
          <p class="lead" data-reveal style="--i:2">Same modules, same updates. Pick what suits you.</p>
        </div>
        <div class="pricing__grid">
          <article class="price" data-reveal style="--i:0">
            <h3>Monthly</h3>
            <p class="price__amount">€9.99<small>/ 30 days</small></p>
            <ul>
              <li>All eight skies and every effect</li>
              <li>Wings, halos, auras, capes and 3D skins</li>
              <li>Five knives, three blades, 97 tricks</li>
              <li>Every update while it runs</li>
            </ul>
            <a class="btn btn--ghost" href="#pricing">Choose Monthly</a>
          </article>
          <article class="price price--hot" data-reveal style="--i:1">
            <span class="price__tag">Best value</span>
            <h3>Lifetime</h3>
            <p class="price__amount">€14.99<small> once</small></p>
            <ul>
              <li>All eight skies and every effect</li>
              <li>Wings, halos, auras, capes and 3D skins</li>
              <li>Five knives, three blades, 97 tricks</li>
              <li>Every future update</li>
            </ul>
            <a class="btn btn--primary" href="#pricing">Choose Lifetime</a>
          </article>
        </div>
        <p class="pricing__note">Works with Sodium and Iris. Needs Fabric Loader and Fabric API.</p>
      </div>
    </section>

    <section class="faq" id="faq">
      <div class="wrap">
        <div class="section__head">
          <p class="eyebrow" data-reveal>FAQ</p>
          <h2 data-reveal style="--i:1">Good to know.</h2>
        </div>
        ${FAQ.map(
          (item) => `
          <details>
            <summary>${item.q}</summary>
            <p>${item.a}</p>
          </details>`
        ).join('')}
      </div>
    </section>

    <section class="closing">
      <div class="closing__bg" aria-hidden="true"><canvas data-closing-sky></canvas></div>
      <div class="closing__veil" aria-hidden="true"></div>
      <div class="wrap closing__content">
        <h2>Ready for a different light?</h2>
        <p>Eight skies. A full wardrobe. Animations that never sit still.</p>
        <a class="btn btn--primary btn--lg" href="#pricing">Get Icees</a>
      </div>
    </section>
  </main>

  <footer class="footer">
    <div class="wrap footer__inner">
      <span>© ${new Date().getFullYear()} Icees Visuals. Not affiliated with Mojang or Microsoft.</span>
      <nav>
        <a href="#skies">Skies</a>
        <a href="#cosmetics">Cosmetics</a>
        <a href="#pricing">Pricing</a>
      </nav>
    </div>
  </footer>
`;

function initNav() {
  const nav = document.querySelector('[data-nav]');
  const toggle = document.querySelector('[data-nav-toggle]');
  const sheet = document.querySelector('[data-nav-sheet]');

  const onScroll = () => nav.classList.toggle('is-scrolled', window.scrollY > 12);
  onScroll();
  window.addEventListener('scroll', onScroll, { passive: true });

  toggle?.addEventListener('click', () => {
    const open = sheet.classList.toggle('is-open');
    sheet.hidden = !open;
    toggle.setAttribute('aria-expanded', String(open));
  });

  sheet?.querySelectorAll('a').forEach((a) =>
    a.addEventListener('click', () => {
      sheet.classList.remove('is-open');
      sheet.hidden = true;
      toggle.setAttribute('aria-expanded', 'false');
    })
  );
}

function initReveal() {
  const nodes = [...document.querySelectorAll('[data-reveal]')];
  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-in');
          io.unobserve(entry.target);
        }
      }
    },
    { threshold: 0.12, rootMargin: '0px 0px -8% 0px' }
  );
  nodes.forEach((n) => io.observe(n));
}

function initCounters() {
  const nodes = [...document.querySelectorAll('[data-count]')];
  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue;
        const el = entry.target;
        const target = Number(el.dataset.count);
        const start = performance.now();
        const dur = 900;
        const tick = (now) => {
          const p = Math.min(1, (now - start) / dur);
          const eased = 1 - Math.pow(1 - p, 3);
          el.textContent = String(Math.round(target * eased));
          if (p < 1) requestAnimationFrame(tick);
        };
        requestAnimationFrame(tick);
        io.unobserve(el);
      }
    },
    { threshold: 0.6 }
  );
  nodes.forEach((n) => io.observe(n));
}

function initHeroSky() {
  const canvas = document.querySelector('[data-hero-sky]');
  if (!canvas) return;
  const renderer = createSkyRenderer(canvas, { skyId: 'galaxy', quality: 0.38 });
  renderer.start();

  // gentle auto-cycle through skies in the hero
  let i = 0;
  setInterval(() => {
    i = (i + 1) % SKIES.length;
    renderer.setSky(SKIES[i].id);
  }, 7000);
}

function initSkiesViewer() {
  const canvas = document.querySelector('[data-sky-canvas]');
  const tabs = document.querySelector('[data-sky-tabs]');
  const nameEl = document.querySelector('[data-sky-name]');
  const descEl = document.querySelector('[data-sky-desc]');
  const meta = document.querySelector('[data-sky-meta]');
  const section = document.querySelector('[data-skies]');
  if (!canvas || !tabs) return;

  const renderer = createSkyRenderer(canvas, { skyId: SKIES[0].id, quality: 0.5 });
  let active = 0;

  tabs.innerHTML = SKIES.map(
    (sky, i) => `
    <button class="sky-tab" type="button" role="tab" aria-selected="${i === 0}" data-sky-tab="${sky.id}" aria-label="${sky.name} sky">
      <canvas width="120" height="68" data-thumb="${sky.id}"></canvas>
      <span>${sky.name}</span>
    </button>`
  ).join('');

  tabs.querySelectorAll('[data-thumb]').forEach((c) => paintSkyThumb(c, c.dataset.thumb));

  const select = (index) => {
    active = index;
    const sky = SKIES[index];
    meta?.classList.add('is-changing');
    section?.style.setProperty('--sky-glow', sky.glow);
    renderer.setSky(sky.id);
    tabs.querySelectorAll('[data-sky-tab]').forEach((btn, i) => {
      btn.setAttribute('aria-selected', String(i === index));
    });
    window.setTimeout(() => {
      if (nameEl) nameEl.textContent = sky.name;
      if (descEl) descEl.textContent = sky.desc;
      meta?.classList.remove('is-changing');
    }, 160);
  };

  tabs.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-sky-tab]');
    if (!btn) return;
    const index = SKIES.findIndex((s) => s.id === btn.dataset.skyTab);
    if (index >= 0) select(index);
  });

  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) renderer.start();
        else renderer.stop();
      }
    },
    { threshold: 0.15 }
  );
  io.observe(canvas);

  // autoplay when visible
  setInterval(() => {
    if (document.hidden) return;
    const rect = canvas.getBoundingClientRect();
    if (rect.bottom < 0 || rect.top > window.innerHeight) return;
    select((active + 1) % SKIES.length);
  }, 5200);
}

function paintEffectPreview(canvas, effectId, t) {
  const ctx = canvas.getContext('2d');
  const dpr = Math.min(window.devicePixelRatio || 1, 2);
  const w = canvas.clientWidth || 640;
  const h = canvas.clientHeight || 360;
  if (canvas.width !== Math.floor(w * dpr) || canvas.height !== Math.floor(h * dpr)) {
    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
  }
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  const g = ctx.createLinearGradient(0, 0, 0, h);
  g.addColorStop(0, '#1a1033');
  g.addColorStop(1, '#0a0714');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, w, h);

  // ground plane
  ctx.fillStyle = '#14101f';
  ctx.fillRect(0, h * 0.62, w, h * 0.38);
  ctx.strokeStyle = 'rgba(192,132,252,0.15)';
  for (let i = 0; i < 8; i++) {
    const y = h * 0.62 + i * 18;
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(w, y);
    ctx.stroke();
  }

  const cx = w * 0.5;
  const cy = h * 0.55;

  if (effectId === 'weather') {
    for (let i = 0; i < 60; i++) {
      const x = ((i * 47 + t * 80) % w);
      const y = ((i * 73 + t * 180) % h);
      ctx.strokeStyle = 'rgba(180,200,255,0.45)';
      ctx.beginPath();
      ctx.moveTo(x, y);
      ctx.lineTo(x - 2, y + 14);
      ctx.stroke();
    }
  } else if (effectId === 'mist' || effectId === 'haze') {
    for (let i = 0; i < 5; i++) {
      const rg = ctx.createRadialGradient(cx + Math.sin(t + i) * 80, cy, 10, cx, cy, 180 + i * 30);
      rg.addColorStop(0, `rgba(192,132,252,${0.18 - i * 0.02})`);
      rg.addColorStop(1, 'transparent');
      ctx.fillStyle = rg;
      ctx.fillRect(0, 0, w, h);
    }
  } else if (effectId === 'chroma') {
    ctx.fillStyle = 'rgba(255,255,255,0.04)';
    ctx.fillRect(0, 0, w, h);
    const rg = ctx.createRadialGradient(cx, cy, 20, cx, cy, 160);
    rg.addColorStop(0, 'rgba(250,180,255,0.55)');
    rg.addColorStop(0.5, 'rgba(120,180,255,0.2)');
    rg.addColorStop(1, 'transparent');
    ctx.fillStyle = rg;
    ctx.fillRect(0, 0, w, h);
  } else if (effectId === 'cyberspace') {
    ctx.strokeStyle = 'rgba(120,220,255,0.35)';
    for (let i = -10; i < 20; i++) {
      ctx.beginPath();
      ctx.moveTo(i * 40 + (t * 30) % 40, h * 0.62);
      ctx.lineTo(cx, h * 0.2);
      ctx.stroke();
    }
    for (let i = 0; i < 20; i++) {
      const x = ((i * 53 + t * 40) % w);
      const y = h * 0.2 + ((i * 17) % (h * 0.4));
      ctx.fillStyle = '#67e8f9';
      ctx.fillRect(x, y, 3, 3);
    }
  } else if (effectId === 'glass') {
    ctx.fillStyle = 'rgba(180,220,255,0.12)';
    ctx.beginPath();
    ctx.roundRect(cx - 40, cy - 70, 80, 120, 12);
    ctx.fill();
    ctx.strokeStyle = 'rgba(220,240,255,0.55)';
    ctx.stroke();
  } else if (effectId === 'bokeh') {
    for (let i = 0; i < 18; i++) {
      const x = (Math.sin(i * 12.1) * 0.5 + 0.5) * w;
      const y = (Math.cos(i * 7.7) * 0.5 + 0.5) * h * 0.7;
      ctx.beginPath();
      ctx.fillStyle = `rgba(235,162,255,${0.15 + (i % 5) * 0.05})`;
      ctx.arc(x, y, 10 + (i % 6) * 4, 0, Math.PI * 2);
      ctx.fill();
    }
  } else if (effectId === 'shockwave') {
    const p = (t % 1.8) / 1.8;
    for (let i = 0; i < 3; i++) {
      const pp = (p + i / 3) % 1;
      ctx.strokeStyle = `rgba(250,232,255,${1 - pp})`;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.ellipse(cx, cy + 40, 20 + pp * 120, 8 + pp * 28, 0, 0, Math.PI * 2);
      ctx.stroke();
    }
  } else if (effectId === 'place') {
    const s = 18 + Math.sin(t * 3) * 6;
    ctx.fillStyle = 'rgba(167,139,250,0.55)';
    ctx.fillRect(cx - s / 2, cy - s / 2, s, s);
    ctx.shadowColor = '#c084fc';
    ctx.shadowBlur = 20;
    ctx.strokeStyle = '#f5d0fe';
    ctx.strokeRect(cx - s / 2, cy - s / 2, s, s);
    ctx.shadowBlur = 0;
  } else {
    // ambience
    for (let i = 0; i < 30; i++) {
      const x = ((i * 37 + Math.sin(t + i) * 20) % w);
      const y = ((i * 59 + t * 25) % h);
      ctx.fillStyle = `rgba(233,213,255,${0.25 + (i % 4) * 0.1})`;
      ctx.beginPath();
      ctx.arc(x, y, 2, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  // tiny player marker
  ctx.fillStyle = '#c4b5fd';
  ctx.fillRect(cx - 6, cy - 18, 12, 28);
}

function initEffects() {
  const canvas = document.querySelector('[data-effect-canvas]');
  const list = document.querySelector('[data-effect-list]');
  const nameEl = document.querySelector('[data-effect-name]');
  const descEl = document.querySelector('[data-effect-desc]');
  if (!canvas || !list) return;

  let active = EFFECTS[0].id;
  let raf = 0;
  let running = false;
  const start = performance.now();

  const frame = (now) => {
    if (!running) return;
    paintEffectPreview(canvas, active, (now - start) / 1000);
    raf = requestAnimationFrame(frame);
  };

  const setEffect = (id) => {
    active = id;
    const fx = EFFECTS.find((e) => e.id === id) || EFFECTS[0];
    nameEl.textContent = fx.name;
    descEl.textContent = fx.desc;
    list.querySelectorAll('[data-effect]').forEach((li) => {
      li.classList.toggle('is-on', li.dataset.effect === id);
    });
  };

  list.addEventListener('click', (e) => {
    const li = e.target.closest('[data-effect]');
    if (li) setEffect(li.dataset.effect);
  });

  list.addEventListener('keydown', (e) => {
    if (e.key !== 'Enter' && e.key !== ' ') return;
    const li = e.target.closest('[data-effect]');
    if (!li) return;
    e.preventDefault();
    setEffect(li.dataset.effect);
  });

  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          running = true;
          raf = requestAnimationFrame(frame);
        } else {
          running = false;
          cancelAnimationFrame(raf);
        }
      }
    },
    { threshold: 0.1 }
  );
  io.observe(canvas);
}

function initCosmetics() {
  const demos = [];
  document.querySelectorAll('canvas[data-demo]').forEach((canvas) => {
    const label = document.querySelector(`[data-demo-label="${canvas.dataset.demo}"]`);
    const demo = mountCosmeticDemo(canvas, label, { demo: canvas.dataset.demo });
    demos.push({ canvas, demo });
  });

  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        const item = demos.find((d) => d.canvas === entry.target);
        if (!item) continue;
        if (entry.isIntersecting) item.demo.start();
        else item.demo.stop();
      }
    },
    { threshold: 0.2 }
  );
  demos.forEach(({ canvas }) => io.observe(canvas));

  // featured interactive panel
  const featured = document.querySelector('[data-featured-canvas]');
  const title = document.querySelector('[data-featured-title]');
  const desc = document.querySelector('[data-featured-desc]');
  if (!featured) return;

  const featuredDemo = mountCosmeticDemo(featured, null, { demo: 'wings', autoCycle: false });
  featuredDemo.setStyle('wing', 0);
  const featuredIo = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) featuredDemo.start();
        else featuredDemo.stop();
      }
    },
    { threshold: 0.15 }
  );
  featuredIo.observe(featured);

  const setFeatured = (type, index) => {
    const map = {
      wing: { demo: 'wings', list: WING_STYLES, label: 'Wing', chip: '[data-wing-chips]', key: 'data-wing' },
      halo: { demo: 'halo', list: HALO_STYLES, label: 'Halo', chip: '[data-halo-chips]', key: 'data-halo' },
      cape: { demo: 'cape', list: CAPE_STYLES, label: 'Cape', chip: '[data-cape-chips]', key: 'data-cape' },
      knife: { demo: 'knife', list: KNIVES, label: 'Knife', chip: '[data-knife-chips]', key: 'data-knife' },
    };
    const cfg = map[type];
    featuredDemo.setDemo(cfg.demo);
    featuredDemo.setStyle(type, index);
    const item = cfg.list[index];
    title.textContent = `${cfg.label} · ${item.name}`;
    desc.textContent =
      type === 'knife'
        ? `${item.name}: ${item.tricks.join(' → ')}`
        : `Live preview of the ${item.name} ${cfg.label.toLowerCase()}.`;

    document.querySelectorAll('.style-chips button').forEach((b) => b.classList.remove('is-on'));
    document.querySelector(`${cfg.chip} [${cfg.key}="${index}"]`)?.classList.add('is-on');
  };

  document.querySelector('[data-wing-chips]')?.addEventListener('click', (e) => {
    const b = e.target.closest('[data-wing]');
    if (b) setFeatured('wing', Number(b.dataset.wing));
  });
  document.querySelector('[data-halo-chips]')?.addEventListener('click', (e) => {
    const b = e.target.closest('[data-halo]');
    if (b) setFeatured('halo', Number(b.dataset.halo));
  });
  document.querySelector('[data-cape-chips]')?.addEventListener('click', (e) => {
    const b = e.target.closest('[data-cape]');
    if (b) setFeatured('cape', Number(b.dataset.cape));
  });
  document.querySelector('[data-knife-chips]')?.addEventListener('click', (e) => {
    const b = e.target.closest('[data-knife]');
    if (b) setFeatured('knife', Number(b.dataset.knife));
  });
}

function initClosingSky() {
  const canvas = document.querySelector('[data-closing-sky]');
  if (!canvas) return;
  const renderer = createSkyRenderer(canvas, { skyId: 'amethyst', quality: 0.28 });
  const io = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) renderer.start();
        else renderer.stop();
      }
    },
    { threshold: 0.1 }
  );
  io.observe(canvas);
}

initNav();
initReveal();
initCounters();
initHeroSky();
initSkiesViewer();
initEffects();
initCosmetics();
initClosingSky();
