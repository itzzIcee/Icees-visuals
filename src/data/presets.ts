export type HimmelPresetId =
  | 'dämmerung'
  | 'nachtblüte'
  | 'nebelmond'
  | 'rosenhimmel'
  | 'sternregen'

export type WaffenPresetId =
  | 'kirschklinge'
  | 'blütenbogen'
  | 'petalschwert'
  | 'mondsichel'
  | 'dornenstab'

export interface HimmelModel {
  id: string
  name: string
  preset: HimmelPresetId
  density: number
  glow: number
  drift: number
  hueShift: number
  stars: boolean
  mist: boolean
}

export interface WaffenModel {
  id: string
  name: string
  preset: WaffenPresetId
  metal: number
  edgeGlow: number
  bloom: number
  trail: number
  scale: number
  enchanted: boolean
}

export const HIMMEL_PRESETS: Record<
  HimmelPresetId,
  { label: string; hint: string; sky: [string, string, string]; accent: string }
> = {
  dämmerung: {
    label: 'Dämmerung',
    hint: 'Warmes Abendlicht, weiche Horizontlinie',
    sky: ['#1a0f18', '#3d2438', '#c45c74'],
    accent: '#ff9eb5',
  },
  nachtblüte: {
    label: 'Nachtblüte',
    hint: 'Tiefe Nacht mit leuchtenden Petals',
    sky: ['#0c080c', '#241018', '#6a3048'],
    accent: '#ffb7c5',
  },
  nebelmond: {
    label: 'Nebelmond',
    hint: 'Silbriger Mondnebel über dem Horizont',
    sky: ['#12151c', '#2a3040', '#8a9bb0'],
    accent: '#f0e6d8',
  },
  rosenhimmel: {
    label: 'Rosenhimmel',
    hint: 'Satte Rosenwolken, lebendige Atmosphäre',
    sky: ['#1c0e14', '#5c2038', '#e87a92'],
    accent: '#ffc2d1',
  },
  sternregen: {
    label: 'Sternregen',
    hint: 'Fallende Sterne durch Kirschzweige',
    sky: ['#100a10', '#2a1820', '#8a5060'],
    accent: '#e8d4a8',
  },
}

export const WAFFEN_PRESETS: Record<
  WaffenPresetId,
  { label: string; hint: string; silhouette: 'blade' | 'bow' | 'staff' | 'sickle'; metal: string; glow: string }
> = {
  kirschklinge: {
    label: 'Kirschklinge',
    hint: 'Schlanke Klinge mit Petal-Ätzung',
    silhouette: 'blade',
    metal: '#c8b8a8',
    glow: '#ff9eb5',
  },
  blütenbogen: {
    label: 'Blütenbogen',
    hint: 'Geschwungener Bogen, weiche Sehne',
    silhouette: 'bow',
    metal: '#a89078',
    glow: '#ffb7c5',
  },
  petalschwert: {
    label: 'Petalschwert',
    hint: 'Breites Schwert, blühende Parierstange',
    silhouette: 'blade',
    metal: '#d4c4b0',
    glow: '#e87a92',
  },
  mondsichel: {
    label: 'Mondsichel',
    hint: 'Gebogene Sichel im Mondlicht',
    silhouette: 'sickle',
    metal: '#b8c0c8',
    glow: '#f0e6d8',
  },
  dornenstab: {
    label: 'Dornenstab',
    hint: 'Stab mit Dornen und Blütenknoten',
    silhouette: 'staff',
    metal: '#8a7060',
    glow: '#c45c74',
  },
}

export function createHimmel(partial?: Partial<HimmelModel>): HimmelModel {
  return {
    id: crypto.randomUUID(),
    name: 'Neuer Himmel',
    preset: 'dämmerung',
    density: 55,
    glow: 40,
    drift: 35,
    hueShift: 0,
    stars: true,
    mist: true,
    ...partial,
  }
}

export function createWaffe(partial?: Partial<WaffenModel>): WaffenModel {
  return {
    id: crypto.randomUUID(),
    name: 'Neue Waffe',
    preset: 'kirschklinge',
    metal: 60,
    edgeGlow: 45,
    bloom: 30,
    trail: 25,
    scale: 50,
    enchanted: true,
    ...partial,
  }
}
