import { Link } from 'react-router-dom'
import { PetalField } from '../components/PetalField'
import { SakuraBranch } from '../components/SakuraBranch'

export function Landing() {
  return (
    <div className="app-shell">
      <header className="site-nav">
        <Link to="/" className="brand-mark" aria-label="Sakura Visuals Start">
          <strong>Sakura</strong>
          <span>by Icee</span>
        </Link>
        <nav className="nav-links" aria-label="Hauptnavigation">
          <Link className="nav-link" to="/studio/himmel">
            Himmel
          </Link>
          <Link className="nav-link" to="/studio/waffen">
            Waffen
          </Link>
          <Link className="btn btn-ghost" to="/studio">
            Studio öffnen
          </Link>
        </nav>
      </header>

      <section className="hero">
        <div className="hero-atmosphere" aria-hidden="true" />
        <PetalField count={20} />
        <SakuraBranch />
        <div className="hero-content">
          <p className="hero-byline">Visuals Client</p>
          <h1 className="hero-brand">
            Sakura <em>Visuals</em>
          </h1>
          <p className="hero-lead">
            Himmel und Waffen — zwei gleichwertige Werkzeuge für Sakura-Atmosphäre
            und Klingenlicht. Gestalte, prüfe live, speichere in deiner Bibliothek.
          </p>
          <div className="hero-cta">
            <Link className="btn btn-primary" to="/studio/himmel">
              Himmel gestalten
            </Link>
            <Link className="btn btn-ghost" to="/studio/waffen">
              Waffen gestalten
            </Link>
          </div>
        </div>
      </section>

      <section className="landing-tools" aria-labelledby="tools-heading">
        <div className="section-head">
          <h2 id="tools-heading">Zwei Flows. Gleiche Priorität.</h2>
          <p>
            Wie ein Visuals-Menü: klar getrennt, sofort editierbar — ohne Combat,
            ohne Ballast. Nur Look & Feel.
          </p>
        </div>
        <div className="tool-pair">
          <article className="tool-entry">
            <span className="tool-kicker">01 · Sky</span>
            <h3>Himmel</h3>
            <p>
              Fünf Presets von Dämmerung bis Sternregen. Petal-Dichte, Horizont-Glow,
              Drift, Sterne und Nebel — live in der Vorschau.
            </p>
            <Link className="btn btn-quiet" to="/studio/himmel">
              Zum Himmel-Studio
            </Link>
          </article>
          <article className="tool-entry">
            <span className="tool-kicker">02 · Weapons</span>
            <h3>Waffen</h3>
            <p>
              Kirschklinge, Blütenbogen, Mondsichel und mehr. Metall, Kantenlicht,
              Bloom, Trail und Verzauberung — gleiche Tiefe wie beim Himmel.
            </p>
            <Link className="btn btn-quiet" to="/studio/waffen">
              Zum Waffen-Studio
            </Link>
          </article>
        </div>
      </section>

      <footer className="site-footer">
        Sakura Visuals by Icee · Client Preview
      </footer>
    </div>
  )
}
