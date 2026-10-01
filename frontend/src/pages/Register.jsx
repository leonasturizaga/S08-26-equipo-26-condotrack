import { useState } from 'react'
import {
  Link,
  Navigate,
  useNavigate,
  NavLink, Outlet
} from 'react-router-dom'

import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'
import Icon from '../components/Icon.jsx'
import loginBackground from '../assets/AlluraCity.png'
import { registerUser } from '../api/authApi.js'

function Register() {
  const { t } = useTranslation()

  const {
    isAuthenticated,
    isInitializing,
  } = useAuth()

  const navigate = useNavigate()

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
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

  const handleSubmit = async (event) => {
    event.preventDefault()

    setError('')
    setSuccess('')

    setIsSubmitting(true)

    try {
      await registerUser({
        email,
        password,
        firstName,
        lastName,
        phone,
      })

      setSuccess(
        t(
          'Your account was created successfully. You can now sign in.',
        ),
      )

      window.setTimeout(() => {
        navigate('/login', {
          replace: true,
          state: {
            registered: true,
          },
        })
      }, 1200)
    } catch (requestError) {
      if (requestError?.status === 409) {
        setError(
          t('An account with this email already exists.'),
        )
      } else if (requestError?.status === 400) {
        setError(
          t('Please check the information entered and try again.'),
        )
      } else if (requestError?.status === 0) {
        setError(
          t('Unable to connect to the CondoTrack server.'),
        )
      } else {
        setError(
          requestError?.message
            || t('Unable to create the account.'),
        )
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div
      className="auth-page-shell auth-register-page"
      style={{
        backgroundImage: `url(${loginBackground})`,
      }}
    >
      <div className="auth-page-overlay" />

      <header className="auth-header">
        <Link
          className="auth-brand"
          to="/"
          aria-label={t('CondoTrack')}
        >
            <NavLink to="/" end>
               <img src="logo.svg" alt="CondoTrack" width="40" height="40"></img>
            </NavLink>

          <strong>{t('CondoTrack')}</strong>
        </Link>
      </header>

      <main className="auth-main">
        <section
          className="auth-content"
          aria-labelledby="register-title"
        >
          <div className="auth-heading">
            <p className="auth-eyebrow">
              {t('CONDO OPERATIONS')}
            </p>

            <h1 id="register-title">
              {t('Create your account')}
            </h1>
          </div>

          <section className="auth-card">
            <div className="auth-intro">
              {t(
                'Centralized building and condominium management with full traceability.',
              )}
            </div>

            <form
              className="auth-form"
              onSubmit={handleSubmit}
            >
              <div className="auth-form-row">
                <label>
                  <span>{t('First name')}</span>

                  <input
                    type="text"
                    value={firstName}
                    onChange={(event) =>
                      setFirstName(event.target.value)
                    }
                    autoComplete="given-name"
                    placeholder={t('First name')}
                    required
                    disabled={isSubmitting}
                  />
                </label>

                <label>
                  <span>{t('Last name')}</span>

                  <input
                    type="text"
                    value={lastName}
                    onChange={(event) =>
                      setLastName(event.target.value)
                    }
                    autoComplete="family-name"
                    placeholder={t('Last name')}
                    required
                    disabled={isSubmitting}
                  />
                </label>
              </div>

              <label>
                <span>{t('Email')}</span>

                <input
                  type="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                  autoComplete="email"
                  placeholder={t('Email placeholder')}
                  required
                  disabled={isSubmitting}
                />
              </label>

              <label>
                <span>{t('Phone')}</span>

                <input
                  type="tel"
                  value={phone}
                  onChange={(event) =>
                    setPhone(event.target.value)
                  }
                  autoComplete="tel"
                  placeholder={t('Phone placeholder')}
                  disabled={isSubmitting}
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
                  autoComplete="new-password"
                  placeholder={t('Minimum 8 characters')}
                  minLength={8}
                  required
                  disabled={isSubmitting}
                />
              </label>

              {error && (
                <div
                  className="form-error"
                  role="alert"
                >
                  {error}
                </div>
              )}

              {success && (
                <div
                  className="form-success"
                  role="status"
                >
                  {success}
                </div>
              )}

              <button
                className="button button-primary button-full auth-submit"
                type="submit"
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? t('Creating account...')
                  : t('Create Account')}
              </button>
            </form>

            <div className="auth-secondary-actions">
              <span>
                {t('Already have an account?')}
              </span>

              <Link
                className="text-link"
                to="/login"
              >
                {t('Log In')}
              </Link>

              <Link
                className="text-link"
                to="/"
              >
                {t('Back to home')}
              </Link>
            </div>
          </section>
        </section>
      </main>
    </div>
  )
}

export default Register