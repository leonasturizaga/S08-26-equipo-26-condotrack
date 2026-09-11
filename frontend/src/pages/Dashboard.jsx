
// import { useState } from 'react'
// import { NavLink } from 'react-router-dom'

// import LanguageToggle from '../components/LanguageToggle.jsx'
// import { useAuth } from '../auth/AuthContext.jsx'
// import {
//   getModulePermission,
//   getVisibleModules,
//   roleLabels,
// } from '../auth/rolePermissions.js'
// import { useTranslation } from '../i18n/i18n.js'

// const sidebarModules = [
//   {
//     key: 'buildings',
//     icon: '▦',
//     label: 'Buildings',
//     path: '/dashboard/buildings',
//   },
//   {
//     key: 'units',
//     icon: '⌂',
//     label: 'Units',
//     path: '/dashboard/units',
//   },
//   {
//     key: 'residents',
//     icon: '♙',
//     label: 'Residents / Owners',
//     path: '/dashboard/residents',
//   },
//   {
//     key: 'access',
//     icon: '◉',
//     label: 'Access / Visitors',
//     path: '/dashboard/access',
//   },
//   {
//     key: 'deliveries',
//     icon: '□',
//     label: 'Deliveries / Mail',
//     path: '/dashboard/deliveries',
//   },
//   {
//     key: 'bookings',
//     icon: '◷',
//     label: 'Common Area Bookings',
//     path: '/dashboard/bookings',
//   },
//   {
//     key: 'incidents',
//     icon: '!',
//     label: 'Incidents',
//     path: '/dashboard/incidents',
//   },
//   {
//     key: 'maintenance',
//     icon: '⌁',
//     label: 'Maintenance',
//     path: '/dashboard/maintenance',
//   },
//   {
//     key: 'moves',
//     icon: '⇄',
//     label: 'Move Requests',
//     path: '/dashboard/moves',
//   },
//   {
//     key: 'notifications',
//     icon: '○',
//     label: 'Notifications',
//     path: '/dashboard/notifications',
//   },
// ]

// const permissionLabels = {
//   full: 'Full access',
//   view: 'View',
//   own: 'Own records',
//   create: 'Create',
//   manage: 'Manage',
//   approve: 'Approve',
//   assigned: 'Assigned work',
// }

// function Dashboard() {
//   const { t } = useTranslation()
//   const { user, logout } = useAuth()

//   const [sidebarOpen, setSidebarOpen] = useState(false)

//   const visibleModules = getVisibleModules(user.role)

//   const allowedSidebarModules = sidebarModules.filter((module) =>
//     visibleModules.some(
//       (visibleModule) => visibleModule.key === module.key
//     )
//   )

//   const roleLabel = roleLabels[user.role] || user.role

//   const handleLogout = () => {
//     logout()
//   }

//   return (
//     <div className="app-shell">
//       {sidebarOpen && (
//         <button
//           className="mobile-backdrop"
//           onClick={() => setSidebarOpen(false)}
//           aria-label={t('Close')}
//         />
//       )}

//       <aside
//         className={`sidebar ${
//           sidebarOpen ? 'sidebar-open' : ''
//         }`}
//       >
//         <div className="brand">
//           <div className="brand-mark">CT</div>

//           <div>
//             <strong>{t('CondoTrack')}</strong>
//             <span>{t('Property operations')}</span>
//           </div>

//           <button
//             className="icon-button mobile-only"
//             onClick={() => setSidebarOpen(false)}
//             aria-label={t('Close')}
//             type="button"
//           >
//             ×
//           </button>
//         </div>

//         <nav className="nav-list">
//           <NavLink
//             to="/dashboard"
//             className={({ isActive }) =>
//               `nav-item ${isActive ? 'active' : ''}`
//             }
//             onClick={() => setSidebarOpen(false)}
//           >
//             <span className="nav-icon">⌂</span>
//             <span>{t('Dashboard')}</span>
//             <span className="nav-chevron">›</span>
//           </NavLink>

//           {allowedSidebarModules.map((module) => (
//             <NavLink
//               key={module.key}
//               to={module.path}
//               className={({ isActive }) =>
//                 `nav-item ${isActive ? 'active' : ''}`
//               }
//               onClick={() => setSidebarOpen(false)}
//             >
//               <span className="nav-icon">
//                 {module.icon}
//               </span>

//               <span>{t(module.label)}</span>
//             </NavLink>
//           ))}
//         </nav>

//         <div className="sidebar-footer">
//           {getModulePermission(
//             user.role,
//             'userManagement'
//           ) !== 'none' && (
//             <NavLink
//               to="/dashboard/user-management"
//               className="nav-item"
//               onClick={() => setSidebarOpen(false)}
//             >
//               <span className="nav-icon">⚙</span>
//               <span>{t('User Management')}</span>
//             </NavLink>
//           )}

//           <button
//             className="nav-item"
//             onClick={handleLogout}
//             type="button"
//           >
//             <span className="nav-icon">↪</span>
//             <span>{t('Sign Out')}</span>
//           </button>
//         </div>
//       </aside>

//       <div className="main-area">
//         <header className="topbar">
//           <button
//             className="icon-button mobile-only"
//             onClick={() => setSidebarOpen(true)}
//             aria-label={t('Open navigation')}
//             type="button"
//           >
//             ☰
//           </button>

//           <div className="topbar-title">
//             <span className="eyebrow">
//               {t('CONDO OPERATIONS')}
//             </span>

//             <h1>{t('Dashboard')}</h1>
//           </div>

//           <div className="topbar-actions">
//             <div className="user-summary">
//               <strong>{user.name}</strong>
//               <span>{t(roleLabel)}</span>
//             </div>

//             <LanguageToggle />
//           </div>
//         </header>

//         <main className="page-content">
//           <section className="welcome">
//             <div>
//               <p className="eyebrow">
//                 {t('Welcome back')}
//               </p>

//               <h2>
//                 {t(
//                   'Centralized building and condominium operations, fully traceable.'
//                 )}
//               </h2>

//               <p className="muted">
//                 {t(
//                   'The modules available below are based on your current role and permissions.'
//                 )}
//               </p>
//             </div>
//           </section>

//           <section className="dashboard-grid">
//             <article className="panel panel-wide">
//               <div className="panel-header">
//                 <div>
//                   <p className="eyebrow">
//                     {t('YOUR ROLE')}
//                   </p>

//                   <h3>{t(roleLabel)}</h3>
//                 </div>

//                 <span className="status-pill">
//                   {visibleModules.length}{' '}
//                   {t('available modules')}
//                 </span>
//               </div>

//               <p className="panel-copy">
//                 {t(
//                   'Your dashboard displays only the operational modules available to your role.'
//                 )}
//               </p>
//             </article>

//             <article className="panel">
//               <p className="eyebrow">
//                 {t('DATA LINEAGE')}
//               </p>

//               <h3>{t('Core Data Hierarchy')}</h3>

//               <div className="hierarchy">
//                 {t('Building → Unit → Resident')
//                   .split(' → ')
//                   .map((item, index, items) => (
//                     <span
//                       key={item}
//                       className="hierarchy-item"
//                     >
//                       <span>{item}</span>

//                       {index < items.length - 1 && (
//                         <span>›</span>
//                       )}
//                     </span>
//                   ))}
//               </div>
//             </article>
//           </section>

//           <section className="module-section">
//             <div className="section-header">
//               <div>
//                 <p className="eyebrow">
//                   {t('AVAILABLE MODULES')}
//                 </p>

//                 <h2>{t('Your workspace')}</h2>
//               </div>
//             </div>

//             <div className="module-card-grid">
//               {visibleModules.map((module) => {
//                 const permission = getModulePermission(
//                   user.role,
//                   module.key
//                 )

//                 return (
//                   <NavLink
//                     key={module.key}
//                     to={module.path}
//                     className="module-card"
//                   >
//                     <div className="module-card-icon">
//                       {module.icon}
//                     </div>

//                     <div className="module-card-content">
//                       <h3>{t(module.label)}</h3>

//                       <p>
//                         {t(
//                           permissionLabels[permission] ||
//                             permission
//                         )}
//                       </p>
//                     </div>

//                     <span className="module-card-arrow">
//                       →
//                     </span>
//                   </NavLink>
//                 )
//               })}
//             </div>
//           </section>
//         </main>
//       </div>
//     </div>
//   )
// }

// export default Dashboard

import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'

import LanguageToggle from '../components/LanguageToggle.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  getModulePermission,
  getVisibleModules,
  roleLabels,
} from '../auth/rolePermissions.js'
import { useTranslation } from '../i18n/i18n.js'

const sidebarModules = [
  {
    key: 'buildings',
    icon: '▦',
    label: 'Buildings',
    path: '/dashboard/buildings',
  },
  {
    key: 'units',
    icon: '⌂',
    label: 'Units',
    path: '/dashboard/units',
  },
  {
    key: 'residents',
    icon: '♙',
    label: 'Residents / Owners',
    path: '/dashboard/residents',
  },
  {
    key: 'access',
    icon: '◉',
    label: 'Access / Visitors',
    path: '/dashboard/access',
  },
  {
    key: 'deliveries',
    icon: '□',
    label: 'Deliveries / Mail',
    path: '/dashboard/deliveries',
  },
  {
    key: 'bookings',
    icon: '◷',
    label: 'Common Area Bookings',
    path: '/dashboard/bookings',
  },
  {
    key: 'incidents',
    icon: '!',
    label: 'Incidents',
    path: '/dashboard/incidents',
  },
  {
    key: 'maintenance',
    icon: '⌁',
    label: 'Maintenance',
    path: '/dashboard/maintenance',
  },
  {
    key: 'moves',
    icon: '⇄',
    label: 'Move Requests',
    path: '/dashboard/moves',
  },
  {
    key: 'notifications',
    icon: '○',
    label: 'Notifications',
    path: '/dashboard/notifications',
  },
]

const permissionLabels = {
  full: 'Full access',
  view: 'View',
  own: 'Own records',
  create: 'Create',
  manage: 'Manage',
  approve: 'Approve',
  assigned: 'Assigned work',
}

function DashboardHome({
  user,
  visibleModules,
  t,
}) {
  const roleLabel = roleLabels[user.role] || user.role

  return (
    <>
      <section className="welcome">
        <div>
          <p className="eyebrow">{t('Welcome back')}</p>

          <h2>{t('Centralized building and condominium operations, fully traceable.')}</h2>

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
                  className={({ isActive }) =>
                    `module-link ${
                      isActive ? 'module-link-active' : ''
                    }`
                  }
                >
                  <span className="module-link-icon">
                    {module.icon}
                  </span>

                  <span className="module-link-content">
                    <strong>{t(module.label)}</strong>
                    <small>
                      {t(permissionLabels[permission] || permission)}
                    </small>
                  </span>

                  <span className="module-link-arrow">→</span>
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

function Dashboard() {
  const { t } = useTranslation()
  const { user, logout } = useAuth()

  const [sidebarOpen, setSidebarOpen] = useState(false)

  const visibleModules = getVisibleModules(user.role)

  const allowedSidebarModules = sidebarModules.filter((module) =>
    visibleModules.some(
      (visibleModule) => visibleModule.key === module.key
    )
  )

  return (
    <div className="app-shell">
      {sidebarOpen && (
        <button
          className="mobile-backdrop"
          onClick={() => setSidebarOpen(false)}
          aria-label={t('Close')}
          type="button"
        />
      )}

      <aside
        className={`sidebar ${
          sidebarOpen ? 'sidebar-open' : ''
        }`}
      >
        <div className="brand">
          <div className="brand-mark">CT</div>

          <div>
            <strong>{t('CondoTrack')}</strong>
            <span>{t('Property operations')}</span>
          </div>

          <button
            className="icon-button mobile-only"
            onClick={() => setSidebarOpen(false)}
            aria-label={t('Close')}
            type="button"
          >
            ×
          </button>
        </div>

        <nav className="nav-list">
          <NavLink
            to="/dashboard"
            end
            className={({ isActive }) =>
              `nav-item ${isActive ? 'active' : ''}`
            }
            onClick={() => setSidebarOpen(false)}
          >
            <span className="nav-icon">⌂</span>
            <span>{t('Dashboard')}</span>
          </NavLink>

          {allowedSidebarModules.map((module) => (
            <NavLink
              key={module.key}
              to={module.path}
              className={({ isActive }) =>
                `nav-item ${isActive ? 'active' : ''}`
              }
              onClick={() => setSidebarOpen(false)}
            >
              <span className="nav-icon">
                {module.icon}
              </span>

              <span>{t(module.label)}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          {getModulePermission(
            user.role,
            'userManagement'
          ) !== 'none' && (
            <NavLink
              to="/dashboard/user-management"
              className="nav-item"
              onClick={() => setSidebarOpen(false)}
            >
              <span className="nav-icon">⚙</span>
              <span>{t('User Management')}</span>
            </NavLink>
          )}

          <button
            className="nav-item"
            onClick={logout}
            type="button"
          >
            <span className="nav-icon">↪</span>
            <span>{t('Sign Out')}</span>
          </button>
        </div>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <button
            className="icon-button mobile-only"
            onClick={() => setSidebarOpen(true)}
            aria-label={t('Open navigation')}
            type="button"
          >
            ☰
          </button>

          <div className="topbar-title">
            <span className="eyebrow">
              {t('CONDO OPERATIONS')}
            </span>

            <h1>{t('Dashboard')}</h1>
          </div>

          <div className="topbar-actions">
            <div className="user-summary">
              <strong>{user.name}</strong>
              <span>
                {t(roleLabels[user.role] || user.role)}
              </span>
            </div>

            <LanguageToggle />
          </div>
        </header>

        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default Dashboard