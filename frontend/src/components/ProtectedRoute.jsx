//-------------------- milestone 0 ---------------------
// import { Navigate, Outlet, useLocation } from 'react-router-dom'
// import { useAuth } from '../auth/AuthContext.jsx'
// import { getRolePermissions } from '../auth/rolePermissions.js'

// function ProtectedRoute({ moduleKey }) {
//   const { user, isAuthenticated } = useAuth()
//   const location = useLocation()

//   if (!isAuthenticated) {
//     return (
//       <Navigate
//         to="/login"
//         replace
//         state={{ from: location.pathname }}
//       />
//     )
//   }

//   if (moduleKey) {
//     const permissions = getRolePermissions(user.role)
//     const permission = permissions[moduleKey]

//     if (!permission || permission === 'none') {
//       return (
//         <Navigate
//           to="/dashboard"
//           replace
//           state={{
//             accessDenied: true,
//             moduleKey,
//           }}
//         />
//       )
//     }
//   }

//   return <Outlet />
// }

// export default ProtectedRoute


//--------------------- milestone 1 ---------------------

import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '../auth/AuthContext.jsx'
import { getRolePermissions } from '../auth/rolePermissions.js'
import { useTranslation } from '../i18n/i18n.js'

function ProtectedRoute({ moduleKey }) {
  const { t } = useTranslation()
  const { user, isAuthenticated, isInitializing } = useAuth()
  const location = useLocation()

  if (isInitializing) {
    return (
      <div className="auth-loading" role="status">
        {t('Loading...')}
      </div>
    )
  }

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
    const role = user?.roles?.[0] || user?.role
    const permissions = getRolePermissions(role)
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
