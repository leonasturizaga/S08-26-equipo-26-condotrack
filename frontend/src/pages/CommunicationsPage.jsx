import { useCallback, useEffect, useState } from 'react'

import { sendCommunication } from '../api/communicationsApi.js'
import { getBuildings } from '../api/buildingsApi.js'
import { getUnits } from '../api/unitsApi.js'
import Modal from '../components/Modal.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

const emptyForm = {
  subject: '',
  message: '',
  audienceType: 'ALL_RESIDENTS',
  buildingId: '',
  unitId: '',
}

function CommunicationsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const isAdministrator = String(user?.roles?.[0] || user?.role || '').trim().toUpperCase() === 'ADMINISTRATOR'

  const [form, setForm] = useState(emptyForm)
  const [buildings, setBuildings] = useState([])
  const [units, setUnits] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [loadingOptions, setLoadingOptions] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const loadOptions = useCallback(async () => {
    if (!isAdministrator) return

    setLoadingOptions(true)
    setError('')

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

  useEffect(() => {
    loadOptions()
  }, [loadOptions])

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
      ...(name === 'audienceType' && value === 'ALL_RESIDENTS'
        ? { buildingId: '', unitId: '' }
        : {}),
      ...(name === 'audienceType' && value === 'BUILDING_RESIDENTS'
        ? { unitId: '' }
        : {}),
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
      setSuccessMessage(
        `${t('Communication sent successfully.')} ${response?.recipientCount ?? 0} ${t('recipients notified.')}`,
      )
      setShowForm(false)
      setForm(emptyForm)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to send communication.'))
    } finally {
      setSaving(false)
    }
  }

  if (!isAdministrator) {
    return null
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('COMMUNICATIONS')}</p>
          <h2>{t('Communications')}</h2>
          <p className="muted">
            {t('Send in-app announcements to residents and owners.')}
          </p>
        </div>

        <button className="button button-primary" type="button" onClick={openComposer}>
          + {t('New communication')}
        </button>
      </div>

      {successMessage && (
        <div className="feedback feedback-success" role="status">
          {successMessage}
        </div>
      )}

      {error && (
        <div className="feedback feedback-error" role="alert">
          {error}
        </div>
      )}

      <article className="panel">
        <p className="eyebrow">{t('IN-APP COMMUNICATIONS')}</p>
        <h3>{t('Send targeted announcements')}</h3>
        <p className="panel-copy">
          {t('Choose all residents and owners, one building, or one unit. Email delivery will be added later.')}
        </p>

        <div className="dashboard-grid compact-grid">
          <article className="metric-card">
            <span>{t('Audience')}</span>
            <strong>{t('Residents + Owners')}</strong>
          </article>
          <article className="metric-card">
            <span>{t('Channel')}</span>
            <strong>{t('In-app')}</strong>
          </article>
          <article className="metric-card">
            <span>{t('Delivery')}</span>
            <strong>{t('Immediate')}</strong>
          </article>
        </div>
      </article>

      <Modal
        open={showForm && isAdministrator}
        onClose={() => !saving && setShowForm(false)}
        eyebrow={t('NEW COMMUNICATION')}
        title={t('Send communication')}
        size="large"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div className="feedback feedback-error modal-feedback" role="alert">
            {formError}
          </div>
        )}

        <form className="entity-form" onSubmit={handleSubmit}>
          <label className="form-field">
            <span>{t('Subject')}</span>
            <input
              name="subject"
              value={form.subject}
              onChange={handleChange}
              required
              maxLength={255}
            />
          </label>

          <label className="form-field">
            <span>{t('Message')}</span>
            <textarea
              name="message"
              value={form.message}
              onChange={handleChange}
              required
              maxLength={10000}
              rows={7}
            />
          </label>

          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('Audience')}</span>
              <select name="audienceType" value={form.audienceType} onChange={handleChange}>
                <option value="ALL_RESIDENTS">{t('All residents and owners')}</option>
                <option value="BUILDING_RESIDENTS">{t('Residents and owners in a building')}</option>
                <option value="UNIT_RESIDENTS">{t('Residents and owners in a unit')}</option>
              </select>
            </label>

            {form.audienceType === 'BUILDING_RESIDENTS' && (
              <label className="form-field">
                <span>{t('Building')}</span>
                <select name="buildingId" value={form.buildingId} onChange={handleChange} required>
                  <option value="">{t('Select building')}</option>
                  {buildings.map((building) => (
                    <option key={building.id} value={building.id}>
                      {building.code} — {building.name}
                    </option>
                  ))}
                </select>
              </label>
            )}

            {form.audienceType === 'UNIT_RESIDENTS' && (
              <label className="form-field">
                <span>{t('Unit')}</span>
                <select name="unitId" value={form.unitId} onChange={handleChange} required>
                  <option value="">{t('Select unit')}</option>
                  {units.map((unit) => (
                    <option key={unit.id} value={unit.id}>
                      {unit.unitNumber}{unit.buildingCode ? ` — ${unit.buildingCode}` : ''}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </div>

          {loadingOptions && (
            <div className="feedback feedback-info">{t('Loading communication options...')}</div>
          )}

          <div className="form-actions">
            <button className="button button-secondary" type="button" onClick={() => setShowForm(false)} disabled={saving}>
              {t('Cancel')}
            </button>
            <button className="button button-primary" type="submit" disabled={saving}>
              {saving ? t('Sending...') : t('Send communication')}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  )
}

export default CommunicationsPage
