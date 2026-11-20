# Sakura Visuals by Icee

Client-Webapp für Sakura-Style Visuals: **Himmel** (Sky-Modelle) und **Waffen** (Weapon-Modelle) — zwei gleichwertige Creation Flows.

Inspiriert von Visuals-Client-UX-Mustern (Layout/Flows), ohne fremde Assets oder Texte zu kopieren.

## Lokal starten

```bash
npm install
npm run dev
```

Dann `http://localhost:5173` öffnen.

```bash
npm run build    # Production Build
npm run preview  # Build lokal ansehen
```

## Was drin ist

| Bereich | Route | Inhalt |
|--------|--------|--------|
| Landing | `/` | Brand-first Hero, Einstiege zu Himmel & Waffen |
| Himmel Studio | `/studio/himmel` | Presets, Dichte, Glow, Drift, Bibliothek |
| Waffen Studio | `/studio/waffen` | Presets, Metall, Bloom, Trail, Bibliothek |

Modelle werden in `localStorage` gehalten (`sakura.himmel.library`, `sakura.waffen.library`).

## Stack

React 19 · Vite · TypeScript · React Router
