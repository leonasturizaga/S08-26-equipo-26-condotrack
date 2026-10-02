import { useCallback,  useEffect, useState } from 'react'
import i18next from 'i18next'
import { initReactI18next, useTranslation as useI18nextTranslation } from 'react-i18next'

import en from './locales/en.json'
import es from './locales/es.json'
import pt from './locales/pt.json'

const supportedLanguages = ['en', 'es', 'pt']

const savedLanguage = localStorage.getItem('condotrack-language')

const initialLanguage = supportedLanguages.includes(savedLanguage)
? savedLanguage
: 'en'

i18next
.use(initReactI18next)
.init({
resources: {
en: {
translation: en
},
es: {
translation: es
},
pt: {
translation: pt
}
},

lng: initialLanguage,

fallbackLng: 'en',

supportedLngs: supportedLanguages,

interpolation: {
  escapeValue: false
}

})

export function useTranslation() {
const { t: translate, i18n } = useI18nextTranslation()
const [, forceUpdate] = useState(0)

useEffect(() => {
const handleLanguageChanged = () => {
forceUpdate((value) => value + 1)
}

i18n.on('languageChanged', handleLanguageChanged)

return () => {
  i18n.off('languageChanged', handleLanguageChanged)
}

}, [i18n])

const t = useCallback((text) => {
return translate(text)
}, [translate])

const changeLanguage = useCallback(() => {
const currentIndex = supportedLanguages.indexOf(i18n.language)
const nextIndex = (currentIndex + 1) % supportedLanguages.length
const nextLanguage = supportedLanguages[nextIndex]

i18n.changeLanguage(nextLanguage)

localStorage.setItem('condotrack-language', nextLanguage)

}, [i18n])

return {
t,
language: i18n.language,
changeLanguage
}
}

export function getSupportedLanguages() {
return supportedLanguages
}

export default i18next