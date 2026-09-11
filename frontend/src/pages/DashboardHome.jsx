import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  getModulePermission,
  getVisibleModules,
  roleLabels,
} from '../auth/rolePermissions.js'
import { useTranslation } from '../i18n/i18n.js'


function DashboardHome() {
  const { t } = useTranslation()
  const { user } = useAuth()

  const visibleModules = getVisibleModules(user.role)
  const roleLabel = roleLabels[user.role] || user.role

  const permissionLabels = {
    full: 'Full access',
    view: 'View',
    own: 'Own records',
    create: 'Create',
    manage: 'Manage',
    approve: 'Approve',
    assigned: 'Assigned work',
  }

  return (
    <>
      <section className="welcome">
        <div>
          <p className="eyebrow">{t('Welcome back')}</p>

          <h2>
            {t(
              'Centralized building and condominium operations, fully traceable.'
            )}
          </h2>

          <p className="muted">
            {t(
              'The modules available below are based on your current role and permissions.'
            )}
          </p>
        </div>
      </section>

      <section className="dashboard-grid">
        <article className="panel panel-wide role-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">{t('YOUR ROLE')}</p>
              <h3>{t(roleLabel)}</h3>
            </div>

            <span className="status-pill">
              {visibleModules.length} {t('available modules')}
            </span>
          </div>

          <p className="panel-copy">
            {t(
              'Your dashboard displays only the operational modules available to your role.'
            )}
          </p>

          <div className="module-link-grid">
            {visibleModules.map((module) => {
              const permission = getModulePermission(
                user.role,
                module.key
              )

              return (
                <NavLink
                  key={module.key}
                  to={module.path}
                  className="module-link"
                >
                  <span className="module-link-icon">
                    {module.icon}
                  </span>

                  <span className="module-link-content">
                    <strong>{t(module.label)}</strong>

                    <small>
                      {t(
                        permissionLabels[permission] ||
                          permission
                      )}
                    </small>
                  </span>

                  <span className="module-link-arrow">
                    →
                  </span>
                </NavLink>
              )
            })}
          </div>
        </article>

        <article className="panel">
          <p className="eyebrow">{t('DATA LINEAGE')}</p>
          <h3>{t('Core Data Hierarchy')}</h3>

          <div className="hierarchy">
            {t('Building → Unit → Resident')
              .split(' → ')
              .map((item, index, items) => (
                <span
                  key={item}
                  className="hierarchy-item"
                >
                  <span>{item}</span>

                  {index < items.length - 1 && (
                    <span>›</span>
                  )}
                </span>
              ))}
          </div>
        </article>

        <article className="panel">
          <p className="eyebrow">{t('NEXT MODULE')}</p>
          <h3>{t('Unified Unit Lookup')}</h3>

          <p className="muted">
            {t(
              'One unit will aggregate residents, access history, deliveries, bookings, moves, incidents and maintenance.'
            )}
          </p>
        </article>
      </section>
    </>
  )
}

export default DashboardHome