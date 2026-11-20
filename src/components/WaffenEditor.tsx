import {
  WAFFEN_PRESETS,
  type WaffenModel,
  type WaffenPresetId,
} from '../data/presets'
import { WaffenPreview } from './WaffenPreview'

interface Props {
  model: WaffenModel
  library: WaffenModel[]
  onChange: (next: WaffenModel) => void
  onSelect: (id: string) => void
  onCreate: () => void
  onDuplicate: () => void
}

export function WaffenEditor({
  model,
  library,
  onChange,
  onSelect,
  onCreate,
  onDuplicate,
}: Props) {
  const patch = (partial: Partial<WaffenModel>) =>
    onChange({ ...model, ...partial })

  return (
    <>
      <aside className="studio-panel">
        <div>
          <h1 className="panel-title">Waffen</h1>
          <p className="panel-sub">
            Weapon-Modelle formen — Silhouette, Metall, Kantenlicht und Trail.
          </p>
        </div>

        <div className="field">
          <label htmlFor="waffe-name">Name</label>
          <input
            id="waffe-name"
            type="text"
            value={model.name}
            onChange={(e) => patch({ name: e.target.value })}
          />
        </div>

        <div className="field">
          <span>Preset</span>
          <div className="preset-grid" role="listbox" aria-label="Waffen Presets">
            {(Object.keys(WAFFEN_PRESETS) as WaffenPresetId[]).map((id) => {
              const p = WAFFEN_PRESETS[id]
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
          id="metal"
          label="Metall"
          value={model.metal}
          onChange={(metal) => patch({ metal })}
        />
        <RangeField
          id="edge"
          label="Kanten-Glow"
          value={model.edgeGlow}
          onChange={(edgeGlow) => patch({ edgeGlow })}
        />
        <RangeField
          id="bloom"
          label="Bloom"
          value={model.bloom}
          onChange={(bloom) => patch({ bloom })}
        />
        <RangeField
          id="trail"
          label="Trail"
          value={model.trail}
          onChange={(trail) => patch({ trail })}
        />
        <RangeField
          id="scale"
          label="Größe"
          value={model.scale}
          onChange={(scale) => patch({ scale })}
        />

        <label className="toggle">
          <input
            type="checkbox"
            checked={model.enchanted}
            onChange={(e) => patch({ enchanted: e.target.checked })}
          />
          Verzaubert
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
                <small>{WAFFEN_PRESETS[item.preset].label}</small>
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

      <section className="studio-preview" aria-label="Waffen Vorschau">
        <span className="preview-label">Live-Vorschau · Waffen</span>
        <WaffenPreview model={model} />
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
}: {
  id: string
  label: string
  value: number
  onChange: (v: number) => void
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
        min={0}
        max={100}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      />
    </div>
  )
}
