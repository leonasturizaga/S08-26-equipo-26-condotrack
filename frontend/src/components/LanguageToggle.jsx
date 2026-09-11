// export default LanguageToggle

import { useTranslation } from '../i18n/i18n.js'

function LanguageToggle() {
  const { language, changeLanguage } = useTranslation()

  return (
    <button
      className="language-button"
      onClick={changeLanguage}
      title="Change language"
      aria-label="Change language"
      type="button"
    >
      <span>◌</span>
      <span>{language.toUpperCase()}</span>
    </button>
  )
}

export default LanguageToggle