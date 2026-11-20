import { SKIES } from './data.js';

function hash(n) {
  const x = Math.sin(n * 127.1 + 311.7) * 43758.5453;
  return x - Math.floor(x);
}

function noise2(x, y) {
  const ix = Math.floor(x);
  const iy = Math.floor(y);
  const fx = x - ix;
  const fy = y - iy;
  const ux = fx * fx * (3 - 2 * fx);
  const uy = fy * fy * (3 - 2 * fy);
  const a = hash(ix + iy * 57);
  const b = hash(ix + 1 + iy * 57);
  const c = hash(ix + (iy + 1) * 57);
  const d = hash(ix + 1 + (iy + 1) * 57);
  return a + (b - a) * ux + (c - a) * uy + (a - b - c + d) * ux * uy;
}

function fbm(x, y, octaves = 5) {
  let v = 0;
  let a = 0.5;
  let f = 1;
  for (let i = 0; i < octaves; i++) {
    v += a * noise2(x * f, y * f);
    a *= 0.5;
    f *= 2.02;
  }
  return v;
}

function mix(a, b, t) {
  return a + (b - a) * t;
}

function rgb(c) {
  return [
    Math.max(0, Math.min(255, Math.round(c[0] * 255))),
    Math.max(0, Math.min(255, Math.round(c[1] * 255))),
    Math.max(0, Math.min(255, Math.round(c[2] * 255))),
  ];
}

function sampleSky(sky, u, v, t, aspect) {
  const p = sky.palette;
  const x = (u - 0.5) * aspect;
  const y = 1 - v;
  const d = y - 0.28;

  let col = [
    mix(p.top[0], p.horizon[0], Math.exp(-Math.max(d, 0) * 2.2)),
    mix(p.top[1], p.horizon[1], Math.exp(-Math.max(d, 0) * 2.2)),
    mix(p.top[2], p.horizon[2], Math.exp(-Math.max(d, 0) * 2.2)),
  ];

  const qx = x * 1.4 + t * 0.02;
  const qy = y * 1.2 - t * 0.015;
  const w1 = fbm(qx * 1.3, qy * 1.1, 4);
  const w2 = fbm(qx * 1.1 + 3.7, qy * 1.4 + 1.9, 4);
  const n = fbm(qx * 2.0 + w1 * 1.6, qy * 2.0 + w2 * 1.4, 5);

  let bandY = 0.42 - 0.18 * x + 0.05 * Math.sin(x * 3 + t * 0.4);
  if (p.fire) bandY = 0.5 + 0.12 * Math.sin(x * 2.2 + t * 0.8);
  if (p.rain || p.snow) bandY = 0.55 + 0.08 * Math.sin(x * 1.4);

  const band =
    Math.exp(-Math.pow((y - bandY) * 3.2, 2)) +
    0.35 * Math.exp(-Math.pow((y - 0.18 - 0.25 * x) * 4.5, 2));

  let neb = Math.max(0, Math.min(1, (n - 0.38) / 0.55)) * band;
  if (p.softClouds) neb = Math.max(0, Math.min(1, (n - 0.28) / 0.5)) * (0.55 + 0.45 * band);
  if (p.marble) neb = Math.abs(n - 0.5) * 1.6 * (0.4 + 0.6 * band);
  if (p.silver) neb *= 0.75;

  let nebCol = [
    mix(p.nebulaA[0], p.nebulaB[0], Math.min(1, n + 0.15 * w1)),
    mix(p.nebulaA[1], p.nebulaB[1], Math.min(1, n + 0.15 * w1)),
    mix(p.nebulaA[2], p.nebulaB[2], Math.min(1, n + 0.15 * w1)),
  ];

  if (p.iridescent) {
    const hue = (n + w1 * 0.3 + t * 0.05) % 1;
    nebCol = [
      mix(0.55, 0.35, hue) + 0.35 * Math.sin(hue * 6.28),
      mix(0.45, 0.85, hue),
      mix(0.9, 0.75, 1 - hue),
    ];
  }

  const above = Math.max(0, Math.min(1, (y - 0.12) / 0.7));
  col[0] += nebCol[0] * neb * 0.55 * above;
  col[1] += nebCol[1] * neb * 0.55 * above;
  col[2] += nebCol[2] * neb * 0.55 * above;

  if (p.aurora) {
    const ax = Math.sin(x * 4.5 + t * 0.6) * 0.5 + 0.5;
    const ay = Math.exp(-Math.pow((y - 0.35) * 4, 2));
    const aurora = ax * ay * 0.35;
    col[0] += 0.55 * aurora;
    col[1] += 0.18 * aurora;
    col[2] += 0.85 * aurora;
  }

  if (p.planet) {
    const px = 0.22;
    const py = 0.58;
    const pr = 0.18;
    const dx = (x - px) / (pr * aspect * 0.55);
    const dy = (y - py) / pr;
    const pd = dx * dx + dy * dy;
    if (pd < 1) {
      const rim = Math.exp((Math.sqrt(pd) - 1) * 3);
      col = [
        mix(0.02, 0.18, rim),
        mix(0.015, 0.08, rim),
        mix(0.04, 0.28, rim),
      ];
      const bands = fbm(dx * 3 + t * 0.05, dy * 4, 3);
      col[0] += bands * 0.08;
      col[2] += bands * 0.12;
    }
    // rings
    const rx = (x - px) / (pr * 1.9);
    const ry = (y - py) / (pr * 0.35);
    const rd = Math.abs(Math.sqrt(rx * rx + ry * ry) - 1);
    if (rd < 0.08 && Math.abs(dy) < 0.55) {
      const ring = 1 - rd / 0.08;
      col[0] = mix(col[0], 0.85, ring * 0.7);
      col[1] = mix(col[1], 0.7, ring * 0.7);
      col[2] = mix(col[2], 1.0, ring * 0.7);
    }
  }

  if (p.fire) {
    const flame = Math.max(0, n - 0.35) * band * 1.4;
    col[0] += flame * 0.5;
    col[1] += flame * 0.22;
    col[2] += flame * 0.02;
  }

  if (p.rain) {
    const streak = hash(Math.floor(u * 90) + Math.floor((v + t * 0.35) * 40) * 17);
    if (streak > 0.92) {
      col[0] += 0.12;
      col[1] += 0.14;
      col[2] += 0.18;
    }
  }

  if (p.snow) {
    const flake = hash(Math.floor(u * 60 + t * 8) * 13 + Math.floor(v * 40 - t * 12) * 29);
    if (flake > 0.97) {
      col = [mix(col[0], 1, 0.8), mix(col[1], 1, 0.8), mix(col[2], 1, 0.85)];
    }
  }

  // stars
  if (p.stars > 0 && y > 0.25) {
    const sx = Math.floor(u * 120);
    const sy = Math.floor(v * 70);
    const s = hash(sx * 19 + sy * 47);
    if (s > 1 - 0.012 * p.stars) {
      const tw = 0.55 + 0.45 * Math.sin(t * 3 + s * 20);
      const bright = (s - (1 - 0.012 * p.stars)) * 80 * tw;
      col[0] += bright * p.accent[0];
      col[1] += bright * p.accent[1];
      col[2] += bright * p.accent[2];
    }
  }

  // horizon glow
  const haze = Math.exp(-Math.max(d, 0) * 8);
  col[0] += p.nebulaB[0] * haze * 0.2;
  col[1] += p.nebulaB[1] * haze * 0.2;
  col[2] += p.nebulaB[2] * haze * 0.2;

  if (p.pale) {
    col = col.map((c) => Math.min(1, c * 1.15 + 0.08));
  }

  return rgb(col);
}

export function createSkyRenderer(canvas, { skyId = 'galaxy', quality = 0.45 } = {}) {
  const ctx = canvas.getContext('2d', { alpha: false });
  let sky = SKIES.find((s) => s.id === skyId) || SKIES[0];
  let raf = 0;
  let running = false;
  let start = performance.now();
  let buffer = null;
  let bw = 0;
  let bh = 0;

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = canvas.clientWidth || canvas.parentElement?.clientWidth || 800;
    const h = canvas.clientHeight || canvas.parentElement?.clientHeight || 450;
    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
    bw = Math.max(80, Math.floor(w * quality));
    bh = Math.max(45, Math.floor(h * quality));
    buffer = ctx.createImageData(bw, bh);
  }

  function frame(now) {
    if (!running) return;
    if (!buffer) resize();
    const t = (now - start) / 1000;
    const data = buffer.data;
    const aspect = bw / bh;
    for (let y = 0; y < bh; y++) {
      for (let x = 0; x < bw; x++) {
        const u = x / (bw - 1);
        const v = y / (bh - 1);
        const [r, g, b] = sampleSky(sky, u, v, t, aspect);
        const i = (y * bw + x) * 4;
        data[i] = r;
        data[i + 1] = g;
        data[i + 2] = b;
        data[i + 3] = 255;
      }
    }
    const off = document.createElement('canvas');
    off.width = bw;
    off.height = bh;
    off.getContext('2d').putImageData(buffer, 0, 0);
    ctx.imageSmoothingEnabled = true;
    ctx.drawImage(off, 0, 0, canvas.width, canvas.height);
    raf = requestAnimationFrame(frame);
  }

  const api = {
    setSky(id) {
      sky = SKIES.find((s) => s.id === id) || sky;
    },
    start() {
      if (running) return;
      running = true;
      resize();
      raf = requestAnimationFrame(frame);
    },
    stop() {
      running = false;
      cancelAnimationFrame(raf);
    },
    resize,
  };

  const ro = new ResizeObserver(() => resize());
  ro.observe(canvas);
  return api;
}

export function paintSkyThumb(canvas, skyId) {
  const sky = SKIES.find((s) => s.id === skyId) || SKIES[0];
  const w = canvas.width;
  const h = canvas.height;
  const ctx = canvas.getContext('2d');
  const img = ctx.createImageData(w, h);
  const aspect = w / h;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const [r, g, b] = sampleSky(sky, x / (w - 1), y / (h - 1), skyId.length * 0.7, aspect);
      const i = (y * w + x) * 4;
      img.data[i] = r;
      img.data[i + 1] = g;
      img.data[i + 2] = b;
      img.data[i + 3] = 255;
    }
  }
  ctx.putImageData(img, 0, 0);
}
