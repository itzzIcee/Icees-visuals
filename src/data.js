/** Feature content for Icees Visuals — skies, cosmetics, effects. */

export const SKIES = [
  {
    id: 'galaxy',
    name: 'Galaxy',
    desc: 'Nebulae, aurora, shooting stars and a ringed planet.',
    glow: 'rgba(130, 40, 240, 0.24)',
    palette: {
      top: [0.018, 0.014, 0.036],
      horizon: [0.07, 0.035, 0.15],
      nebulaA: [0.27, 0.05, 0.8],
      nebulaB: [0.86, 0.24, 0.96],
      accent: [0.96, 0.84, 1.0],
      planet: true,
      aurora: true,
      stars: 1,
    },
  },
  {
    id: 'aether',
    name: 'Aether',
    desc: 'Silver light flowing over dark anthracite.',
    glow: 'rgba(170, 176, 196, 0.12)',
    palette: {
      top: [0.02, 0.022, 0.03],
      horizon: [0.12, 0.13, 0.16],
      nebulaA: [0.55, 0.58, 0.68],
      nebulaB: [0.85, 0.88, 0.95],
      accent: [0.95, 0.97, 1.0],
      planet: false,
      aurora: false,
      stars: 0.7,
      silver: true,
    },
  },
  {
    id: 'nocturne',
    name: 'Nocturne',
    desc: 'Soft violet clouds with a lilac glow at their edges.',
    glow: 'rgba(130, 60, 180, 0.2)',
    palette: {
      top: [0.04, 0.02, 0.08],
      horizon: [0.18, 0.08, 0.28],
      nebulaA: [0.45, 0.2, 0.7],
      nebulaB: [0.78, 0.55, 0.95],
      accent: [0.9, 0.75, 1.0],
      planet: false,
      aurora: false,
      stars: 0.55,
      softClouds: true,
    },
  },
  {
    id: 'amethyst',
    name: 'Amethyst',
    desc: 'Deep violet marble with fine veins of light.',
    glow: 'rgba(110, 40, 210, 0.22)',
    palette: {
      top: [0.05, 0.015, 0.1],
      horizon: [0.2, 0.05, 0.35],
      nebulaA: [0.35, 0.08, 0.65],
      nebulaB: [0.7, 0.3, 0.95],
      accent: [0.85, 0.55, 1.0],
      planet: false,
      aurora: false,
      stars: 0.4,
      marble: true,
    },
  },
  {
    id: 'opaline',
    name: 'Opaline',
    desc: 'Iridescent veils of lavender, pearl and cool cyan.',
    glow: 'rgba(90, 170, 190, 0.15)',
    palette: {
      top: [0.04, 0.03, 0.08],
      horizon: [0.15, 0.18, 0.28],
      nebulaA: [0.55, 0.4, 0.85],
      nebulaB: [0.4, 0.85, 0.9],
      accent: [0.95, 0.9, 1.0],
      planet: false,
      aurora: false,
      stars: 0.5,
      iridescent: true,
    },
  },
  {
    id: 'rain',
    name: 'Rain',
    desc: 'Heavy blue-grey clouds and distant veils of rain.',
    glow: 'rgba(120, 132, 156, 0.14)',
    palette: {
      top: [0.08, 0.09, 0.12],
      horizon: [0.22, 0.24, 0.3],
      nebulaA: [0.35, 0.4, 0.5],
      nebulaB: [0.55, 0.6, 0.7],
      accent: [0.75, 0.8, 0.9],
      planet: false,
      aurora: false,
      stars: 0.15,
      rain: true,
    },
  },
  {
    id: 'snow',
    name: 'Snow',
    desc: 'A pale winter sky with soft layers of cloud.',
    glow: 'rgba(200, 212, 235, 0.1)',
    palette: {
      top: [0.35, 0.4, 0.48],
      horizon: [0.72, 0.78, 0.88],
      nebulaA: [0.85, 0.88, 0.95],
      nebulaB: [0.95, 0.97, 1.0],
      accent: [1.0, 1.0, 1.0],
      planet: false,
      aurora: false,
      stars: 0.05,
      snow: true,
      pale: true,
    },
  },
  {
    id: 'fire',
    name: 'Fire',
    desc: 'A fairytale sky of moving flame clouds and dark smoke.',
    glow: 'rgba(240, 110, 40, 0.18)',
    palette: {
      top: [0.08, 0.02, 0.01],
      horizon: [0.45, 0.12, 0.04],
      nebulaA: [0.85, 0.25, 0.05],
      nebulaB: [1.0, 0.7, 0.2],
      accent: [1.0, 0.9, 0.5],
      planet: false,
      aurora: false,
      stars: 0.2,
      fire: true,
    },
  },
];

export const EFFECTS = [
  {
    id: 'weather',
    name: 'Weather',
    desc: 'Glassy rain with real puddles, snow that settles, and ember showers.',
  },
  {
    id: 'mist',
    name: 'Mist',
    desc: 'Soft mist that hangs low over the world, with light falling through it.',
  },
  {
    id: 'haze',
    name: 'Haze',
    desc: 'A soft haze of colour over the world, in eight palettes.',
  },
  {
    id: 'chroma',
    name: 'Chroma',
    desc: 'The world waits in grey, and colour blooms wherever you walk.',
  },
  {
    id: 'cyberspace',
    name: 'Cyberspace',
    desc: 'The world redrawn as a glowing network, with packets flying through it.',
  },
  {
    id: 'glass',
    name: 'Glass',
    desc: 'Players and what they hold, turned into clear, refracting glass.',
  },
  {
    id: 'bokeh',
    name: 'Bokeh',
    desc: 'Sharp where you look, soft discs of light behind.',
  },
  {
    id: 'shockwave',
    name: 'Shockwave',
    desc: 'Every landing sends a wave of light through the ground.',
  },
  {
    id: 'place',
    name: 'Place Animation',
    desc: 'Blocks grow into place as you build, with a soft glow.',
  },
  {
    id: 'ambience',
    name: 'Ambience',
    desc: 'Glowing particles drifting through the world around you.',
  },
];

export const COSMETICS = [
  {
    id: 'wings',
    name: 'Wings',
    desc: 'Five designs that fold, spread and move with you.',
    demo: 'wings',
  },
  {
    id: 'halo',
    name: 'Halo',
    desc: 'A softly glowing halo, in six designs.',
    demo: 'halo',
  },
  {
    id: 'aura',
    name: 'Aura',
    desc: 'Soft light that lives around you, in four styles.',
    demo: 'aura',
  },
  {
    id: 'capes',
    name: 'Capes',
    desc: 'Nine animated capes that sway like cloth.',
    demo: 'cape',
  },
  {
    id: 'skins3d',
    name: '3D Skins',
    desc: 'Your skin in real depth, and a wardrobe of new ones.',
    demo: 'skin',
  },
  {
    id: 'trail',
    name: 'Trail',
    desc: 'A ribbon of light behind you, in four styles.',
    demo: 'trail',
  },
  {
    id: 'jump',
    name: 'Jump Circles',
    desc: 'Soft rings of light where you jump and land.',
    demo: 'jump',
  },
  {
    id: 'knives',
    name: 'Knives',
    desc: 'Five knives and three blades, with 97 tricks that flow into each other.',
    demo: 'knife',
  },
  {
    id: 'hand',
    name: 'Hand Movement',
    desc: 'Your hand where you want it, with a swing that leaves a trail.',
    demo: 'hand',
  },
];

export const WING_STYLES = [
  { id: 'angel', name: 'Angel', color: '#f0e6ff' },
  { id: 'dragon', name: 'Dragon', color: '#ff6b4a' },
  { id: 'crystal', name: 'Crystal', color: '#9ad7ff' },
  { id: 'shadow', name: 'Shadow', color: '#6b5cff' },
  { id: 'phoenix', name: 'Phoenix', color: '#ffb347' },
];

export const HALO_STYLES = [
  { id: 'ring', name: 'Ring' },
  { id: 'orbit', name: 'Orbit' },
  { id: 'crown', name: 'Crown' },
  { id: 'pulse', name: 'Pulse' },
  { id: 'shard', name: 'Shard' },
  { id: 'nova', name: 'Nova' },
];

export const CAPE_STYLES = [
  { id: 'void', name: 'Void', colors: ['#2a1460', '#a855f7'] },
  { id: 'aurora', name: 'Aurora', colors: ['#0ea5e9', '#a78bfa'] },
  { id: 'ember', name: 'Ember', colors: ['#7c2d12', '#fb923c'] },
  { id: 'frost', name: 'Frost', colors: ['#0f172a', '#e0f2fe'] },
  { id: 'prism', name: 'Prism', colors: ['#581c87', '#f0abfc'] },
  { id: 'night', name: 'Night', colors: ['#111827', '#6366f1'] },
  { id: 'bloom', name: 'Bloom', colors: ['#831843', '#fb7185'] },
  { id: 'tide', name: 'Tide', colors: ['#164e63', '#67e8f9'] },
  { id: 'gold', name: 'Gold', colors: ['#422006', '#fbbf24'] },
];

export const KNIVES = [
  { id: 'butterfly', name: 'Butterfly', tricks: ['Open', 'Flip', 'Twirl', 'Inspect'] },
  { id: 'karambit', name: 'Karambit', tricks: ['Spin', 'Flick', 'Orbit', 'Inspect'] },
  { id: 'bayonet', name: 'Bayonet', tricks: ['Slash', 'Guard', 'Toss', 'Inspect'] },
  { id: 'stiletto', name: 'Stiletto', tricks: ['Draw', 'Point', 'Flip', 'Inspect'] },
  { id: 'ursus', name: 'Ursus', tricks: ['Snap', 'Roll', 'Catch', 'Inspect'] },
  { id: 'blade-a', name: 'Long Blade', tricks: ['Draw', 'Arc', 'Guard', 'Inspect'] },
  { id: 'blade-b', name: 'Short Blade', tricks: ['Quick', 'Cross', 'Flash', 'Inspect'] },
  { id: 'blade-c', name: 'Curved Blade', tricks: ['Sweep', 'Hook', 'Spin', 'Inspect'] },
];

export const FAQ = [
  {
    q: 'What do I need to play?',
    a: 'Minecraft: Java Edition with Fabric Loader and Fabric API. Your dashboard shows the exact versions when you download.',
  },
  {
    q: 'Do other players see my wings, cape or ride?',
    a: 'They are drawn on your own screen: you see them, and so does everyone who watches your clips and streams. Other players see the game as it always is.',
  },
  {
    q: 'Can I tune or turn off every effect?',
    a: 'Yes. Every effect has its own settings, and anything can be switched off. Many of them show a live preview on your own character while you tune them.',
  },
  {
    q: 'Does it work with Sodium and Iris?',
    a: 'Yes. Icees is tested with Sodium and with Iris shader packs such as BSL.',
  },
  {
    q: "What's the difference between Monthly and Lifetime?",
    a: 'Nothing inside the client. Monthly is €9.99 for 30 days and never renews by itself. Lifetime is €14.99 once, and it’s yours for good.',
  },
];
