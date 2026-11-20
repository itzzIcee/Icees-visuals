import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { HimmelEditor } from '../components/HimmelEditor'
import { WaffenEditor } from '../components/WaffenEditor'
import {
  createHimmel,
  createWaffe,
  type HimmelModel,
  type WaffenModel,
} from '../data/presets'

type Tool = 'himmel' | 'waffen'

function isTool(value: string | undefined): value is Tool {
  return value === 'himmel' || value === 'waffen'
}

const HIMMEL_KEY = 'sakura.himmel.library'
const WAFFEN_KEY = 'sakura.waffen.library'

function loadLibrary<T>(key: string, fallback: T[]): T[] {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    const parsed = JSON.parse(raw) as T[]
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : fallback
  } catch {
    return fallback
  }
}

export function Studio() {
  const { tool: toolParam } = useParams()
  const navigate = useNavigate()
  const tool: Tool = isTool(toolParam) ? toolParam : 'himmel'

  const initialHimmel = useMemo(
    () => [
      createHimmel({ name: 'Abendblüte', preset: 'dämmerung' }),
      createHimmel({ name: 'Silent Grove', preset: 'nachtblüte', density: 70 }),
    ],
    [],
  )
  const initialWaffen = useMemo(
    () => [
      createWaffe({ name: 'Sakura Edge', preset: 'kirschklinge' }),
      createWaffe({ name: 'Mondlicht', preset: 'mondsichel', edgeGlow: 70 }),
    ],
    [],
  )

  const [himmelLib, setHimmelLib] = useState<HimmelModel[]>(() =>
    loadLibrary(HIMMEL_KEY, initialHimmel),
  )
  const [waffenLib, setWaffenLib] = useState<WaffenModel[]>(() =>
    loadLibrary(WAFFEN_KEY, initialWaffen),
  )
  const [activeHimmelId, setActiveHimmelId] = useState(himmelLib[0]?.id)
  const [activeWaffenId, setActiveWaffenId] = useState(waffenLib[0]?.id)

  useEffect(() => {
    if (!isTool(toolParam)) {
      navigate('/studio/himmel', { replace: true })
    }
  }, [toolParam, navigate])

  useEffect(() => {
    localStorage.setItem(HIMMEL_KEY, JSON.stringify(himmelLib))
  }, [himmelLib])

  useEffect(() => {
    localStorage.setItem(WAFFEN_KEY, JSON.stringify(waffenLib))
  }, [waffenLib])

  const activeHimmel =
    himmelLib.find((m) => m.id === activeHimmelId) ?? himmelLib[0]
  const activeWaffe =
    waffenLib.find((m) => m.id === activeWaffenId) ?? waffenLib[0]

  const updateHimmel = (next: HimmelModel) => {
    setHimmelLib((lib) => lib.map((m) => (m.id === next.id ? next : m)))
  }

  const updateWaffe = (next: WaffenModel) => {
    setWaffenLib((lib) => lib.map((m) => (m.id === next.id ? next : m)))
  }

  return (
    <div className="studio">
      <header className="studio-bar">
        <Link to="/" className="brand-mark">
          <strong>Sakura</strong>
          <span>Studio</span>
        </Link>

        <div className="studio-tabs" role="tablist" aria-label="Werkzeuge">
          <button
            type="button"
            role="tab"
            className="studio-tab"
            aria-selected={tool === 'himmel'}
            onClick={() => navigate('/studio/himmel')}
          >
            Himmel
          </button>
          <button
            type="button"
            role="tab"
            className="studio-tab"
            aria-selected={tool === 'waffen'}
            onClick={() => navigate('/studio/waffen')}
          >
            Waffen
          </button>
        </div>

        <Link className="btn btn-ghost" to="/">
          Zur Startseite
        </Link>
      </header>

      <div className="studio-body">
        {tool === 'himmel' && activeHimmel && (
          <HimmelEditor
            model={activeHimmel}
            library={himmelLib}
            onChange={updateHimmel}
            onSelect={setActiveHimmelId}
            onCreate={() => {
              const next = createHimmel({
                name: `Himmel ${himmelLib.length + 1}`,
              })
              setHimmelLib((lib) => [...lib, next])
              setActiveHimmelId(next.id)
            }}
            onDuplicate={() => {
              const next = createHimmel({
                ...activeHimmel,
                id: crypto.randomUUID(),
                name: `${activeHimmel.name} Kopie`,
              })
              setHimmelLib((lib) => [...lib, next])
              setActiveHimmelId(next.id)
            }}
          />
        )}

        {tool === 'waffen' && activeWaffe && (
          <WaffenEditor
            model={activeWaffe}
            library={waffenLib}
            onChange={updateWaffe}
            onSelect={setActiveWaffenId}
            onCreate={() => {
              const next = createWaffe({
                name: `Waffe ${waffenLib.length + 1}`,
              })
              setWaffenLib((lib) => [...lib, next])
              setActiveWaffenId(next.id)
            }}
            onDuplicate={() => {
              const next = createWaffe({
                ...activeWaffe,
                id: crypto.randomUUID(),
                name: `${activeWaffe.name} Kopie`,
              })
              setWaffenLib((lib) => [...lib, next])
              setActiveWaffenId(next.id)
            }}
          />
        )}
      </div>
    </div>
  )
}
