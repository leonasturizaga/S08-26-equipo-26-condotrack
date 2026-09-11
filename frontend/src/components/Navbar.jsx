import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'
import LanguageToggle from './LanguageToggle.jsx'

function Navbar() {
  const { t } = useTranslation()
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  const handleAuthAction = () => {
    if (isAuthenticated) {
      logout()
      navigate('/')
      return
    }

    navigate('/login')
  }

  return (
    <header className="public-navbar">
      <div className="public-navbar-inner">
        <Link className="public-brand" to="/">
          <span className="public-brand-mark">CT</span>

          <span>
            <strong>{t('CondoTrack')}</strong>
            <small>{t('Property operations')}</small>
          </span>
        </Link>

        <div className="public-nav-actions">
          <LanguageToggle />

          <button
            className="button button-outline"
            onClick={handleAuthAction}
            type="button"
          >
            {isAuthenticated
              ? t('Sign Out')
              : t('Log In')}
          </button>
        </div>
      </div>
    </header>
  )
}

export default Navbar