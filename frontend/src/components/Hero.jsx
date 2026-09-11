import { Link } from 'react-router-dom'
import { useTranslation } from '../i18n/i18n.js'

function Hero() {
  const { t } = useTranslation()

  return (
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
            <span>{t('Centralized configuration')}</span>
          </div>

          <span>↓</span>

          <div>
            <strong>{t('Unit')}</strong>
            <span>{t('Operational context')}</span>
          </div>

          <span>↓</span>

          <div>
            <strong>{t('Resident')}</strong>
            <span>{t('Traceable activity')}</span>
          </div>
        </div>
      </div>
    </section>
  )
}

export default Hero