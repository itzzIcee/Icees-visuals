import { WING_STYLES, HALO_STYLES, CAPE_STYLES, KNIVES } from './data.js';

function clear(ctx, w, h) {
  const g = ctx.createLinearGradient(0, 0, 0, h);
  g.addColorStop(0, '#1a1030');
  g.addColorStop(0.55, '#120c22');
  g.addColorStop(1, '#0a0714');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, w, h);

  // soft ground glow
  const glow = ctx.createRadialGradient(w * 0.5, h * 0.78, 10, w * 0.5, h * 0.78, w * 0.45);
  glow.addColorStop(0, 'rgba(181, 31, 244, 0.18)');
  glow.addColorStop(1, 'rgba(0,0,0,0)');
  ctx.fillStyle = glow;
  ctx.fillRect(0, 0, w, h);
}

function drawFigure(ctx, cx, cy, scale = 1) {
  ctx.save();
  ctx.translate(cx, cy);
  ctx.scale(scale, scale);
  ctx.fillStyle = '#1e1633';
  ctx.strokeStyle = '#c4b5fd';
  ctx.lineWidth = 2;

  // legs
  ctx.beginPath();
  ctx.moveTo(-10, 28);
  ctx.lineTo(-12, 58);
  ctx.moveTo(10, 28);
  ctx.lineTo(12, 58);
  ctx.stroke();

  // torso
  ctx.beginPath();
  ctx.roundRect(-16, -8, 32, 40, 8);
  ctx.fill();
  ctx.stroke();

  // arms
  ctx.beginPath();
  ctx.moveTo(-16, 4);
  ctx.lineTo(-28, 26);
  ctx.moveTo(16, 4);
  ctx.lineTo(28, 26);
  ctx.stroke();

  // head
  ctx.beginPath();
  ctx.arc(0, -24, 14, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();
  ctx.restore();
}

function drawWings(ctx, cx, cy, t, style) {
  const beat = Math.sin(t * 2.4) * 0.22;
  const color = style.color;
  for (const side of [-1, 1]) {
    ctx.save();
    ctx.translate(cx + side * 14, cy - 2);
    ctx.rotate(side * (-0.55 + beat));
    const grad = ctx.createLinearGradient(0, -40, side * 90, 40);
    grad.addColorStop(0, color);
    grad.addColorStop(0.5, `${color}cc`);
    grad.addColorStop(1, 'transparent');
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.bezierCurveTo(side * 20, -30, side * 70, -50, side * 95, -10);
    ctx.bezierCurveTo(side * 70, 10, side * 45, 35, side * 10, 25);
    ctx.closePath();
    ctx.fill();

    // feather lines
    ctx.strokeStyle = 'rgba(255,255,255,0.28)';
    ctx.lineWidth = 1;
    for (let i = 0; i < 5; i++) {
      ctx.beginPath();
      ctx.moveTo(side * 8, 4 + i * 4);
      ctx.quadraticCurveTo(side * (40 + i * 8), -10 + i * 6, side * (70 + i * 4), -5 + i * 8);
      ctx.stroke();
    }
    ctx.restore();
  }
}

function drawHalo(ctx, cx, cy, t, styleIndex) {
  const style = HALO_STYLES[styleIndex % HALO_STYLES.length];
  ctx.save();
  ctx.translate(cx, cy - 48);
  ctx.shadowColor = 'rgba(235, 162, 255, 0.85)';
  ctx.shadowBlur = 18;

  if (style.id === 'ring' || style.id === 'pulse') {
    const pulse = style.id === 'pulse' ? 1 + Math.sin(t * 3) * 0.08 : 1;
    ctx.strokeStyle = '#f5d0fe';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.ellipse(0, 0, 28 * pulse, 10 * pulse, 0, 0, Math.PI * 2);
    ctx.stroke();
  } else if (style.id === 'orbit') {
    for (let i = 0; i < 3; i++) {
      const a = t * 1.5 + (i * Math.PI * 2) / 3;
      ctx.fillStyle = '#e9d5ff';
      ctx.beginPath();
      ctx.arc(Math.cos(a) * 30, Math.sin(a) * 10, 3.5, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.strokeStyle = 'rgba(233,213,255,0.45)';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.ellipse(0, 0, 30, 10, 0, 0, Math.PI * 2);
    ctx.stroke();
  } else if (style.id === 'crown') {
    ctx.fillStyle = '#f5d0fe';
    ctx.beginPath();
    for (let i = 0; i < 6; i++) {
      const a = -Math.PI / 2 + (i * Math.PI * 2) / 6;
      const r = i % 2 === 0 ? 22 : 12;
      const x = Math.cos(a) * r;
      const y = Math.sin(a) * r * 0.45;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.closePath();
    ctx.fill();
  } else if (style.id === 'shard') {
    ctx.rotate(t * 0.4);
    ctx.strokeStyle = '#ddd6fe';
    ctx.lineWidth = 2;
    for (let i = 0; i < 8; i++) {
      const a = (i * Math.PI * 2) / 8;
      ctx.beginPath();
      ctx.moveTo(Math.cos(a) * 10, Math.sin(a) * 4);
      ctx.lineTo(Math.cos(a) * 28, Math.sin(a) * 10);
      ctx.stroke();
    }
  } else {
    // nova
    const rays = 12;
    for (let i = 0; i < rays; i++) {
      const a = (i / rays) * Math.PI * 2 + t;
      const len = 18 + Math.sin(t * 4 + i) * 6;
      ctx.strokeStyle = `rgba(250, 232, 255, ${0.35 + 0.35 * Math.sin(t * 3 + i)})`;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(0, 0);
      ctx.lineTo(Math.cos(a) * len, Math.sin(a) * len * 0.4);
      ctx.stroke();
    }
  }
  ctx.restore();
}

function drawCape(ctx, cx, cy, t, style) {
  const [c1, c2] = style.colors;
  ctx.save();
  ctx.translate(cx, cy - 4);
  const sway = Math.sin(t * 1.8) * 10;
  const sway2 = Math.sin(t * 2.3 + 1) * 8;
  const grad = ctx.createLinearGradient(0, 0, 0, 90);
  grad.addColorStop(0, c2);
  grad.addColorStop(1, c1);
  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.moveTo(-18, 0);
  ctx.lineTo(18, 0);
  ctx.bezierCurveTo(22, 30, 20 + sway, 60, 12 + sway, 88);
  ctx.quadraticCurveTo(0, 78 + Math.sin(t * 2) * 4, -12 + sway2, 88);
  ctx.bezierCurveTo(-20 + sway2, 60, -22, 30, -18, 0);
  ctx.closePath();
  ctx.fill();

  // cloth folds
  ctx.strokeStyle = 'rgba(255,255,255,0.18)';
  ctx.lineWidth = 1.2;
  for (let i = -1; i <= 1; i++) {
    ctx.beginPath();
    ctx.moveTo(i * 8, 8);
    ctx.bezierCurveTo(i * 10 + sway * 0.2, 35, i * 8 + sway * 0.4, 60, i * 6 + sway * 0.5, 82);
    ctx.stroke();
  }
  ctx.restore();
}

function drawAura(ctx, cx, cy, t) {
  for (let i = 0; i < 3; i++) {
    const r = 36 + i * 14 + Math.sin(t * 2 + i) * 4;
    ctx.strokeStyle = `rgba(192, 132, 252, ${0.28 - i * 0.07})`;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.ellipse(cx, cy + 8, r * 0.7, r, 0, 0, Math.PI * 2);
    ctx.stroke();
  }
  for (let i = 0; i < 18; i++) {
    const a = t * 0.8 + (i / 18) * Math.PI * 2;
    const r = 40 + Math.sin(t * 3 + i) * 8;
    ctx.fillStyle = 'rgba(233, 213, 255, 0.55)';
    ctx.beginPath();
    ctx.arc(cx + Math.cos(a) * r * 0.55, cy + Math.sin(a) * r, 2, 0, Math.PI * 2);
    ctx.fill();
  }
}

function drawTrail(ctx, cx, cy, t) {
  for (let i = 0; i < 12; i++) {
    const p = i / 12;
    const x = cx - 20 - p * 90;
    const y = cy + 10 + Math.sin(t * 3 - p * 4) * 12;
    ctx.fillStyle = `rgba(192, 132, 252, ${0.55 * (1 - p)})`;
    ctx.beginPath();
    ctx.ellipse(x, y, 10 * (1 - p * 0.5), 4, -0.4, 0, Math.PI * 2);
    ctx.fill();
  }
}

function drawJump(ctx, cx, cy, t) {
  const phase = (t % 2) / 2;
  for (let i = 0; i < 3; i++) {
    const p = (phase + i / 3) % 1;
    ctx.strokeStyle = `rgba(250, 232, 255, ${1 - p})`;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.ellipse(cx, cy + 58, 18 + p * 50, 6 + p * 10, 0, 0, Math.PI * 2);
    ctx.stroke();
  }
}

function drawKnife(ctx, cx, cy, t, knifeIndex) {
  const knife = KNIVES[knifeIndex % KNIVES.length];
  const trickPhase = Math.floor(t * 0.55) % knife.tricks.length;
  const local = (t * 0.55) % 1;
  const spin = (() => {
    switch (knife.tricks[trickPhase]) {
      case 'Flip':
      case 'Twirl':
      case 'Spin':
      case 'Orbit':
      case 'Roll':
        return local * Math.PI * 2;
      case 'Toss':
      case 'Flash':
        return Math.sin(local * Math.PI) * Math.PI;
      case 'Open':
        return local * 0.8;
      default:
        return Math.sin(local * Math.PI * 2) * 0.35;
    }
  })();

  const lift = knife.tricks[trickPhase] === 'Toss' ? Math.sin(local * Math.PI) * 40 : 0;

  ctx.save();
  ctx.translate(cx + 36, cy + 10 - lift);
  ctx.rotate(-0.6 + spin);

  // handle
  const isButterfly = knife.id === 'butterfly';
  ctx.fillStyle = '#2e1065';
  ctx.strokeStyle = '#e9d5ff';
  ctx.lineWidth = 1.5;

  if (isButterfly) {
    const open = 0.4 + Math.abs(Math.sin(t * 2)) * 0.9;
    ctx.save();
    ctx.rotate(-open);
    ctx.beginPath();
    ctx.roundRect(-6, -4, 12, 34, 3);
    ctx.fill();
    ctx.stroke();
    ctx.restore();
    ctx.save();
    ctx.rotate(open);
    ctx.beginPath();
    ctx.roundRect(-6, -4, 12, 34, 3);
    ctx.fill();
    ctx.stroke();
    ctx.restore();
  } else {
    ctx.beginPath();
    ctx.roundRect(-5, 0, 10, 28, 3);
    ctx.fill();
    ctx.stroke();
  }

  // blade
  const bladeGrad = ctx.createLinearGradient(0, 0, 0, -70);
  bladeGrad.addColorStop(0, '#c4b5fd');
  bladeGrad.addColorStop(1, '#f5d0fe');
  ctx.fillStyle = bladeGrad;
  ctx.beginPath();
  if (knife.id === 'karambit') {
    ctx.moveTo(0, 0);
    ctx.bezierCurveTo(28, -10, 36, -40, 8, -58);
    ctx.bezierCurveTo(4, -30, 2, -12, 0, 0);
  } else if (knife.id.includes('blade') || knife.id === 'bayonet') {
    ctx.moveTo(-4, 0);
    ctx.lineTo(4, 0);
    ctx.lineTo(3, -62);
    ctx.lineTo(0, -72);
    ctx.lineTo(-3, -62);
    ctx.closePath();
  } else {
    ctx.moveTo(-3, 0);
    ctx.lineTo(5, 0);
    ctx.lineTo(4, -48);
    ctx.lineTo(0, -58);
    ctx.lineTo(-4, -48);
    ctx.closePath();
  }
  ctx.fill();
  ctx.stroke();

  // motion trail
  ctx.globalAlpha = 0.35;
  ctx.strokeStyle = '#f0abfc';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(0, -20, 34, spin - 1.2, spin);
  ctx.stroke();
  ctx.restore();

  // arm
  ctx.strokeStyle = '#c4b5fd';
  ctx.lineWidth = 2.5;
  ctx.beginPath();
  ctx.moveTo(cx + 16, cy + 4);
  ctx.quadraticCurveTo(cx + 28, cy + 16, cx + 36, cy + 10 - lift * 0.3);
  ctx.stroke();

  return knife.tricks[trickPhase];
}

function drawSkin(ctx, cx, cy, t) {
  drawFigure(ctx, cx, cy, 1.05);
  ctx.save();
  ctx.translate(cx, cy);
  ctx.strokeStyle = `rgba(233, 213, 255, ${0.4 + Math.sin(t * 2) * 0.2})`;
  ctx.lineWidth = 2;
  ctx.strokeRect(-22, -40, 44, 100);
  ctx.restore();
}

function drawHand(ctx, cx, cy, t) {
  drawFigure(ctx, cx - 10, cy, 0.95);
  const swing = Math.sin(t * 4) * 0.8;
  ctx.save();
  ctx.translate(cx + 24, cy + 8);
  ctx.rotate(-0.4 + swing);
  ctx.fillStyle = '#ddd6fe';
  ctx.beginPath();
  ctx.roundRect(-4, -8, 10, 22, 4);
  ctx.fill();
  // trail
  for (let i = 0; i < 6; i++) {
    ctx.globalAlpha = 0.25 - i * 0.03;
    ctx.beginPath();
    ctx.arc(-10 - i * 8, Math.sin(t * 4 - i) * 6, 4, 0, Math.PI * 2);
    ctx.fill();
  }
  ctx.restore();
}

const DEMO_DRAW = {
  wings: (ctx, w, h, t, state) => {
    clear(ctx, w, h);
    const style = WING_STYLES[state.wing % WING_STYLES.length];
    drawWings(ctx, w * 0.5, h * 0.48, t, style);
    drawFigure(ctx, w * 0.5, h * 0.48, 1);
    return style.name;
  },
  halo: (ctx, w, h, t, state) => {
    clear(ctx, w, h);
    drawFigure(ctx, w * 0.5, h * 0.52, 1);
    drawHalo(ctx, w * 0.5, h * 0.52, t, state.halo);
    return HALO_STYLES[state.halo % HALO_STYLES.length].name;
  },
  aura: (ctx, w, h, t) => {
    clear(ctx, w, h);
    drawAura(ctx, w * 0.5, h * 0.5, t);
    drawFigure(ctx, w * 0.5, h * 0.5, 1);
    return 'Soft Aura';
  },
  cape: (ctx, w, h, t, state) => {
    clear(ctx, w, h);
    const style = CAPE_STYLES[state.cape % CAPE_STYLES.length];
    drawCape(ctx, w * 0.5, h * 0.46, t, style);
    drawFigure(ctx, w * 0.5, h * 0.46, 1);
    return style.name;
  },
  skin: (ctx, w, h, t) => {
    clear(ctx, w, h);
    drawSkin(ctx, w * 0.5, h * 0.48, t);
    return 'Depth';
  },
  trail: (ctx, w, h, t) => {
    clear(ctx, w, h);
    drawTrail(ctx, w * 0.58, h * 0.5, t);
    drawFigure(ctx, w * 0.58, h * 0.5, 1);
    return 'Ribbon';
  },
  jump: (ctx, w, h, t) => {
    clear(ctx, w, h);
    drawFigure(ctx, w * 0.5, h * 0.45, 1);
    drawJump(ctx, w * 0.5, h * 0.45, t);
    return 'Rings';
  },
  knife: (ctx, w, h, t, state) => {
    clear(ctx, w, h);
    drawFigure(ctx, w * 0.42, h * 0.5, 1);
    const trick = drawKnife(ctx, w * 0.42, h * 0.5, t, state.knife);
    return `${KNIVES[state.knife % KNIVES.length].name} · ${trick}`;
  },
  hand: (ctx, w, h, t) => {
    clear(ctx, w, h);
    drawHand(ctx, w * 0.5, h * 0.5, t);
    return 'Swing trail';
  },
};

export function mountCosmeticDemo(canvas, labelEl, { demo = 'wings', autoCycle = true } = {}) {
  const ctx = canvas.getContext('2d');
  const state = { wing: 0, halo: 0, cape: 0, knife: 0, demo, locked: false };
  let raf = 0;
  let running = false;
  let start = performance.now();

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = canvas.clientWidth || 400;
    const h = canvas.clientHeight || 260;
    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function frame(now) {
    if (!running) return;
    const t = (now - start) / 1000;
    if (autoCycle && !state.locked) {
      state.wing = Math.floor(t / 3) % WING_STYLES.length;
      state.halo = Math.floor(t / 2.5) % HALO_STYLES.length;
      state.cape = Math.floor(t / 2.8) % CAPE_STYLES.length;
      state.knife = Math.floor(t / 4.5) % KNIVES.length;
    }

    const w = canvas.clientWidth || 400;
    const h = canvas.clientHeight || 260;
    const draw = DEMO_DRAW[state.demo] || DEMO_DRAW.wings;
    const label = draw(ctx, w, h, t, state);
    if (labelEl) labelEl.textContent = label;
    raf = requestAnimationFrame(frame);
  }

  const api = {
    setDemo(id) {
      state.demo = id;
      start = performance.now();
    },
    setStyle(type, index) {
      state.locked = true;
      if (type === 'wing') state.wing = index % WING_STYLES.length;
      if (type === 'halo') state.halo = index % HALO_STYLES.length;
      if (type === 'cape') state.cape = index % CAPE_STYLES.length;
      if (type === 'knife') state.knife = index % KNIVES.length;
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

  new ResizeObserver(() => resize()).observe(canvas);
  return api;
}
