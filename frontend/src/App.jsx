//------------------ milestone 1 ------------------
// import { Navigate, Route, Routes } from 'react-router-dom'

// import { AuthProvider } from './auth/AuthContext.jsx'
// import ProtectedRoute from './components/ProtectedRoute.jsx'

// import Dashboard from './pages/Dashboard.jsx'
// import DashboardHome from './pages/DashboardHome.jsx'
// import Home from './pages/Home.jsx'
// import Login from './pages/Login.jsx'
// import ModulePlaceholder from './pages/ModulePlaceholder.jsx'

// const protectedModules = [
//   'buildings',
//   'units',
//   'residents',
//   'access',
//   'deliveries',
//   'bookings',
//   'incidents',
//   'maintenance',
//   'moves',
//   'notifications',
//   'reports',
//   'buildingConfig',
//   'userManagement',
//   'communications',
// ]

// function App() {
//   return (
//     <AuthProvider>
//       <Routes>
//         <Route path="/" element={<Home />} />

//         <Route path="/login" element={<Login />} />

//         <Route element={<ProtectedRoute />}>
//           <Route path="/dashboard" element={<Dashboard />}>
//             <Route
//               index
//               element={<DashboardHome />}
//             />

//             {protectedModules.map((moduleKey) => (
//               <Route
//                 key={moduleKey}
//                 element={
//                   <ProtectedRoute moduleKey={moduleKey} />
//                 }
//               >
//                 <Route
//                   path={moduleKey}
//                   element={<ModulePlaceholder />}
//                 />
//               </Route>
//             ))}
//           </Route>
//         </Route>

//         <Route
//           path="*"
//           element={<Navigate to="/" replace />}
//         />
//       </Routes>
//     </AuthProvider>
//   )
// }

// export default App


//------------------ milestone 2 ------------------
import { Navigate, Route, Routes } from 'react-router-dom'

import { AuthProvider } from './auth/AuthContext.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

import Dashboard from './pages/Dashboard.jsx'
import BuildingsPage from './pages/BuildingsPage.jsx'
import UnitsPage from './pages/UnitsPage.jsx'
import DashboardHome from './pages/DashboardHome.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import ModulePlaceholder from './pages/ModulePlaceholder.jsx'
import Modal from './components/Modal.jsx'

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

            {protectedModules
              .filter((moduleKey) => !['buildings', 'units'].includes(moduleKey))
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