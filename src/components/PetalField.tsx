import { useMemo, type CSSProperties } from 'react'

interface PetalFieldProps {
  count?: number
  className?: string
}

export function PetalField({ count = 18, className }: PetalFieldProps) {
  const petals = useMemo(
    () =>
      Array.from({ length: count }, (_, i) => {
        const left = ((i * 37) % 100) + (i % 5)
        const delay = (i * 0.37) % 8
        const duration = 9 + (i % 6)
        const size = 8 + (i % 7)
        const drift = 20 + ((i * 13) % 70)
        return { left, delay, duration, size, drift, id: i }
      }),
    [count],
  )

  return (
    <div className={className} aria-hidden="true" style={styles.field}>
      {petals.map((p) => (
        <span
          key={p.id}
          style={{
            ...styles.petal,
            left: `${p.left}%`,
            width: p.size,
            height: p.size * 1.35,
            animationDelay: `${p.delay}s`,
            animationDuration: `${p.duration}s`,
            ['--drift' as string]: `${p.drift}px`,
            opacity: 0.55 + (p.id % 4) * 0.08,
          }}
        />
      ))}
    </div>
  )
}

const styles: Record<string, CSSProperties> = {
  field: {
    position: 'absolute',
    inset: 0,
    overflow: 'hidden',
    pointerEvents: 'none',
    zIndex: 2,
  },
  petal: {
    position: 'absolute',
    top: '-4%',
    borderRadius: '60% 40% 60% 40%',
    background:
      'radial-gradient(ellipse at 30% 30%, #ffe4ec, #ff9eb5 55%, #c45c74 100%)',
    animationName: 'petal-fall',
    animationTimingFunction: 'linear',
    animationIterationCount: 'infinite',
    filter: 'blur(0.2px)',
  },
}
