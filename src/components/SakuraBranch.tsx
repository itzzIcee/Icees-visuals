export function SakuraBranch() {
  return (
    <svg
      className="hero-branch"
      viewBox="0 0 720 640"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <path
        d="M720 40C560 70 470 130 410 210C350 290 300 360 220 430C160 485 90 530 20 575"
        stroke="#3d2a34"
        strokeWidth="10"
        strokeLinecap="round"
      />
      <path
        d="M520 120C470 150 430 200 400 250C370 300 340 340 290 380"
        stroke="#2a1a22"
        strokeWidth="6"
        strokeLinecap="round"
      />
      <path
        d="M430 280C400 310 380 350 360 390C340 430 300 470 250 505"
        stroke="#2a1a22"
        strokeWidth="5"
        strokeLinecap="round"
      />
      {blossoms.map((b, i) => (
        <g key={i} transform={`translate(${b.x} ${b.y}) scale(${b.s})`} opacity={b.o}>
          <Blossom fill={b.fill} />
        </g>
      ))}
      <circle cx="480" cy="90" r="28" fill="url(#moonGlow)" opacity="0.55">
        <animate
          attributeName="opacity"
          values="0.4;0.7;0.4"
          dur="5s"
          repeatCount="indefinite"
        />
      </circle>
      <defs>
        <radialGradient id="moonGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#f0e6d8" />
          <stop offset="100%" stopColor="#f0e6d8" stopOpacity="0" />
        </radialGradient>
      </defs>
    </svg>
  )
}

function Blossom({ fill }: { fill: string }) {
  return (
    <>
      {[0, 72, 144, 216, 288].map((rot) => (
        <ellipse
          key={rot}
          cx="0"
          cy="-10"
          rx="5.5"
          ry="10"
          fill={fill}
          transform={`rotate(${rot})`}
        />
      ))}
      <circle cx="0" cy="0" r="3.2" fill="#f0e6d8" opacity="0.9" />
    </>
  )
}

const blossoms = [
  { x: 620, y: 70, s: 1.4, o: 0.95, fill: '#ff9eb5' },
  { x: 560, y: 110, s: 1.1, o: 0.85, fill: '#ffb7c5' },
  { x: 500, y: 150, s: 1.25, o: 0.9, fill: '#ff9eb5' },
  { x: 450, y: 200, s: 0.95, o: 0.75, fill: '#e87a92' },
  { x: 400, y: 250, s: 1.15, o: 0.88, fill: '#ffd0dc' },
  { x: 360, y: 300, s: 0.9, o: 0.7, fill: '#ff9eb5' },
  { x: 310, y: 350, s: 1.2, o: 0.85, fill: '#ffb7c5' },
  { x: 270, y: 400, s: 1, o: 0.8, fill: '#c45c74' },
  { x: 220, y: 450, s: 1.1, o: 0.75, fill: '#ff9eb5' },
  { x: 170, y: 500, s: 0.85, o: 0.65, fill: '#ffd0dc' },
  { x: 540, y: 180, s: 0.8, o: 0.7, fill: '#e87a92' },
  { x: 480, y: 240, s: 0.75, o: 0.6, fill: '#ffb7c5' },
]
