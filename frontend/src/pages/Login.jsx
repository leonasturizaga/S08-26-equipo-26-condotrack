//----------------- milestone 0 ----------------------
// import { useState } from 'react'
// import {
//   Link,
//   Navigate,
//   useLocation,
//   useNavigate,
// } from 'react-router-dom'

// import Navbar from '../components/Navbar.jsx'
// import Footer from '../components/Footer.jsx'
// import { useAuth } from '../auth/AuthContext.jsx'
// import { useTranslation } from '../i18n/i18n.js'

// function Login() {
//   const { t } = useTranslation()
//   const { isAuthenticated, login } = useAuth()

//   const navigate = useNavigate()
//   const location = useLocation()

//   const [identifier, setIdentifier] = useState('')
//   const [password, setPassword] = useState('')
//   const [error, setError] = useState('')

//   if (isAuthenticated) {
//     return <Navigate to="/dashboard" replace />
//   }

//   const destination =
//     location.state?.from || '/dashboard'

//   const handleSubmit = (event) => {
//     event.preventDefault()
//     setError('')

//     const result = login(identifier, password)

//     if (!result.success) {
//       setError(
//         t('Invalid username or password.')
//       )
//       return
//     }

//     navigate(destination, { replace: true })
//   }

//   return (
//    //  <div className="public-page">
//    <div className="login-page">
//       <Navbar />

//       <main className="login-page">
//         <section className="login-card">
//           <p className="eyebrow">
//             {t('CONDO OPERATIONS')}
//           </p>

//           <h1>{t('Welcome back')}</h1>

//           <p className="muted">
//             {t(
//               'Sign in to continue to your CondoTrack workspace.'
//             )}
//           </p>

//           <form
//             className="login-form"
//             onSubmit={handleSubmit}
//           >
//             <label>
//               <span>{t('Username or email')}</span>

//               <input
//                 value={identifier}
//                 onChange={(event) =>
//                   setIdentifier(event.target.value)
//                 }
//                 autoComplete="username"
//                 required
//               />
//             </label>

//             <label>
//               <span>{t('Password')}</span>

//               <input
//                 type="password"
//                 value={password}
//                 onChange={(event) =>
//                   setPassword(event.target.value)
//                 }
//                 autoComplete="current-password"
//                 required
//               />
//             </label>

//             {error && (
//               <div className="form-error">
//                 {error}
//               </div>
//             )}

//             <button
//               className="button button-primary button-full"
//               type="submit"
//             >
//               {t('Log In')}
//             </button>
//           </form>

//           <div className="demo-credentials">
//             <p className="eyebrow">
//               {t('Demo accounts')}
//             </p>

//             <p>
//               {t(
//                 'Use the mock accounts in src/auth/mockUsers.js for frontend testing.'
//               )}
//             </p>
//           </div>

//           <Link className="text-link" to="/">
//             {t('Back to home')}
//           </Link>
//         </section>
//       </main>

//       <Footer />
//     </div>
//   )
// }

// export default Login


//----------------- milestone 1 ----------------------
import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

function Login() {
  const { t } = useTranslation()
  const {
    isAuthenticated,
    isInitializing,
    login,
  } = useAuth()

  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (isInitializing) {
    return (
      <main className="login-loading" role="status">
        {t('Loading...')}
      </main>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const destination = location.state?.from || '/dashboard'

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    const result = await login(email, password)

    setIsSubmitting(false)

    if (!result.success) {
      if (result.error?.status === 0) {
        setError(t('Unable to connect to the CondoTrack server.'))
      } else if (result.error?.status === 400) {
        setError(t('Please enter a valid email and password.'))
      } else {
        setError(t('Invalid email or password.'))
      }
      return
    }

    navigate(destination, { replace: true })
  }

  return (
    <div className="login-page-shell">
      <header className="login-header">
        <Link className="login-brand" to="/" aria-label={t('CondoTrack')}>
          <span className="login-brand-mark">CT</span>
          <strong>{t('CondoTrack')}</strong>
        </Link>
      </header>

      <main className="login-main">
        <section className="login-content" aria-labelledby="login-title">
          <div className="login-heading">
            <p className="login-eyebrow">{t('CONDO OPERATIONS')}</p>
            <h1 id="login-title">{t('Welcome back')}</h1>
          </div>

          <section className="login-card">
            <form className="login-form" onSubmit={handleSubmit}>
              <label>
                <span>{t('Email')}</span>
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  autoComplete="email"
                  placeholder={t('Email placeholder')}
                  required
                  disabled={isSubmitting}
                />
              </label>

              <label>
                <span>{t('Password')}</span>
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete="current-password"
                  placeholder={t('Password placeholder')}
                  required
                  disabled={isSubmitting}
                />
              </label>

              {error && (
                <div className="form-error" role="alert">
                  {error}
                </div>
              )}

              <button
                className="button button-primary button-full login-submit"
                type="submit"
                disabled={isSubmitting}
              >
                {isSubmitting ? t('Signing in...') : t('Log In')}
              </button>
            </form>

            <div className="login-secondary-actions">
              <button className="text-button" type="button" disabled>
                {t('Forgot Password?')}
              </button>

              <Link className="text-link" to="/">
                {t('Back to home')}
              </Link>
            </div>
          </section>
        </section>
      </main>
    </div>
  )
}

export default Login