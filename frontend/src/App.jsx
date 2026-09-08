import { useState } from 'react'
import { useTranslation } from './i18n/i18n.js'

const navigation = [
  ['dashboard', '⌂'],
  ['buildings', '▦'],
  ['residents', '♙'],
  ['access', '◉'],
  ['deliveries', '□'],
  ['bookings', '◷'],
  ['moves', '⇄'],
  ['incidents', '!'],
  ['maintenance', '⌁'],
  ['notifications', '○'],
]

function App() {
  const { t, language, changeLanguage } = useTranslation()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="app-shell">
      {sidebarOpen && (
        <button className="mobile-backdrop" onClick={() => setSidebarOpen(false)} aria-label="Close navigation" />
      )}

      <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="brand">
          <div className="brand-mark">CT</div>
          <div>
            <strong>{t('appName')}</strong>
            <span>Property operations</span>
          </div>
          <button className="icon-button mobile-only" onClick={() => setSidebarOpen(false)} aria-label="Close navigation">×</button>
        </div>

        <nav className="nav-list">
          {navigation.map(([label, icon]) => (
            <button key={label} className={`nav-item ${label === 'dashboard' ? 'active' : ''}`}>
              <span className="nav-icon">{icon}</span>
              <span>{t(label)}</span>
              {label === 'dashboard' && <span className="nav-chevron">›</span>}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button className="nav-item"><span className="nav-icon">⚙</span><span>{t('administration')}</span></button>
          <button className="nav-item"><span className="nav-icon">↪</span><span>{t('signOut')}</span></button>
        </div>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <button className="icon-button mobile-only" onClick={() => setSidebarOpen(true)} aria-label="Open navigation">☰</button>
          <div className="topbar-title">
            <span className="eyebrow">CONDO OPERATIONS</span>
            <h1>{t('dashboard')}</h1>
          </div>
          <button className="language-button" onClick={changeLanguage} title={t('language')}>
            <span>◌</span><span>{language.toUpperCase()}</span>
          </button>
        </header>

        <main className="page-content">
          <div className="welcome">
            <div>
              <p className="eyebrow">{t('ready')}</p>
              <h2>{t('tagline')}</h2>
              <p className="muted">{t('foundation')}</p>
            </div>
          </div>

          <section className="dashboard-grid">
            <article className="panel panel-wide">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">CONDO TRACK CORE</p>
                  <h3>{t('projectStatus')}</h3>
                </div>
                <span className="status-pill">{t('ready')}</span>
              </div>
              <p className="panel-copy">
                PostgreSQL schema, JPA entity foundation, audit fields, operational indexes and booking-conflict protection are established as the first backend milestone.
              </p>
            </article>

            <article className="panel">
              <p className="eyebrow">DATA LINEAGE</p>
              <h3>{t('hierarchy')}</h3>
              <div className="hierarchy">
                {t('hierarchyValue').split(' → ').map((item, index, items) => (
                  <span key={item} className="hierarchy-item"><span>{item}</span>{index < items.length - 1 && <span>›</span>}</span>
                ))}
              </div>
            </article>

            <article className="panel">
              <p className="eyebrow">NEXT MODULE</p>
              <h3>{t('unifiedLookup')}</h3>
              <p className="muted">{t('unifiedLookupText')}</p>
            </article>
          </section>
        </main>
      </div>
    </div>
  )
}

export default App
