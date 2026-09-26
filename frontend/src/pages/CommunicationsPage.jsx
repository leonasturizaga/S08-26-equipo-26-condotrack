//----------------------- milestone 19 ----------------------
// import { useCallback, useEffect, useState } from 'react'

// import { sendCommunication } from '../api/communicationsApi.js'
// import { getBuildings } from '../api/buildingsApi.js'
// import { getUnits } from '../api/unitsApi.js'
// import Modal from '../components/Modal.jsx'
// import { useAuth } from '../auth/AuthContext.jsx'
// import { useTranslation } from '../i18n/i18n.js'

// const emptyForm = {
//   subject: '',
//   message: '',
//   audienceType: 'ALL_RESIDENTS',
//   buildingId: '',
//   unitId: '',
// }

// function CommunicationsPage() {
//   const { t } = useTranslation()
//   const { user } = useAuth()
//   const isAdministrator = String(user?.roles?.[0] || user?.role || '').trim().toUpperCase() === 'ADMINISTRATOR'

//   const [form, setForm] = useState(emptyForm)
//   const [buildings, setBuildings] = useState([])
//   const [units, setUnits] = useState([])
//   const [showForm, setShowForm] = useState(false)
//   const [loadingOptions, setLoadingOptions] = useState(false)
//   const [saving, setSaving] = useState(false)
//   const [error, setError] = useState('')
//   const [formError, setFormError] = useState('')
//   const [successMessage, setSuccessMessage] = useState('')

//   const loadOptions = useCallback(async () => {
//     if (!isAdministrator) return

//     setLoadingOptions(true)
//     setError('')

//     try {
//       const [buildingResponse, unitResponse] = await Promise.all([
//         getBuildings(0, 100),
//         getUnits(0, 100),
//       ])
//       setBuildings((Array.isArray(buildingResponse?.content) ? buildingResponse.content : []).filter((item) => item.active !== false))
//       setUnits((Array.isArray(unitResponse?.content) ? unitResponse.content : []).filter((item) => item.active !== false))
//     } catch (requestError) {
//       setError(requestError.message || t('Unable to load communication options.'))
//     } finally {
//       setLoadingOptions(false)
//     }
//   }, [isAdministrator, t])

//   useEffect(() => {
//     loadOptions()
//   }, [loadOptions])

//   const openComposer = () => {
//     setForm(emptyForm)
//     setFormError('')
//     setSuccessMessage('')
//     setShowForm(true)
//   }

//   const handleChange = (event) => {
//     const { name, value } = event.target
//     setForm((current) => ({
//       ...current,
//       [name]: value,
//       ...(name === 'audienceType' && value === 'ALL_RESIDENTS'
//         ? { buildingId: '', unitId: '' }
//         : {}),
//       ...(name === 'audienceType' && value === 'BUILDING_RESIDENTS'
//         ? { unitId: '' }
//         : {}),
//     }))
//   }

//   const handleSubmit = async (event) => {
//     event.preventDefault()
//     setSaving(true)
//     setFormError('')
//     setSuccessMessage('')

//     const payload = {
//       subject: form.subject.trim(),
//       message: form.message.trim(),
//       audienceType: form.audienceType,
//       buildingId: form.audienceType === 'BUILDING_RESIDENTS' ? form.buildingId || null : null,
//       unitId: form.audienceType === 'UNIT_RESIDENTS' ? form.unitId || null : null,
//     }

//     try {
//       const response = await sendCommunication(payload)
//       setSuccessMessage(
//         `${t('Communication sent successfully.')} ${response?.recipientCount ?? 0} ${t('recipients notified.')}`,
//       )
//       setShowForm(false)
//       setForm(emptyForm)
//     } catch (requestError) {
//       setFormError(requestError.message || t('Unable to send communication.'))
//     } finally {
//       setSaving(false)
//     }
//   }

//   if (!isAdministrator) {
//     return null
//   }

//   return (
//     <section className="module-page">
//       <div className="module-page-header">
//         <div>
//           <p className="eyebrow">{t('COMMUNICATIONS')}</p>
//           <h2>{t('Communications')}</h2>
//           <p className="muted">
//             {t('Send in-app announcements to residents and owners.')}
//           </p>
//         </div>

//         <button className="button button-primary" type="button" onClick={openComposer}>
//           + {t('New communication')}
//         </button>
//       </div>

//       {successMessage && (
//         <div className="feedback feedback-success" role="status">
//           {successMessage}
//         </div>
//       )}

//       {error && (
//         <div className="feedback feedback-error" role="alert">
//           {error}
//         </div>
//       )}

//       <article className="panel">
//         <p className="eyebrow">{t('IN-APP COMMUNICATIONS')}</p>
//         <h3>{t('Send targeted announcements')}</h3>
//         <p className="panel-copy">
//           {t('Choose all residents and owners, one building, or one unit. Email delivery will be added later.')}
//         </p>

//         <div className="dashboard-grid compact-grid">
//           <article className="metric-card">
//             <span>{t('Audience')}</span>
//             <strong>{t('Residents + Owners')}</strong>
//           </article>
//           <article className="metric-card">
//             <span>{t('Channel')}</span>
//             <strong>{t('In-app')}</strong>
//           </article>
//           <article className="metric-card">
//             <span>{t('Delivery')}</span>
//             <strong>{t('Immediate')}</strong>
//           </article>
//         </div>
//       </article>

//       <Modal
//         open={showForm && isAdministrator}
//         onClose={() => !saving && setShowForm(false)}
//         eyebrow={t('NEW COMMUNICATION')}
//         title={t('Send communication')}
//         size="large"
//         closeOnBackdrop={!saving}
//       >
//         {formError && (
//           <div className="feedback feedback-error modal-feedback" role="alert">
//             {formError}
//           </div>
//         )}

//         <form className="entity-form" onSubmit={handleSubmit}>
//           <label className="form-field">
//             <span>{t('Subject')}</span>
//             <input
//               name="subject"
//               value={form.subject}
//               onChange={handleChange}
//               required
//               maxLength={255}
//             />
//           </label>

//           <label className="form-field">
//             <span>{t('Message')}</span>
//             <textarea
//               name="message"
//               value={form.message}
//               onChange={handleChange}
//               required
//               maxLength={10000}
//               rows={7}
//             />
//           </label>

//           <div className="form-grid-2">
//             <label className="form-field">
//               <span>{t('Audience')}</span>
//               <select name="audienceType" value={form.audienceType} onChange={handleChange}>
//                 <option value="ALL_RESIDENTS">{t('All residents and owners')}</option>
//                 <option value="BUILDING_RESIDENTS">{t('Residents and owners in a building')}</option>
//                 <option value="UNIT_RESIDENTS">{t('Residents and owners in a unit')}</option>
//               </select>
//             </label>

//             {form.audienceType === 'BUILDING_RESIDENTS' && (
//               <label className="form-field">
//                 <span>{t('Building')}</span>
//                 <select name="buildingId" value={form.buildingId} onChange={handleChange} required>
//                   <option value="">{t('Select building')}</option>
//                   {buildings.map((building) => (
//                     <option key={building.id} value={building.id}>
//                       {building.code} — {building.name}
//                     </option>
//                   ))}
//                 </select>
//               </label>
//             )}

//             {form.audienceType === 'UNIT_RESIDENTS' && (
//               <label className="form-field">
//                 <span>{t('Unit')}</span>
//                 <select name="unitId" value={form.unitId} onChange={handleChange} required>
//                   <option value="">{t('Select unit')}</option>
//                   {units.map((unit) => (
//                     <option key={unit.id} value={unit.id}>
//                       {unit.unitNumber}{unit.buildingCode ? ` — ${unit.buildingCode}` : ''}
//                     </option>
//                   ))}
//                 </select>
//               </label>
//             )}
//           </div>

//           {loadingOptions && (
//             <div className="feedback feedback-info">{t('Loading communication options...')}</div>
//           )}

//           <div className="form-actions">
//             <button className="button button-secondary" type="button" onClick={() => setShowForm(false)} disabled={saving}>
//               {t('Cancel')}
//             </button>
//             <button className="button button-primary" type="submit" disabled={saving}>
//               {saving ? t('Sending...') : t('Send communication')}
//             </button>
//           </div>
//         </form>
//       </Modal>
//     </section>
//   )
// }

// export default CommunicationsPage


//----------------------- milestone 19.1 ----------------------
import { useCallback, useEffect, useState } from 'react'

import { getCommunication, getCommunications, sendCommunication } from '../api/communicationsApi.js'
import { getBuildings } from '../api/buildingsApi.js'
import { getUnits } from '../api/unitsApi.js'
import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from '../components/Icon.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 20
const emptyForm = {
  subject: '',
  message: '',
  audienceType: 'ALL_RESIDENTS',
  buildingId: '',
  unitId: '',
}

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

function audienceLabel(t, audienceType) {
  const labels = {
    ALL_RESIDENTS: 'All residents and owners',
    BUILDING_RESIDENTS: 'Building residents and owners',
    UNIT_RESIDENTS: 'Unit residents and owners',
  }
  return t(labels[audienceType] || audienceType || 'Unknown')
}

function CommunicationsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const isAdministrator = String(user?.roles?.[0] || user?.role || '').trim().toUpperCase() === 'ADMINISTRATOR'

  const [form, setForm] = useState(emptyForm)
  const [buildings, setBuildings] = useState([])
  const [units, setUnits] = useState([])
  const [communications, setCommunications] = useState([])
  const [selectedCommunication, setSelectedCommunication] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [showForm, setShowForm] = useState(false)
  const [loadingOptions, setLoadingOptions] = useState(false)
  const [loadingCommunications, setLoadingCommunications] = useState(true)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const loadOptions = useCallback(async () => {
    if (!isAdministrator) return
    setLoadingOptions(true)
    try {
      const [buildingResponse, unitResponse] = await Promise.all([
        getBuildings(0, 100),
        getUnits(0, 100),
      ])
      setBuildings((Array.isArray(buildingResponse?.content) ? buildingResponse.content : []).filter((item) => item.active !== false))
      setUnits((Array.isArray(unitResponse?.content) ? unitResponse.content : []).filter((item) => item.active !== false))
    } catch (requestError) {
      setError(requestError.message || t('Unable to load communication options.'))
    } finally {
      setLoadingOptions(false)
    }
  }, [isAdministrator, t])

  const loadCommunications = useCallback(async (pageToLoad = 0) => {
    if (!isAdministrator) return
    setLoadingCommunications(true)
    setError('')
    try {
      const response = await getCommunications(pageToLoad, PAGE_SIZE)
      setCommunications(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load sent communications.'))
    } finally {
      setLoadingCommunications(false)
    }
  }, [isAdministrator, t])

  useEffect(() => { loadOptions() }, [loadOptions])
  useEffect(() => { loadCommunications(0) }, [loadCommunications])

  const openComposer = () => {
    setForm(emptyForm)
    setFormError('')
    setSuccessMessage('')
    setShowForm(true)
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'audienceType' && value === 'ALL_RESIDENTS' ? { buildingId: '', unitId: '' } : {}),
      ...(name === 'audienceType' && value === 'BUILDING_RESIDENTS' ? { unitId: '' } : {}),
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccessMessage('')
    const payload = {
      subject: form.subject.trim(),
      message: form.message.trim(),
      audienceType: form.audienceType,
      buildingId: form.audienceType === 'BUILDING_RESIDENTS' ? form.buildingId || null : null,
      unitId: form.audienceType === 'UNIT_RESIDENTS' ? form.unitId || null : null,
    }
    try {
      const response = await sendCommunication(payload)
      setSuccessMessage(`${t('Communication sent successfully.')} ${response?.recipientCount ?? 0} ${t('recipients notified.')}`)
      setShowForm(false)
      setForm(emptyForm)
      await loadCommunications(0)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to send communication.'))
    } finally {
      setSaving(false)
    }
  }

  const openCommunication = async (communication) => {
    setSelectedCommunication(communication)
    setLoadingDetail(true)
    try {
      const response = await getCommunication(communication.id)
      setSelectedCommunication(response)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load communication details.'))
    } finally {
      setLoadingDetail(false)
    }
  }

  if (!isAdministrator) return null

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('COMMUNICATIONS')}</p>
          <h2>{t('Communications')}</h2>
          <p className="muted">{t('Send in-app announcements and review sent communication history.')}</p>
        </div>
        <button className="button button-primary" type="button" onClick={openComposer}>
          <Icon name="plus" size={16} />
          {t('New communication')}
        </button>
      </div>

      {successMessage && <div className="feedback feedback-success" role="status">{successMessage}</div>}
      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">{t('SENT COMMUNICATIONS')}</p>
            <h3>{totalElements} {t('communications')}</h3>
          </div>
          <button className="button button-secondary button-small" type="button" onClick={() => loadCommunications(page)} disabled={loadingCommunications}>↻ {t('Refresh')}</button>
        </div>

        {loadingCommunications ? (
          <div className="feedback feedback-info">{t('Loading...')}</div>
        ) : communications.length === 0 ? (
          <div className="empty-state">{t('No sent communications found.')}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr><th>{t('Subject')}</th><th>{t('Audience')}</th><th>{t('Sent')}</th><th>{t('Read')}</th><th>{t('Unread')}</th><th>{t('Actions')}</th></tr></thead>
              <tbody>
                {communications.map((communication) => (
                  <tr key={communication.id}>
                    <td><strong>{communication.subject}</strong><div className="table-secondary-text">{communication.message}</div></td>
                    <td>{audienceLabel(t, communication.audienceType)}{communication.buildingCode ? ` — ${communication.buildingCode}` : ''}{communication.unitNumber ? ` / ${communication.unitNumber}` : ''}</td>
                    <td>{formatDateTime(communication.sentAt)}</td>
                    <td>{communication.readCount} / {communication.recipientCount}</td>
                    <td>{communication.unreadCount}</td>
                    <td><TableActions moreLabel={t('More')}><TableAction icon="eye" label={t('View')} variant="view" onClick={() => openCommunication(communication)} /></TableActions></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button className="button button-secondary button-small" type="button" onClick={() => loadCommunications(page - 1)} disabled={page <= 0 || loadingCommunications}>{t('Previous')}</button>
            <span>{t('Page')} {page + 1} {t('of')} {totalPages}</span>
            <button className="button button-secondary button-small" type="button" onClick={() => loadCommunications(page + 1)} disabled={page + 1 >= totalPages || loadingCommunications}>{t('Next')}</button>
          </div>
        )}
      </article>

      <Modal open={showForm} onClose={() => !saving && setShowForm(false)} eyebrow={t('NEW COMMUNICATION')} title={t('Send communication')} size="large" closeOnBackdrop={!saving}>
        {formError && <div className="feedback feedback-error modal-feedback" role="alert">{formError}</div>}
        <form className="entity-form" onSubmit={handleSubmit}>
          <label className="form-field"><span>{t('Subject')}</span><input name="subject" value={form.subject} onChange={handleChange} required maxLength={255} /></label>
          <label className="form-field"><span>{t('Message')}</span><textarea name="message" value={form.message} onChange={handleChange} required maxLength={10000} rows={7} /></label>
          <div className="form-grid-2">
            <label className="form-field"><span>{t('Audience')}</span><select name="audienceType" value={form.audienceType} onChange={handleChange}><option value="ALL_RESIDENTS">{t('All residents and owners')}</option><option value="BUILDING_RESIDENTS">{t('Residents and owners in a building')}</option><option value="UNIT_RESIDENTS">{t('Residents and owners in a unit')}</option></select></label>
            {form.audienceType === 'BUILDING_RESIDENTS' && <label className="form-field"><span>{t('Building')}</span><select name="buildingId" value={form.buildingId} onChange={handleChange} required><option value="">{t('Select building')}</option>{buildings.map((building) => <option key={building.id} value={building.id}>{building.code} — {building.name}</option>)}</select></label>}
            {form.audienceType === 'UNIT_RESIDENTS' && <label className="form-field"><span>{t('Unit')}</span><select name="unitId" value={form.unitId} onChange={handleChange} required><option value="">{t('Select unit')}</option>{units.map((unit) => <option key={unit.id} value={unit.id}>{unit.unitNumber}{unit.buildingCode ? ` — ${unit.buildingCode}` : ''}</option>)}</select></label>}
          </div>
          {loadingOptions && <div className="feedback feedback-info">{t('Loading communication options...')}</div>}
          <div className="form-actions"><button className="button button-secondary" type="button" onClick={() => setShowForm(false)} disabled={saving}>{t('Cancel')}</button><button className="button button-primary" type="submit" disabled={saving}>{saving ? t('Sending...') : t('Send communication')}</button></div>
        </form>
      </Modal>

      <Modal open={Boolean(selectedCommunication)} onClose={() => setSelectedCommunication(null)} eyebrow={t('SENT COMMUNICATION')} title={selectedCommunication?.subject || t('Communication')} size="large">
        {loadingDetail ? <div className="feedback feedback-info">{t('Loading...')}</div> : selectedCommunication && (
          <div className="detail-grid">
            <div><span>{t('Audience')}</span><strong>{audienceLabel(t, selectedCommunication.audienceType)}</strong></div>
            <div><span>{t('Sent')}</span><strong>{formatDateTime(selectedCommunication.sentAt)}</strong></div>
            <div><span>{t('Recipients')}</span><strong>{selectedCommunication.recipientCount}</strong></div>
            <div><span>{t('Read')}</span><strong>{selectedCommunication.readCount}</strong></div>
            <div><span>{t('Unread')}</span><strong>{selectedCommunication.unreadCount}</strong></div>
            <div><span>{t('Sent by')}</span><strong>{selectedCommunication.sentByName || '—'}</strong></div>
            <div className="detail-grid-wide"><span>{t('Message')}</span><strong>{selectedCommunication.message}</strong></div>
          </div>
        )}
      </Modal>
    </section>
  )
}

export default CommunicationsPage