import { type CSSProperties } from 'react'
import { WAFFEN_PRESETS, type WaffenModel } from '../data/presets'

interface Props {
  model: WaffenModel
}

export function WaffenPreview({ model }: Props) {
  const preset = WAFFEN_PRESETS[model.preset]
  const scale = 0.75 + model.scale / 200
  const metalLight = Math.round(140 + model.metal)
  const metal = `rgb(${metalLight}, ${metalLight - 12}, ${metalLight - 28})`
  const glowOpacity = 0.25 + model.edgeGlow / 120
  const bloomOpacity = model.bloom / 140
  const trailOpacity = model.trail / 130

  return (
    <div style={styles.root}>
      <div style={styles.floor} />
      <div
        style={{
          ...styles.bloom,
          opacity: bloomOpacity,
          background: `radial-gradient(circle, ${preset.glow}, transparent 65%)`,
        }}
      />

      {model.enchanted && (
        <div
          style={{
            ...styles.trail,
            opacity: trailOpacity,
            background: `linear-gradient(90deg, transparent, ${preset.glow}, transparent)`,
          }}
        />
      )}

      <svg
        viewBox="0 0 240 320"
        width="240"
        height="320"
        style={{
          ...styles.weapon,
          transform: `scale(${scale})`,
          filter: `drop-shadow(0 0 ${8 + model.edgeGlow / 8}px ${preset.glow})`,
        }}
        aria-hidden="true"
      >
        {preset.silhouette === 'blade' && (
          <Blade metal={metal} glow={preset.glow} glowOpacity={glowOpacity} />
        )}
        {preset.silhouette === 'bow' && (
          <Bow metal={metal} glow={preset.glow} glowOpacity={glowOpacity} />
        )}
        {preset.silhouette === 'sickle' && (
          <Sickle metal={metal} glow={preset.glow} glowOpacity={glowOpacity} />
        )}
        {preset.silhouette === 'staff' && (
          <Staff metal={metal} glow={preset.glow} glowOpacity={glowOpacity} />
        )}
      </svg>

      {model.enchanted && (
        <div style={{ ...styles.rune, color: preset.glow, opacity: 0.5 + model.edgeGlow / 200 }}>
          ✦
        </div>
      )}
    </div>
  )
}

function Blade({
  metal,
  glow,
  glowOpacity,
}: {
  metal: string
  glow: string
  glowOpacity: number
}) {
  return (
    <g>
      <path d="M118 40 L128 200 L118 210 L108 200 Z" fill={metal} />
      <path
        d="M118 40 L124 200 L118 206 L112 200 Z"
        fill={glow}
        opacity={glowOpacity}
      />
      <rect x="108" y="200" width="20" height="14" rx="2" fill="#5a4038" />
      <rect x="114" y="214" width="8" height="55" rx="2" fill="#3d2a24" />
      <circle cx="118" cy="198" r="7" fill={glow} opacity={0.55} />
      <ellipse cx="118" cy="70" rx="4" ry="7" fill="#ffd0dc" opacity="0.55" />
      <ellipse cx="118" cy="110" rx="3.5" ry="6" fill="#ff9eb5" opacity="0.45" />
    </g>
  )
}

function Bow({
  metal,
  glow,
  glowOpacity,
}: {
  metal: string
  glow: string
  glowOpacity: number
}) {
  return (
    <g>
      <path
        d="M90 50 C40 120, 40 200, 90 270"
        stroke={metal}
        strokeWidth="8"
        fill="none"
        strokeLinecap="round"
      />
      <path
        d="M90 50 C50 120, 50 200, 90 270"
        stroke={glow}
        strokeWidth="3"
        fill="none"
        opacity={glowOpacity}
      />
      <line x1="90" y1="50" x2="90" y2="270" stroke="#f0e6d8" strokeWidth="1.5" opacity="0.7" />
      <circle cx="90" cy="160" r="5" fill={glow} opacity="0.7" />
    </g>
  )
}

function Sickle({
  metal,
  glow,
  glowOpacity,
}: {
  metal: string
  glow: string
  glowOpacity: number
}) {
  return (
    <g>
      <path
        d="M140 70 C190 90, 195 180, 130 210 C160 160, 155 100, 140 70 Z"
        fill={metal}
      />
      <path
        d="M145 80 C180 100, 182 170, 140 200"
        stroke={glow}
        strokeWidth="4"
        fill="none"
        opacity={glowOpacity}
      />
      <rect x="114" y="200" width="10" height="70" rx="3" fill="#3d2a24" />
      <circle cx="119" cy="198" r="8" fill={glow} opacity="0.5" />
    </g>
  )
}

function Staff({
  metal,
  glow,
  glowOpacity,
}: {
  metal: string
  glow: string
  glowOpacity: number
}) {
  return (
    <g>
      <rect x="114" y="90" width="10" height="190" rx="3" fill={metal} />
      <circle cx="119" cy="70" r="22" fill={glow} opacity={glowOpacity + 0.2} />
      <circle cx="119" cy="70" r="12" fill="#f0e6d8" opacity="0.85" />
      <path
        d="M105 120 L119 100 L133 120"
        stroke={glow}
        strokeWidth="3"
        fill="none"
        opacity="0.7"
      />
      <circle cx="119" cy="150" r="4" fill="#ff9eb5" />
      <circle cx="119" cy="190" r="3.5" fill="#c45c74" />
    </g>
  )
}

const styles: Record<string, CSSProperties> = {
  root: {
    position: 'absolute',
    inset: 0,
    display: 'grid',
    placeItems: 'center',
    background:
      'radial-gradient(ellipse 60% 50% at 50% 45%, #2a1824 0%, #12090e 70%)',
    overflow: 'hidden',
  },
  floor: {
    position: 'absolute',
    left: '10%',
    right: '10%',
    bottom: '12%',
    height: '18%',
    background:
      'radial-gradient(ellipse at center, rgba(255,158,181,0.12), transparent 70%)',
    filter: 'blur(8px)',
  },
  bloom: {
    position: 'absolute',
    width: '55%',
    height: '55%',
    borderRadius: '50%',
    filter: 'blur(40px)',
    animation: 'soft-pulse 4s ease-in-out infinite',
  },
  trail: {
    position: 'absolute',
    width: '70%',
    height: 4,
    top: '48%',
    filter: 'blur(6px)',
    animation: 'shimmer 2.8s linear infinite',
    backgroundSize: '200% 100%',
  },
  weapon: {
    position: 'relative',
    zIndex: 2,
    transition: 'transform 0.35s cubic-bezier(0.22, 1, 0.36, 1)',
  },
  rune: {
    position: 'absolute',
    bottom: '18%',
    fontSize: '1.4rem',
    animation: 'soft-pulse 2.4s ease-in-out infinite',
  },
}
