// import { useState } from 'react'
// import { useTranslation } from '../i18n/i18n.js'

// const navigation = [
// ['Dashboard', '⌂'],
// ['Buildings', '▦'],
// ['Residents', '♙'],
// ['Access Control', '◉'],
// ['Deliveries', '□'],
// ['Bookings', '◷'],
// ['Move Requests', '⇄'],
// ['Incidents', '!'],
// ['Maintenance', '⌁'],
// ['Notifications', '○'],
// ]

// function Dashboard() {
// const { t, language, changeLanguage } = useTranslation()
// const [sidebarOpen, setSidebarOpen] = useState(false)

// return (
// <div className="app-shell">
// {sidebarOpen && (
// <button
// className="mobile-backdrop"
// onClick={() => setSidebarOpen(false)}
// aria-label={t('Close')}
// />
// )}

//   <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
//     <div className="brand">
//       <div className="brand-mark">CT</div>

//       <div>
//         <strong>{t('CondoTrack')}</strong>
//         <span>{t('Property operations')}</span>
//       </div>

//       <button
//         className="icon-button mobile-only"
//         onClick={() => setSidebarOpen(false)}
//         aria-label={t('Close')}
//       >
//         ×
//       </button>
//     </div>

//     <nav className="nav-list">
//       {navigation.map(([label, icon]) => (
//         <button
//           key={label}
//           className={`nav-item ${label === 'Dashboard' ? 'active' : ''}`}
//         >
//           <span className="nav-icon">{icon}</span>
//           <span>{t(label)}</span>

//           {label === 'Dashboard' && (
//             <span className="nav-chevron">›</span>
//           )}
//         </button>
//       ))}
//     </nav>

//     <div className="sidebar-footer">
//       <button className="nav-item">
//         <span className="nav-icon">⚙</span>
//         <span>{t('Administration')}</span>
//       </button>

//       <button className="nav-item">
//         <span className="nav-icon">↪</span>
//         <span>{t('Sign Out')}</span>
//       </button>
//     </div>
//   </aside>

//   <div className="main-area">
//     <header className="topbar">
//       <button
//         className="icon-button mobile-only"
//         onClick={() => setSidebarOpen(true)}
//         aria-label={t('Open navigation')}
//       >
//         ☰
//       </button>

//       <div className="topbar-title">
//         <span className="eyebrow">{t('CONDO OPERATIONS')}</span>
//         <h1>{t('Dashboard')}</h1>
//       </div>

//       <button
//         className="language-button"
//         onClick={changeLanguage}
//         title={t('Language')}
//       >
//         <span>◌</span>
//         <span>{language.toUpperCase()}</span>
//       </button>
//     </header>

//     <main className="page-content">
//       <div className="welcome">
//         <div>
//           <p className="eyebrow">{t('Foundation ready')}</p>

//           <h2>
//             {t(
//               'Centralized building and condominium operations, fully traceable.'
//             )}
//           </h2>

//           <p className="muted">
//             {t(
//               'The application foundation is ready for the first operational modules.'
//             )}
//           </p>
//         </div>
//       </div>

//       <section className="dashboard-grid">
//         <article className="panel panel-wide">
//           <div className="panel-header">
//             <div>
//               <p className="eyebrow">{t('CONDO TRACK CORE')}</p>
//               <h3>{t('Project Status')}</h3>
//             </div>

//             <span className="status-pill">
//               {t('Foundation Ready')}
//             </span>
//           </div>

//           <p className="panel-copy">
//             {t(
//               'PostgreSQL schema, JPA entity foundation, audit fields, operational indexes and booking-conflict protection are established as the first backend milestone.'
//             )}
//           </p>
//         </article>

//         <article className="panel">
//           <p className="eyebrow">{t('DATA LINEAGE')}</p>

//           <h3>{t('Core Data Hierarchy')}</h3>

//           <div className="hierarchy">
//             {t('Building → Unit → Resident')
//               .split(' → ')
//               .map((item, index, items) => (
//                 <span
//                   key={item}
//                   className="hierarchy-item"
//                 >
//                   <span>{item}</span>

//                   {index < items.length - 1 && (
//                     <span>›</span>
//                   )}
//                 </span>
//               ))}
//           </div>
//         </article>

//         <article className="panel">
//           <p className="eyebrow">{t('NEXT MODULE')}</p>

//           <h3>{t('Unified Unit Lookup')}</h3>

//           <p className="muted">
//             {t(
//               'One unit will aggregate residents, access history, deliveries, bookings, moves, incidents and maintenance.'
//             )}
//           </p>
//         </article>
//       </section>
//     </main>
//   </div>
// </div>

// )
// }

// export default Dashboard


//---------------------- version -------------------
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { getRolePermissions } from '../auth/rolePermissions.js'

function ProtectedRoute({ moduleKey }) {
  const { user, isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname }}
      />
    )
  }

  if (moduleKey) {
    const permissions = getRolePermissions(user.role)
    const permission = permissions[moduleKey]

    if (!permission || permission === 'none') {
      return (
        <Navigate
          to="/dashboard"
          replace
          state={{
            accessDenied: true,
            moduleKey,
          }}
        />
      )
    }
  }

  return <Outlet />
}

export default ProtectedRoute