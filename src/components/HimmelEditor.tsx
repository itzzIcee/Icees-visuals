import {
  HIMMEL_PRESETS,
  type HimmelModel,
  type HimmelPresetId,
} from '../data/presets'
import { HimmelPreview } from './HimmelPreview'

interface Props {
  model: HimmelModel
  library: HimmelModel[]
  onChange: (next: HimmelModel) => void
  onSelect: (id: string) => void
  onCreate: () => void
  onDuplicate: () => void
}

export function HimmelEditor({
  model,
  library,
  onChange,
  onSelect,
  onCreate,
  onDuplicate,
}: Props) {
  const patch = (partial: Partial<HimmelModel>) =>
    onChange({ ...model, ...partial })

  return (
    <>
      <aside className="studio-panel">
        <div>
          <h1 className="panel-title">Himmel</h1>
          <p className="panel-sub">
            Sky-Modelle gestalten — Presets, Dichte, Glow und Drift.
          </p>
        </div>

        <div className="field">
          <label htmlFor="himmel-name">Name</label>
          <input
            id="himmel-name"
            type="text"
            value={model.name}
            onChange={(e) => patch({ name: e.target.value })}
          />
        </div>

        <div className="field">
          <span className="field-label-static">Preset</span>
          <div className="preset-grid" role="listbox" aria-label="Himmel Presets">
            {(Object.keys(HIMMEL_PRESETS) as HimmelPresetId[]).map((id) => {
              const p = HIMMEL_PRESETS[id]
              return (
                <button
                  key={id}
                  type="button"
                  className="preset-chip"
                  role="option"
                  aria-selected={model.preset === id}
                  aria-pressed={model.preset === id}
                  onClick={() => patch({ preset: id })}
                >
                  <strong>{p.label}</strong>
                  <span>{p.hint}</span>
                </button>
              )
            })}
          </div>
        </div>

        <RangeField
          id="density"
          label="Petal-Dichte"
          value={model.density}
          onChange={(density) => patch({ density })}
        />
        <RangeField
          id="glow"
          label="Horizont-Glow"
          value={model.glow}
          onChange={(glow) => patch({ glow })}
        />
        <RangeField
          id="drift"
          label="Drift"
          value={model.drift}
          onChange={(drift) => patch({ drift })}
        />
        <RangeField
          id="hue"
          label="Farbverschiebung"
          value={model.hueShift}
          min={-40}
          max={40}
          onChange={(hueShift) => patch({ hueShift })}
        />

        <label className="toggle">
          <input
            type="checkbox"
            checked={model.stars}
            onChange={(e) => patch({ stars: e.target.checked })}
          />
          Sterne
        </label>
        <label className="toggle">
          <input
            type="checkbox"
            checked={model.mist}
            onChange={(e) => patch({ mist: e.target.checked })}
          />
          Nebel
        </label>

        <div className="field">
          <span>Bibliothek</span>
          <div className="library">
            {library.map((item) => (
              <button
                key={item.id}
                type="button"
                className="library-item"
                aria-current={item.id === model.id}
                onClick={() => onSelect(item.id)}
              >
                <span>{item.name}</span>
                <small>{HIMMEL_PRESETS[item.preset].label}</small>
              </button>
            ))}
          </div>
        </div>

        <div className="panel-actions">
          <button type="button" className="btn btn-primary" onClick={onCreate}>
            Neu
          </button>
          <button type="button" className="btn btn-quiet" onClick={onDuplicate}>
            Duplizieren
          </button>
        </div>
      </aside>

      <section className="studio-preview" aria-label="Himmel Vorschau">
        <span className="preview-label">Live-Vorschau · Himmel</span>
        <HimmelPreview model={model} />
        <span className="preview-name">{model.name}</span>
      </section>
    </>
  )
}

function RangeField({
  id,
  label,
  value,
  onChange,
  min = 0,
  max = 100,
}: {
  id: string
  label: string
  value: number
  onChange: (v: number) => void
  min?: number
  max?: number
}) {
  return (
    <div className="field">
      <div className="field-row">
        <label htmlFor={id}>{label}</label>
        <span className="field-value">{value}</span>
      </div>
      <input
        id={id}
        type="range"
        min={min}
        max={max}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      />
    </div>
  )
}
