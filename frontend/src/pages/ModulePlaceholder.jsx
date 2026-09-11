
import { useParams } from 'react-router-dom'

import {
  getModulePermission,
  moduleDefinitions,
} from '../auth/rolePermissions.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

function ModulePlaceholder() {
  const { moduleKey } = useParams()
  const { t } = useTranslation()
  const { user } = useAuth()

  const module = moduleDefinitions.find(
    (item) => item.key === moduleKey
  )

  const permission = getModulePermission(
    user.role,
    moduleKey
  )

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('MODULE')}</p>

          <h2>
            {module
              ? t(module.label)
              : t('Module')}
          </h2>

          <p className="muted">
            {t(
              'This module is currently under development.'
            )}
          </p>
        </div>

        <span className="status-pill">
          {t(permission)}
        </span>
      </div>

      <div className="module-empty-state">
        <div className="module-empty-icon">
          {module?.icon || '□'}
        </div>

        <h3>
          {module
            ? t(module.label)
            : t('Module')}
        </h3>

        <p className="muted">
          {t(
            'The operational functionality for this module will be implemented next.'
          )}
        </p>
      </div>
    </section>
  )
}

export default ModulePlaceholder