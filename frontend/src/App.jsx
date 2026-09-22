//------------------ milestone 2 ------------------
import { Navigate, Route, Routes } from 'react-router-dom'

import { AuthProvider } from './auth/AuthContext.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

import Dashboard from './pages/Dashboard.jsx'
import BuildingsPage from './pages/BuildingsPage.jsx'
import UnitsPage from './pages/UnitsPage.jsx'
import ResidentsPage from './pages/ResidentsPage.jsx'
import UserManagementPage from './pages/UserManagementPage.jsx'
import AccessPage from './pages/AccessPage.jsx'
import DeliveriesPage from './pages/DeliveriesPage.jsx'
import IncidentsPage from './pages/IncidentsPage.jsx'
import MaintenancePage from './pages/MaintenancePage.jsx'
import MovesPage from './pages/MovesPage.jsx'
import DashboardHome from './pages/DashboardHome.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import ModulePlaceholder from './pages/ModulePlaceholder.jsx'
import RbacSettingsPage from './pages/RbacSettingsPage.jsx'

const protectedModules = [
  'buildings',
  'units',
  'residents',
  'access',
  'deliveries',
  'bookings',
  'incidents',
  'maintenance',
  'moves',
  'notifications',
  'reports',
  'buildingConfig',
  'userManagement',
  'communications',
]

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Home />} />

        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />}>
            <Route
              index
              element={<DashboardHome />}
            />

            <Route element={<ProtectedRoute moduleKey="buildings" />}>
              <Route path="buildings" element={<BuildingsPage />} />
            </Route>

            <Route element={<ProtectedRoute moduleKey="units" />}>
              <Route path="units" element={<UnitsPage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="residents" />}>
              <Route path="residents" element={<ResidentsPage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="access" />}>
              <Route path="access" element={<AccessPage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="deliveries" />}>
              <Route path="deliveries" element={<DeliveriesPage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="incidents" />}>
              <Route path="incidents" element={<IncidentsPage />} />
            </Route>

            <Route element={<ProtectedRoute moduleKey="maintenance" />}>
              <Route path="maintenance" element={<MaintenancePage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="moves" />}>
              <Route path="moves" element={<MovesPage />} />
            </Route>
            <Route element={<ProtectedRoute moduleKey="userManagement" />}>
              <Route path="user-management" element={<UserManagementPage />} />
            </Route>

            <Route element={<ProtectedRoute moduleKey="generalSettings" />}>
              <Route path="settings/permissions" element={<RbacSettingsPage />} />
            </Route>

            {protectedModules
              .filter((moduleKey) => !['buildings', 'units', 'residents', 'access', 'deliveries', 'incidents', 'maintenance', 'moves', 'userManagement'].includes(moduleKey))
              .map((moduleKey) => (

                <Route
                  key={moduleKey}
                  element={
                    <ProtectedRoute moduleKey={moduleKey} />
                  }
                >
                  <Route
                    path={moduleKey}
                    element={<ModulePlaceholder />}
                  />
                </Route>
              ))}
          </Route>
        </Route>

        <Route
          path="*"
          element={<Navigate to="/" replace />}
        />
      </Routes>
    </AuthProvider>
  )
}

export default App