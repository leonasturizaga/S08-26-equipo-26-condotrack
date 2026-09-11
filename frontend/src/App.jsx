// import Dashboard from './pages/Dashboard.jsx'

// function App() {
// return <Dashboard />
// }

// export default App


import { Navigate, Route, Routes } from 'react-router-dom'

import { AuthProvider } from './auth/AuthContext.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

import Dashboard from './pages/Dashboard.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import ModulePlaceholder from './pages/ModulePlaceholder.jsx'

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

        <Route
          path="/login"
          element={<Login />}
        />

        <Route element={<ProtectedRoute />}>
          <Route
            path="/dashboard"
            element={<Dashboard />}
          />

          {protectedModules.map((moduleKey) => (
            <Route
              key={moduleKey}
              element={
                <ProtectedRoute
                  moduleKey={moduleKey}
                />
              }
            >
              <Route
                path={`/dashboard/${moduleKey}`}
                element={<ModulePlaceholder />}
              />
            </Route>
          ))}
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