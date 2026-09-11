
import { Link } from 'react-router-dom'

import Footer from '../components/Footer.jsx'
import LanguageToggle from '../components/LanguageToggle.jsx'
import { useTranslation } from '../i18n/i18n.js'

function Home() {
  const { t } = useTranslation()

  return (
    <div className="public-page">
      <header className="public-navbar">
        <div className="public-navbar-inner">
          <Link
            className="public-brand"
            to="/"
          >
            <span className="public-brand-mark">
              CT
            </span>

            <span>
              <strong>{t('CondoTrack')}</strong>
              <small>
                {t('Property operations')}
              </small>
            </span>
          </Link>

          <div className="public-nav-actions">
            <LanguageToggle />

            <Link
              className="button button-outline"
              to="/login"
            >
              {t('Log In')}
            </Link>
          </div>
        </div>
      </header>

      <main>
        <section className="hero-section">
          <div className="hero-copy">
            <p className="eyebrow">
              {t('CONDO OPERATIONS')}
            </p>

            <h1>
              {t(
                'Centralized building and condominium operations, fully traceable.'
              )}
            </h1>

            <p className="hero-description">
              {t(
                'A single operational platform connecting buildings, units, residents, access, deliveries, bookings, incidents and maintenance.'
              )}
            </p>

            <div className="hero-actions">
              <Link
                className="button button-primary"
                to="/login"
              >
                {t('Open Dashboard')}
              </Link>

              <a
                className="button button-outline"
                href="#features"
              >
                {t('Explore platform')}
              </a>
            </div>
          </div>

          <div className="hero-card">
            <p className="eyebrow">
              {t('Core Data Hierarchy')}
            </p>

            <div className="hero-hierarchy">
              <div>
                <strong>{t('Building')}</strong>
                <span>
                  {t('Centralized configuration')}
                </span>
              </div>

              <span>↓</span>

              <div>
                <strong>{t('Unit')}</strong>
                <span>
                  {t('Operational context')}
                </span>
              </div>

              <span>↓</span>

              <div>
                <strong>{t('Resident')}</strong>
                <span>
                  {t('Traceable activity')}
                </span>
              </div>
            </div>
          </div>
        </section>

        <section
          className="feature-strip"
          id="features"
        >
          <article className="feature-card">
            <span className="feature-number">
              01
            </span>

            <h2>{t('Unified operations')}</h2>

            <p>
              {t(
                'Connect the operational information of each building and unit in one place.'
              )}
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-number">
              02
            </span>

            <h2>{t('Role-based access')}</h2>

            <p>
              {t(
                'Each role sees the modules and information appropriate to its responsibilities.'
              )}
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-number">
              03
            </span>

            <h2>{t('Full traceability')}</h2>

            <p>
              {t(
                'Operational events remain linked to the building, unit and people involved.'
              )}
            </p>
          </article>
        </section>
      </main>

      <Footer />
    </div>
  )
}

export default Home