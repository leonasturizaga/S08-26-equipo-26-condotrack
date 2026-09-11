import { useState } from 'react'
import {
  Link,
  Navigate,
  useLocation,
  useNavigate,
} from 'react-router-dom'

import Navbar from '../components/Navbar.jsx'
import Footer from '../components/Footer.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

function Login() {
  const { t } = useTranslation()
  const { isAuthenticated, login } = useAuth()

  const navigate = useNavigate()
  const location = useLocation()

  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const destination =
    location.state?.from || '/dashboard'

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')

    const result = login(identifier, password)

    if (!result.success) {
      setError(
        t('Invalid username or password.')
      )
      return
    }

    navigate(destination, { replace: true })
  }

  return (
    <div className="public-page">
      <Navbar />

      <main className="login-page">
        <section className="login-card">
          <p className="eyebrow">
            {t('CONDO OPERATIONS')}
          </p>

          <h1>{t('Welcome back')}</h1>

          <p className="muted">
            {t(
              'Sign in to continue to your CondoTrack workspace.'
            )}
          </p>

          <form
            className="login-form"
            onSubmit={handleSubmit}
          >
            <label>
              <span>{t('Username or email')}</span>

              <input
                value={identifier}
                onChange={(event) =>
                  setIdentifier(event.target.value)
                }
                autoComplete="username"
                required
              />
            </label>

            <label>
              <span>{t('Password')}</span>

              <input
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                autoComplete="current-password"
                required
              />
            </label>

            {error && (
              <div className="form-error">
                {error}
              </div>
            )}

            <button
              className="button button-primary button-full"
              type="submit"
            >
              {t('Log In')}
            </button>
          </form>

          <div className="demo-credentials">
            <p className="eyebrow">
              {t('Demo accounts')}
            </p>

            <p>
              {t(
                'Use the mock accounts in src/auth/mockUsers.js for frontend testing.'
              )}
            </p>
          </div>

          <Link className="text-link" to="/">
            {t('Back to home')}
          </Link>
        </section>
      </main>

      <Footer />
    </div>
  )
}

export default Login