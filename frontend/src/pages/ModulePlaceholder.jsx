import { Link, useParams } from 'react-router-dom'
import { useTranslation } from '../i18n/i18n.js'

import LanguageToggle from '../components/LanguageToggle.jsx'
import { moduleDefinitions } from '../auth/rolePermissions.js'
import { useAuth } from '../auth/AuthContext.jsx'

function ModulePlaceholder() {
  const { moduleKey } = useParams()
  const { t } = useTranslation()
  const { user } = useAuth()

  const module = moduleDefinitions.find(
    (item) => item.key === moduleKey
  )

  const title = module
    ? t(module.label)
    : t('Module')

  return (
    <div className="public-page">
      <header className="public-navbar">
        <div className="public-navbar-inner">
          <Link
            className="public-brand"
            to="/dashboard"
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

          <LanguageToggle />
        </div>
      </header>

      <main className="page-content">
        <section className="panel panel-wide">
          <p className="eyebrow">
            {t('MODULE')}
          </p>

          <h1>{title}</h1>

          <p className="muted">
            {t(
              'This module is currently under development.'
            )}
          </p>

          <p className="panel-copy">
            {t('Signed in as')}:{' '}
            <strong>{user.name}</strong>
          </p>

          <Link
            className="button button-primary"
            to="/dashboard"
          >
            {t('Back to Dashboard')}
          </Link>
        </section>
      </main>
    </div>
  )
}

export default ModulePlaceholder