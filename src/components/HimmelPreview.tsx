import { useMemo, type CSSProperties } from 'react'
import {
  HIMMEL_PRESETS,
  type HimmelModel,
} from '../data/presets'

interface Props {
  model: HimmelModel
}

export function HimmelPreview({ model }: Props) {
  const preset = HIMMEL_PRESETS[model.preset]
  const [c0, c1, c2] = preset.sky

  const petals = useMemo(
    () =>
      Array.from({ length: Math.round(8 + model.density / 8) }, (_, i) => ({
        id: i,
        x: (i * 47) % 100,
        y: (i * 29) % 70,
        s: 0.6 + ((i * 3) % 5) * 0.15,
        delay: (i % 7) * 0.4,
      })),
    [model.density],
  )

  const stars = useMemo(
    () =>
      Array.from({ length: 28 }, (_, i) => ({
        id: i,
        x: (i * 37 + 11) % 100,
        y: (i * 53 + 7) % 55,
        r: 0.6 + (i % 3) * 0.4,
      })),
    [],
  )

  const hue = `hue-rotate(${model.hueShift}deg)`
  const glow = 0.25 + model.glow / 120

  return (
    <div
      className="himmel-preview"
      style={{
        ...styles.root,
        filter: hue,
        background: `linear-gradient(180deg, ${c0} 0%, ${c1} 48%, ${c2} 100%)`,
      }}
    >
      <div
        style={{
          ...styles.horizonGlow,
          opacity: glow,
          background: `radial-gradient(ellipse 80% 50% at 50% 100%, ${preset.accent}, transparent 70%)`,
        }}
      />

      {model.mist && (
        <div
          style={{
            ...styles.mist,
            opacity: 0.2 + model.density / 250,
            animationDuration: `${14 - model.drift / 12}s`,
          }}
        />
      )}

      {model.stars &&
        stars.map((s) => (
          <span
            key={s.id}
            style={{
              ...styles.star,
              left: `${s.x}%`,
              top: `${s.y}%`,
              width: s.r * 2,
              height: s.r * 2,
              animationDelay: `${s.id * 0.2}s`,
            }}
          />
        ))}

      {petals.map((p) => (
        <span
          key={p.id}
          style={{
            ...styles.petal,
            left: `${p.x}%`,
            top: `${p.y}%`,
            transform: `scale(${p.s})`,
            animationDuration: `${10 - model.drift / 15}s`,
            animationDelay: `${p.delay}s`,
            opacity: 0.35 + model.density / 200,
          }}
        />
      ))}

      <div style={styles.silhouette} />
    </div>
  )
}

const styles: Record<string, CSSProperties> = {
  root: {
    position: 'absolute',
    inset: 0,
    overflow: 'hidden',
  },
  horizonGlow: {
    position: 'absolute',
    inset: '40% 0 0',
    pointerEvents: 'none',
  },
  mist: {
    position: 'absolute',
    inset: '45% -20% -10%',
    background:
      'radial-gradient(ellipse at 30% 50%, rgba(240,230,216,0.25), transparent 55%), radial-gradient(ellipse at 70% 60%, rgba(255,158,181,0.18), transparent 50%)',
    filter: 'blur(18px)',
    animation: 'soft-pulse ease-in-out infinite',
  },
  star: {
    position: 'absolute',
    borderRadius: '50%',
    background: '#f0e6d8',
    boxShadow: '0 0 6px rgba(240,230,216,0.8)',
    animation: 'soft-pulse 3s ease-in-out infinite',
  },
  petal: {
    position: 'absolute',
    width: 14,
    height: 18,
    borderRadius: '60% 40% 55% 45%',
    background: 'linear-gradient(145deg, #ffd0dc, #ff9eb5 60%, #c45c74)',
    animationName: 'petal-fall',
    animationTimingFunction: 'linear',
    animationIterationCount: 'infinite',
  },
  silhouette: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: '22%',
    background:
      'linear-gradient(to top, rgba(8,4,6,0.85), transparent), radial-gradient(ellipse 50% 80% at 20% 100%, #1a1014 40%, transparent)',
  },
}
