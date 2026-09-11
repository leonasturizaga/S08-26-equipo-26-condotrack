import { useTranslation } from '../i18n/i18n.js'

function Footer() {
  const { t } = useTranslation()
  const year = new Date().getFullYear()

  return (
    <footer className="public-footer">
      <div className="public-footer-inner">
        <span>
          © {year} CondoTrack
        </span>

        <div className="footer-links">
          <a href="#terms">
            {t('Terms & Conditions')}
          </a>

          <a href="#privacy">
            {t('Privacy Policy')}
          </a>
        </div>
      </div>
    </footer>
  )
}

export default Footer