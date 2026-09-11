import { useTranslation } from '../i18n/i18n.js'

function LanguageToggle() {
  const { language, changeLanguage } = useTranslation()

  return (
    <button
      className="language-button"
      onClick={changeLanguage}
      title="Change language"
      type="button"
      aria-label="Change language"
    >
      <span>◌</span>
      <span>{language.toUpperCase()}</span>
    </button>
  )
}

export default LanguageToggle