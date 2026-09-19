import { useCallback, useEffect, useMemo, useState } from 'react'

import Modal from '../components/Modal.jsx'
import { checkInVisitorAuthorization, createVisitorAuthorization } from '../api/accessApi.js'
import { getResidents } from '../api/residentsApi.js'
import { getUnits } from '../api/unitsApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 100

function formatDateTime(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString()
}

function toOffsetDateTime(localValue) {
  if (!localValue) {
    return ''
  }

  const date = new Date(localValue)
  return Number.isNaN(date.getTime()) ? '' : date.toISOString()
}

function getLocalDateTimeValue(date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 16)
}

function createDefaultForm() {
  const start = new Date()
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000)

  return {
    unitId: '',
    residentId: '',
    visitorFirstName: '',
    visitorLastName: '',
    documentType: '',
    documentNumber: '',
    phone: '',
    companyName: '',
    purpose: '',
    validFrom: getLocalDateTimeValue(start),
    validUntil: getLocalDateTimeValue(end),
  }
}

function AccessPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const role = user?.roles?.[0] || user?.role
  const isResident = role === 'RESIDENT'
  const canCreate = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT'].includes(role)
  const canCheckIn = ['ADMINISTRATOR', 'RECEPTION'].includes(role)
  const isStaffScope = ['ADMINISTRATOR', 'RECEPTION'].includes(role)

  const [units, setUnits] = useState([])
  const [residents, setResidents] = useState([])
  const [optionsLoading, setOptionsLoading] = useState(true)
  const [optionsError, setOptionsError] = useState('')

  const [form, setForm] = useState(createDefaultForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [createdAuthorization, setCreatedAuthorization] = useState(null)
  const [checkInId, setCheckInId] = useState('')
  const [checkingIn, setCheckingIn] = useState(false)
  const [checkInError, setCheckInError] = useState('')
  const [checkInResult, setCheckInResult] = useState(null)

  const loadOptions = useCallback(async () => {
    if (!canCreate) {
      setOptionsLoading(false)
      return
    }

    setOptionsLoading(true)
    setOptionsError('')

    try {
      const [unitsResponse, residentsResponse] = await Promise.all([
        getUnits(0, PAGE_SIZE),
        getResidents(0, PAGE_SIZE),
      ])

      const availableUnits = Array.isArray(unitsResponse?.content)
        ? unitsResponse.content.filter((unit) => unit.active)
        : []

      const availableResidents = Array.isArray(residentsResponse?.content)
        ? residentsResponse.content.filter((resident) => resident.active)
        : []

      setUnits(availableUnits)
      setResidents(availableResidents)

      if (isResident) {
        const ownUnit = availableUnits[0]
        const ownResident = availableResidents.find(
          (resident) => resident.email?.toLowerCase() === user?.email?.toLowerCase(),
        ) || availableResidents[0]

        setForm((current) => ({
          ...current,
          unitId: current.unitId || ownUnit?.id || '',
          residentId: current.residentId || ownResident?.id || '',
        }))
      }
    } catch (requestError) {
      setOptionsError(requestError.message || t('Unable to load access options.'))
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, isResident, t, user?.email])

  useEffect(() => {
    loadOptions()
  }, [loadOptions])

  const selectedUnitId = form.unitId

  const residentOptions = useMemo(
    () => residents.filter((resident) => !selectedUnitId || resident.unitId === selectedUnitId),
    [residents, selectedUnitId],
  )

  const selectedResident = residentOptions.find((resident) => resident.id === form.residentId)

  useEffect(() => {
    if (!isStaffScope || !form.residentId) {
      return
    }

    const stillValid = residentOptions.some((resident) => resident.id === form.residentId)
    if (!stillValid) {
      setForm((current) => ({ ...current, residentId: '' }))
    }
  }, [form.residentId, isStaffScope, residentOptions])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'unitId' && isStaffScope ? { residentId: '' } : {}),
    }))
  }

  const resetForm = () => {
    const nextForm = createDefaultForm()

    if (isResident) {
      nextForm.unitId = units[0]?.id || ''
      nextForm.residentId = residents.find(
        (resident) => resident.email?.toLowerCase() === user?.email?.toLowerCase(),
      )?.id || residents[0]?.id || ''
    }

    setForm(nextForm)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    setSuccessMessage('')
    setCreatedAuthorization(null)

    try {
      const response = await createVisitorAuthorization({
        unitId: form.unitId,
        residentId: form.residentId || null,
        visitorFirstName: form.visitorFirstName.trim(),
        visitorLastName: form.visitorLastName.trim(),
        documentType: form.documentType.trim() || null,
        documentNumber: form.documentNumber.trim() || null,
        phone: form.phone.trim() || null,
        companyName: form.companyName.trim() || null,
        purpose: form.purpose.trim() || null,
        validFrom: toOffsetDateTime(form.validFrom),
        validUntil: toOffsetDateTime(form.validUntil),
      })

      setCreatedAuthorization(response)
      setCheckInId(response?.id || '')
      setSuccessMessage(t('Visitor authorization created successfully.'))
      resetForm()
    } catch (requestError) {
      setError(requestError.message || t('Unable to create visitor authorization.'))
    } finally {
      setSaving(false)
    }
  }

  const handleCheckIn = async (event) => {
    event.preventDefault()
    setCheckingIn(true)
    setCheckInError('')
    setCheckInResult(null)

    try {
      const response = await checkInVisitorAuthorization(checkInId.trim())
      setCheckInResult(response)
      setCheckInId(response?.authorizationId || checkInId.trim())
    } catch (requestError) {
      setCheckInError(requestError.message || t('Unable to check in visitor.'))
    } finally {
      setCheckingIn(false)
    }
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('ACCESS / VISITORS')}</p>
          <h2>{t('Access / Visitors')}</h2>
          <p className="muted">
            {t('Create visitor authorizations and register authorized visitors at entry.')}
          </p>
        </div>
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

      {optionsError && (
        <div className="feedback feedback-error" role="alert">
          {optionsError}
        </div>
      )}

      <section className="dashboard-grid">
        {canCreate && (
          <article className="panel panel-wide">
            <div className="panel-header">
              <div>
                <p className="eyebrow">{t('VISITOR AUTHORIZATION')}</p>
                <h3>{t('Authorize a visitor')}</h3>
              </div>
              <span className="status-pill">{t('Approved on creation')}</span>
            </div>

            <p className="panel-copy">
              {t('Create a visitor authorization for an active unit and define the valid time window.')}
            </p>

            {optionsLoading ? (
              <div className="feedback feedback-info" role="status">
                {t('Loading units and residents...')}
              </div>
            ) : (
              <form className="entity-form" onSubmit={handleSubmit}>
                <div className="form-section-label">{t('Authorization scope')}</div>

                <div className="form-grid-2">
                  <label className="form-field">
                    <span>{t('Unit')}</span>
                    <select
                      name="unitId"
                      value={form.unitId}
                      onChange={handleChange}
                      required
                      disabled={isResident}
                    >
                      <option value="">{t('Select a unit')}</option>
                      {units.map((unit) => (
                        <option key={unit.id} value={unit.id}>
                          {unit.unitNumber} · {unit.id}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="form-field">
                    <span>{t('Resident')}</span>
                    <select
                      name="residentId"
                      value={form.residentId}
                      onChange={handleChange}
                      disabled={isResident || residentOptions.length === 0}
                    >
                      <option value="">{t('No specific resident')}</option>
                      {residentOptions.map((resident) => (
                        <option key={resident.id} value={resident.id}>
                          {resident.firstName} {resident.lastName} · {resident.unitNumber}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <div className="form-section-label">{t('Visitor details')}</div>

                <div className="form-grid-2">
                  <label className="form-field">
                    <span>{t('First name')}</span>
                    <input name="visitorFirstName" value={form.visitorFirstName} onChange={handleChange} required />
                  </label>

                  <label className="form-field">
                    <span>{t('Last name')}</span>
                    <input name="visitorLastName" value={form.visitorLastName} onChange={handleChange} required />
                  </label>

                  <label className="form-field">
                    <span>{t('Document type')}</span>
                    <input name="documentType" value={form.documentType} onChange={handleChange} placeholder={t('Optional')} />
                  </label>

                  <label className="form-field">
                    <span>{t('Document number')}</span>
                    <input name="documentNumber" value={form.documentNumber} onChange={handleChange} placeholder={t('Optional')} />
                  </label>

                  <label className="form-field">
                    <span>{t('Phone')}</span>
                    <input name="phone" value={form.phone} onChange={handleChange} placeholder={t('Optional')} />
                  </label>

                  <label className="form-field">
                    <span>{t('Company')}</span>
                    <input name="companyName" value={form.companyName} onChange={handleChange} placeholder={t('Optional')} />
                  </label>
                </div>

                <label className="form-field">
                  <span>{t('Purpose')}</span>
                  <input name="purpose" value={form.purpose} onChange={handleChange} placeholder={t('Optional')} />
                </label>

                <div className="form-section-label">{t('Validity window')}</div>

                <div className="form-grid-2">
                  <label className="form-field">
                    <span>{t('Valid from')}</span>
                    <input type="datetime-local" name="validFrom" value={form.validFrom} onChange={handleChange} required />
                  </label>

                  <label className="form-field">
                    <span>{t('Valid until')}</span>
                    <input type="datetime-local" name="validUntil" value={form.validUntil} onChange={handleChange} required />
                  </label>
                </div>

                <div className="table-actions">
                  <button className="button button-primary" type="submit" disabled={saving || optionsLoading}>
                    {saving ? t('Creating...') : t('Create authorization')}
                  </button>
                  <button className="button button-secondary" type="button" onClick={resetForm} disabled={saving}>
                    {t('Clear')}
                  </button>
                </div>

                {selectedResident && (
                  <div className="feedback feedback-info">
                    {selectedResident.firstName} {selectedResident.lastName} · {selectedResident.unitNumber}
                  </div>
                )}
              </form>
            )}
          </article>
        )}

        {canCheckIn && (
          <article className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">{t('CHECK IN')}</p>
                <h3>{t('Register visitor entry')}</h3>
              </div>
            </div>

            <p className="panel-copy">
              {t('Use the authorization ID returned by the visitor authorization flow to register entry.')}
            </p>

            <form className="entity-form" onSubmit={handleCheckIn}>
              <label className="form-field">
                <span>{t('Authorization ID')}</span>
                <input
                  value={checkInId}
                  onChange={(event) => setCheckInId(event.target.value)}
                  placeholder={t('Paste authorization ID')}
                  required
                />
              </label>

              {checkInError && (
                <div className="form-error" role="alert">
                  {checkInError}
                </div>
              )}

              <button className="button button-primary" type="submit" disabled={checkingIn || !checkInId.trim()}>
                {checkingIn ? t('Checking in...') : t('Confirm entry')}
              </button>
            </form>
          </article>
        )}

        {!canCreate && !canCheckIn && (
          <article className="panel panel-wide">
            <div className="feedback feedback-info">
              {t('Your role does not currently have access actions for this module.')}
            </div>
          </article>
        )}
      </section>

      {createdAuthorization && (
        <Modal
          open
          title={t('Visitor authorization created')}
          onClose={() => setCreatedAuthorization(null)}
          size="medium"
        >
          <div className="form-grid-2">
            <div><span>{t('Visitor')}</span><strong>{createdAuthorization.visitorFirstName} {createdAuthorization.visitorLastName}</strong></div>
            <div><span>{t('Unit')}</span><strong>{createdAuthorization.unitNumber}</strong></div>
            <div><span>{t('Status')}</span><strong>{createdAuthorization.status}</strong></div>
            <div><span>{t('Purpose')}</span><strong>{createdAuthorization.purpose || '—'}</strong></div>
            <div><span>{t('Valid from')}</span><strong>{formatDateTime(createdAuthorization.validFrom)}</strong></div>
            <div><span>{t('Valid until')}</span><strong>{formatDateTime(createdAuthorization.validUntil)}</strong></div>
          </div>

          <div className="feedback feedback-info">
            <strong>{t('QR token')}</strong>
            <div className="activity-text">{createdAuthorization.qrToken}</div>
          </div>

          {canCheckIn && (
            <div className="table-actions">
              <button
                className="button button-primary"
                type="button"
                onClick={async () => {
                  setCreatedAuthorization(null)
                  setCheckInError('')
                  try {
                    const response = await checkInVisitorAuthorization(createdAuthorization.id)
                    setCheckInResult(response)
                  } catch (requestError) {
                    setCheckInError(requestError.message || t('Unable to check in visitor.'))
                  }
                }}
              >
                {t('Confirm entry now')}
              </button>
            </div>
          )}
        </Modal>
      )}

      {checkInResult && (
        <Modal
          open
          title={t('Visitor entry confirmed')}
          onClose={() => setCheckInResult(null)}
          size="medium"
        >
          <div className="feedback feedback-success">
            {t('Visitor entry has been registered successfully.')}
          </div>

          <div className="form-grid-2">
            <div><span>{t('Visitor')}</span><strong>{checkInResult.visitorFirstName} {checkInResult.visitorLastName}</strong></div>
            <div><span>{t('Unit')}</span><strong>{checkInResult.unitNumber}</strong></div>
            <div><span>{t('Direction')}</span><strong>{checkInResult.direction}</strong></div>
            <div><span>{t('Access method')}</span><strong>{checkInResult.accessMethod}</strong></div>
            <div><span>{t('Occurred at')}</span><strong>{formatDateTime(checkInResult.occurredAt)}</strong></div>
          </div>
        </Modal>
      )}
    </section>
  )
}

export default AccessPage
