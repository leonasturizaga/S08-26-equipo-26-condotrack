// ------------------ version 0 by module --------------------------
// import { useState } from 'react'

// const translations = {
//   en: { appName:'CondoTrack', tagline:'Centralized building and condominium operations, fully traceable.', dashboard:'Dashboard', buildings:'Buildings', residents:'Residents', access:'Access control', deliveries:'Deliveries', bookings:'Bookings', moves:'Move requests', incidents:'Incidents', maintenance:'Maintenance', notifications:'Notifications', administration:'Administration', signOut:'Sign out', projectStatus:'Project status', foundation:'The application foundation is ready for the first operational modules.', hierarchy:'Core data hierarchy', hierarchyValue:'Building → Unit → Resident', language:'Language', ready:'Foundation ready', unifiedLookup:'Unified unit lookup', unifiedLookupText:'One unit will aggregate residents, access history, deliveries, bookings, moves, incidents and maintenance.' },
//   es: { appName:'CondoTrack', tagline:'Operaciones centralizadas de edificios y condominios, totalmente trazables.', dashboard:'Panel', buildings:'Edificios', residents:'Residentes', access:'Control de acceso', deliveries:'Entregas', bookings:'Reservas', moves:'Solicitudes de mudanza', incidents:'Incidentes', maintenance:'Mantenimiento', notifications:'Notificaciones', administration:'Administración', signOut:'Cerrar sesión', projectStatus:'Estado del proyecto', foundation:'La base de la aplicación está lista para los primeros módulos operativos.', hierarchy:'Jerarquía de datos principal', hierarchyValue:'Edificio → Unidad → Residente', language:'Idioma', ready:'Base lista', unifiedLookup:'Consulta unificada de unidad', unifiedLookupText:'Una unidad agregará residentes, historial de accesos, entregas, reservas, mudanzas, incidentes y mantenimiento.' },
//   pt: { appName:'CondoTrack', tagline:'Operações centralizadas de edifícios e condomínios, totalmente rastreáveis.', dashboard:'Painel', buildings:'Edifícios', residents:'Moradores', access:'Controle de acesso', deliveries:'Entregas', bookings:'Reservas', moves:'Solicitações de mudança', incidents:'Incidentes', maintenance:'Manutenção', notifications:'Notificações', administration:'Administração', signOut:'Sair', projectStatus:'Status do projeto', foundation:'A base da aplicação está pronta para os primeiros módulos operacionais.', hierarchy:'Hierarquia central de dados', hierarchyValue:'Edifício → Unidade → Morador', language:'Idioma', ready:'Base pronta', unifiedLookup:'Consulta unificada da unidade', unifiedLookupText:'Uma unidade agregará moradores, histórico de acessos, entregas, reservas, mudanças, incidentes e manutenção.' },
// }

// export function useTranslation() {
//   const [language, setLanguage] = useState(() => localStorage.getItem('condotrack-language') || 'en')
//   const t = (key) => translations[language]?.[key] ?? translations.en[key] ?? key
//   const changeLanguage = () => {
//     const languages = ['en', 'es', 'pt']
//     const next = languages[(languages.indexOf(language) + 1) % languages.length]
//     localStorage.setItem('condotrack-language', next)
//     setLanguage(next)
//   }
//   return { t, language, changeLanguage }
// }

// export { translations }

import { useEffect, useState } from 'react'
import i18next from 'i18next'
import { initReactI18next, useTranslation as useI18nextTranslation } from 'react-i18next'

import en from './locales/en.json'
import es from './locales/es.json'

const supportedLanguages = ['en', 'es']

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

const t = (text) => {
return translate(text)
}

const changeLanguage = () => {
const currentIndex = supportedLanguages.indexOf(i18n.language)
const nextIndex = (currentIndex + 1) % supportedLanguages.length
const nextLanguage = supportedLanguages[nextIndex]

i18n.changeLanguage(nextLanguage)

localStorage.setItem('condotrack-language', nextLanguage)

}

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