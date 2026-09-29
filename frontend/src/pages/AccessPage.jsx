//----------------- milestone 21.2 ------------------
import { useCallback, useEffect, useMemo, useState } from 'react'
import { QRCodeCanvas } from 'qrcode.react'

import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import {
  checkInVisitorAuthorization,
  checkInVisitorByQrToken,
  checkOutVisitorAuthorization,
  createVisitorAuthorization,
  getActiveVisitors,
  getVisitorAuthorizations,
  getVisitorAuthorization,
} from '../api/accessApi.js'
import { getResidents } from '../api/residentsApi.js'
import { getUnits } from '../api/unitsApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 100
const LIST_PAGE_SIZE = 20
const STATUS_OPTIONS = ['', 'APPROVED', 'USED', 'EXPIRED', 'CANCELLED', 'REJECTED', 'PENDING']

function formatDateTime(value) {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return date.toLocaleString()
}

function toOffsetDateTime(localValue) {
  if (!localValue) return ''

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
  const canOperateStaff = ['ADMINISTRATOR', 'RECEPTION'].includes(role)
  const canView = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT'].includes(role)

  const [units, setUnits] = useState([])
  const [residents, setResidents] = useState([])
  const [optionsLoading, setOptionsLoading] = useState(true)
  const [optionsError, setOptionsError] = useState('')

  const [authorizations, setAuthorizations] = useState([])
  const [authorizationLoading, setAuthorizationLoading] = useState(true)
  const [authorizationError, setAuthorizationError] = useState('')
  const [authorizationStatus, setAuthorizationStatus] = useState('')

  const [activeVisitors, setActiveVisitors] = useState([])
  const [activeLoading, setActiveLoading] = useState(true)
  const [activeError, setActiveError] = useState('')

  const [form, setForm] = useState(createDefaultForm)
  const [saving, setSaving] = useState(false)
  const [authorizationFormOpen, setAuthorizationFormOpen] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [createdAuthorization, setCreatedAuthorization] = useState(null)
  const [selectedAuthorization, setSelectedAuthorization] = useState(null)
  const [authorizationLoadingDetail, setAuthorizationLoadingDetail] = useState(false)

  const [qrToken, setQrToken] = useState('')
  const [checkingIn, setCheckingIn] = useState(false)
  const [checkInError, setCheckInError] = useState('')
  const [checkInResult, setCheckInResult] = useState(null)

  const [checkingOutId, setCheckingOutId] = useState('')
  const [checkOutError, setCheckOutError] = useState('')
  const [checkOutResult, setCheckOutResult] = useState(null)

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

  const loadAuthorizations = useCallback(async () => {
    if (!canView) {
      setAuthorizationLoading(false)
      return
    }

    setAuthorizationLoading(true)
    setAuthorizationError('')

    try {
      const response = await getVisitorAuthorizations(0, LIST_PAGE_SIZE, authorizationStatus)
      setAuthorizations(Array.isArray(response?.content) ? response.content : [])
    } catch (requestError) {
      setAuthorizationError(requestError.message || t('Unable to load visitor authorizations.'))
    } finally {
      setAuthorizationLoading(false)
    }
  }, [authorizationStatus, canView, t])

  const loadActiveVisitors = useCallback(async () => {
    if (!canView) {
      setActiveLoading(false)
      return
    }

    setActiveLoading(true)
    setActiveError('')

    try {
      const response = await getActiveVisitors(0, LIST_PAGE_SIZE)
      setActiveVisitors(Array.isArray(response?.content) ? response.content : [])
    } catch (requestError) {
      setActiveError(requestError.message || t('Unable to load active visitors.'))
    } finally {
      setActiveLoading(false)
    }
  }, [canView, t])

  useEffect(() => {
    loadOptions()
  }, [loadOptions])

  useEffect(() => {
    loadAuthorizations()
  }, [loadAuthorizations])

  useEffect(() => {
    loadActiveVisitors()
  }, [loadActiveVisitors])

  const selectedUnitId = form.unitId

  const residentOptions = useMemo(
    () => residents.filter((resident) => !selectedUnitId || resident.unitId === selectedUnitId),
    [residents, selectedUnitId],
  )

  const activeAuthorizationIds = useMemo(
    () => new Set(activeVisitors.map((visitor) => visitor.authorizationId).filter(Boolean)),
    [activeVisitors],
  )

  const selectedResident = residentOptions.find((resident) => resident.id === form.residentId)

  useEffect(() => {
    if (isResident || !form.residentId) return

    const stillValid = residentOptions.some((resident) => resident.id === form.residentId)
    if (!stillValid) {
      setForm((current) => ({ ...current, residentId: '' }))
    }
  }, [form.residentId, isResident, residentOptions])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'unitId' && !isResident ? { residentId: '' } : {}),
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
      setAuthorizationFormOpen(false)
      setSuccessMessage(t('Visitor authorization created successfully.'))
      await Promise.all([loadAuthorizations(), loadActiveVisitors()])
      resetForm()
    } catch (requestError) {
      setError(requestError.message || t('Unable to create visitor authorization.'))
    } finally {
      setSaving(false)
    }
  }

  const openAuthorization = async (authorizationId) => {
    setSelectedAuthorization(null)
    setAuthorizationLoadingDetail(true)
    setAuthorizationError('')

    try {
      const response = await getVisitorAuthorization(authorizationId)
      setSelectedAuthorization(response)
    } catch (requestError) {
      setAuthorizationError(requestError.message || t('Unable to load authorization details.'))
    } finally {
      setAuthorizationLoadingDetail(false)
    }
  }

  const handleQrCheckIn = async (event) => {
    event.preventDefault()
    const token = qrToken.trim()
    if (!token) return

    setCheckingIn(true)
    setCheckInError('')
    setCheckInResult(null)

    try {
      const response = await checkInVisitorByQrToken(token)
      setCheckInResult(response)
      setQrToken('')
      await Promise.all([loadAuthorizations(), loadActiveVisitors()])
    } catch (requestError) {
      setCheckInError(requestError.message || t('Unable to check in visitor.'))
    } finally {
      setCheckingIn(false)
    }
  }

  const handleAuthorizationCheckIn = async (authorization) => {
    setCheckInError('')
    setCheckInResult(null)

    try {
      const response = await checkInVisitorAuthorization(authorization.id)
      setCheckInResult(response)
      await Promise.all([loadAuthorizations(), loadActiveVisitors()])
    } catch (requestError) {
      setCheckInError(requestError.message || t('Unable to check in visitor.'))
    }
  }

  const handleCheckOut = async (authorizationId) => {
    setCheckingOutId(authorizationId)
    setCheckOutError('')
    setCheckOutResult(null)

    try {
      const response = await checkOutVisitorAuthorization(authorizationId)
      setCheckOutResult(response)
      await Promise.all([loadAuthorizations(), loadActiveVisitors()])
    } catch (requestError) {
      setCheckOutError(requestError.message || t('Unable to check out visitor.'))
    } finally {
      setCheckingOutId('')
    }
  }

  const showQr = (authorization) => {
    setCreatedAuthorization(authorization)
  }

  const statusLabel = (status) => t(status || 'Unknown')

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('ACCESS / VISITORS')}</p>
          <h2>{t('Access / Visitors')}</h2>
          <p className="muted">
            {t('Create visitor authorizations, register entry and monitor visitors currently inside.')}
          </p>
        </div>
        {canCreate && (
          <button
            className="button button-primary"
            type="button"
            onClick={() => {
              setError('')
              setAuthorizationFormOpen(true)
            }}
          >
            {t('Authorize a visitor')}
          </button>
        )}
      </div>

      {successMessage && <div className="feedback feedback-success" role="status">{successMessage}</div>}
      {optionsError && <div className="feedback feedback-error" role="alert">{optionsError}</div>}

      {canCreate && (
        <Modal
          open={authorizationFormOpen}
          eyebrow={t('VISITOR AUTHORIZATION')}
          title={t('Authorize a visitor')}
          onClose={() => {
            if (!saving) {
              setAuthorizationFormOpen(false)
              setError('')
              resetForm()
            }
          }}
          size="large"
          footer={
            <div className="modal-actions">
              <button
                className="button button-secondary"
                type="button"
                onClick={() => {
                  if (!saving) {
                    setAuthorizationFormOpen(false)
                    setError('')
                    resetForm()
                  }
                }}
                disabled={saving}
              >
                {t('Cancel')}
              </button>
              <button
                className="button button-primary"
                type="submit"
                form="visitor-authorization-form"
                disabled={saving || optionsLoading}
              >
                {saving ? t('Creating...') : t('Create authorization')}
              </button>
            </div>
          }
        >

          {optionsLoading ? (
            <div className="feedback feedback-info" role="status">{t('Loading units and residents...')}</div>
          ) : (
            <form id="visitor-authorization-form" className="entity-form" onSubmit={handleSubmit}>
              {error && <div className="modal-feedback feedback feedback-error" role="alert">{error}</div>}
              {optionsError && <div className="modal-feedback feedback feedback-error" role="alert">{optionsError}</div>}
              <div className="form-section-label">{t('Authorization scope')}</div>

              <div className="form-grid-2">
                <label className="form-field">
                  <span>{t('Unit')}</span>
                  <select name="unitId" value={form.unitId} onChange={handleChange} required disabled={isResident}>
                    <option value="">{t('Select a unit')}</option>
                    {units.map((unit) => (
                      <option key={unit.id} value={unit.id}>{unit.unitNumber}</option>
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
                        {resident.firstName} {resident.lastName}
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

              {selectedResident && (
                <div className="feedback feedback-info">
                  {selectedResident.firstName} {selectedResident.lastName} · {selectedResident.unitNumber}
                </div>
              )}
            </form>
          )}
        </Modal>
      )}

      {canOperateStaff && (
        <article className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">{t('CHECK IN')}</p>
              <h3>{t('Register visitor entry')}</h3>
            </div>
          </div>
          <p className="panel-copy">
            {t('Use the QR token presented by the visitor. Camera scanning can be added later; pasting the token is supported now.')}
          </p>
          <form className="form-grid-2" onSubmit={handleQrCheckIn}>
            <label className="form-field">
              <span>{t('QR token')}</span>
              <input value={qrToken} onChange={(event) => setQrToken(event.target.value)} placeholder={t('Paste QR token')} required />
            </label>
            <div className="table-actions form-align-end">
              <button className="button button-primary" type="submit" disabled={checkingIn || !qrToken.trim()}>
                {checkingIn ? t('Checking in...') : t('Confirm entry')}
              </button>
            </div>
          </form>
          {checkInError && <div className="feedback feedback-error" role="alert">{checkInError}</div>}
        </article>
      )}

      {canView && (
        <article className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">{t('AUTHORIZATIONS')}</p>
              <h3>{t('Visitor authorizations')}</h3>
            </div>
            <label className="inline-filter">
              <span>{t('Status')}</span>
              <select value={authorizationStatus} onChange={(event) => setAuthorizationStatus(event.target.value)}>
                {STATUS_OPTIONS.map((status) => (
                  <option key={status || 'ALL'} value={status}>{status ? statusLabel(status) : t('All')}</option>
                ))}
              </select>
            </label>
          </div>

          {authorizationError && <div className="feedback feedback-error" role="alert">{authorizationError}</div>}
          {authorizationLoading ? (
            <div className="feedback feedback-info">{t('Loading visitor authorizations...')}</div>
          ) : authorizations.length === 0 ? (
            <div className="empty-state">{t('No visitor authorizations found.')}</div>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>{t('Visitor')}</th>
                    <th>{t('Unit')}</th>
                    <th>{t('Valid until')}</th>
                    <th>{t('Status')}</th>
                    <th>{t('Access')}</th>
                    <th>{t('Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {authorizations.map((authorization) => {
                    const isInside = activeAuthorizationIds.has(authorization.id)
                    return (
                      <tr key={authorization.id}>
                        <td>{authorization.visitorFirstName} {authorization.visitorLastName}</td>
                        <td>{authorization.unitNumber}</td>
                        <td>{formatDateTime(authorization.validUntil)}</td>
                        <td><span className={`status-pill status-${authorization.status?.toLowerCase()}`}>{statusLabel(authorization.status)}</span></td>
                        <td>{isInside ? t('Inside') : t('Not inside')}</td>
                        <td>
                          <TableActions moreLabel={t('More')}>
                            <TableAction icon="eye" label={t('View')} variant="view" onClick={() => openAuthorization(authorization.id)} />
                            <TableAction icon="qr" label={t('QR')} variant="neutral" onClick={() => showQr(authorization)} />
                            {canOperateStaff && authorization.status === 'APPROVED' && !isInside && (
                              <TableAction icon="arrowRight" label={t('Check in')} variant="edit" onClick={() => handleAuthorizationCheckIn(authorization)} />
                            )}
                            {canOperateStaff && isInside && (
                              <TableAction
                                icon="logout"
                                label={t('Check out')}
                                variant="delete"
                                onClick={() => handleCheckOut(authorization.id)}
                                disabled={checkingOutId === authorization.id}
                                loading={checkingOutId === authorization.id}
                                loadingLabel={t('Checking out...')}
                              />
                            )}
                          </TableActions>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </article>
      )}

      {canView && (
        <article className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">{t('ACTIVE VISITORS')}</p>
              <h3>{t('Currently inside')}</h3>
            </div>
          </div>

          {activeError && <div className="feedback feedback-error" role="alert">{activeError}</div>}
          {checkOutError && <div className="feedback feedback-error" role="alert">{checkOutError}</div>}
          {activeLoading ? (
            <div className="feedback feedback-info">{t('Loading active visitors...')}</div>
          ) : activeVisitors.length === 0 ? (
            <div className="empty-state">{t('No visitors are currently inside.')}</div>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>{t('Visitor')}</th>
                    <th>{t('Unit')}</th>
                    <th>{t('Entry time')}</th>
                    <th>{t('Access method')}</th>
                    <th>{t('Action')}</th>
                  </tr>
                </thead>
                <tbody>
                  {activeVisitors.map((visitor) => (
                    <tr key={visitor.id}>
                      <td>{visitor.visitorFirstName} {visitor.visitorLastName}</td>
                      <td>{visitor.unitNumber}</td>
                      <td>{formatDateTime(visitor.occurredAt)}</td>
                      <td>{visitor.accessMethod}</td>
                      <td>
                        {canOperateStaff ? (
                          <TableActions moreLabel={t('More')}>
                            <TableAction
                              icon="logout"
                              label={t('Check out')}
                              variant="delete"
                              onClick={() => handleCheckOut(visitor.authorizationId)}
                              disabled={checkingOutId === visitor.authorizationId}
                              loading={checkingOutId === visitor.authorizationId}
                              loadingLabel={t('Checking out...')}
                            />
                          </TableActions>
                        ) : (
                          <span className="muted">{t('View only')}</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </article>
      )}

      {!canCreate && !canOperateStaff && !canView && (
        <article className="panel panel-wide">
          <div className="feedback feedback-info">{t('Your role does not currently have access actions for this module.')}</div>
        </article>
      )}
      {selectedAuthorization && (
        <Modal open title={t('Authorization details')} onClose={() => setSelectedAuthorization(null)} size="medium">
          <div className="details-grid">
            <div><span>{t('Visitor')}</span><strong>{selectedAuthorization.visitorFirstName} {selectedAuthorization.visitorLastName}</strong></div>
            <div><span>{t('Unit')}</span><strong>{selectedAuthorization.unitNumber}</strong></div>
            <div><span>{t('Status')}</span><strong>{statusLabel(selectedAuthorization.status)}</strong></div>
            <div><span>{t('Purpose')}</span><strong>{selectedAuthorization.purpose || '—'}</strong></div>
            <div><span>{t('Valid from')}</span><strong>{formatDateTime(selectedAuthorization.validFrom)}</strong></div>
            <div><span>{t('Valid until')}</span><strong>{formatDateTime(selectedAuthorization.validUntil)}</strong></div>
            <div><span>{t('Created')}</span><strong>{formatDateTime(selectedAuthorization.createdAt)}</strong></div>
          </div>
          <div className="table-actions">
            <button className="button button-secondary" type="button" onClick={() => setSelectedAuthorization(null)}>{t('Close')}</button>
            <button className="button button-ghost" type="button" onClick={() => showQr(selectedAuthorization)}>{t('Show QR')}</button>
          </div>
        </Modal>
      )}
      {createdAuthorization && (
        <Modal open title={t('Visitor authorization')} onClose={() => setCreatedAuthorization(null)} size="medium">
          <div className="qr-layout">
            <div className="qr-code-card">
              <QRCodeCanvas value={createdAuthorization.qrToken || ''} size={220} includeMargin />
            </div>
            <div className="qr-details">
              <h4>{createdAuthorization.visitorFirstName} {createdAuthorization.visitorLastName}</h4>
              <div className="details-grid">
                <div><span>{t('Unit')}</span><strong>{createdAuthorization.unitNumber}</strong></div>
                <div><span>{t('Status')}</span><strong>{statusLabel(createdAuthorization.status)}</strong></div>
                <div><span>{t('Valid from')}</span><strong>{formatDateTime(createdAuthorization.validFrom)}</strong></div>
                <div><span>{t('Valid until')}</span><strong>{formatDateTime(createdAuthorization.validUntil)}</strong></div>
              </div>
              <div className="feedback feedback-info">
                <strong>{t('QR token')}</strong>
                <div className="activity-text qr-token-text">{createdAuthorization.qrToken}</div>
              </div>
            </div>
          </div>
        </Modal>
      )}

      {authorizationLoadingDetail && (
        <Modal open title={t('Authorization details')} onClose={() => setAuthorizationLoadingDetail(false)} size="medium">
          <div className="modal-loading-state">{t('Loading...')}</div>
        </Modal>
      )}

      {checkInResult && (
        <Modal open title={t('Visitor entry confirmed')} onClose={() => setCheckInResult(null)} size="medium">
          <div className="feedback feedback-success">{t('Visitor entry has been registered successfully.')}</div>
          <div className="details-grid">
            <div><span>{t('Visitor')}</span><strong>{checkInResult.visitorFirstName} {checkInResult.visitorLastName}</strong></div>
            <div><span>{t('Unit')}</span><strong>{checkInResult.unitNumber}</strong></div>
            <div><span>{t('Direction')}</span><strong>{checkInResult.direction}</strong></div>
            <div><span>{t('Access method')}</span><strong>{checkInResult.accessMethod}</strong></div>
            <div><span>{t('Occurred at')}</span><strong>{formatDateTime(checkInResult.occurredAt)}</strong></div>
            <div><span>{t('Handled by')}</span><strong>{checkInResult.handledByStaffName || '—'}</strong></div>
          </div>
        </Modal>
      )}

      {checkOutResult && (
        <Modal open title={t('Visitor exit confirmed')} onClose={() => setCheckOutResult(null)} size="medium">
          <div className="feedback feedback-success">{t('Visitor exit has been registered successfully.')}</div>
          <div className="details-grid">
            <div><span>{t('Visitor')}</span><strong>{checkOutResult.visitorFirstName} {checkOutResult.visitorLastName}</strong></div>
            <div><span>{t('Unit')}</span><strong>{checkOutResult.unitNumber}</strong></div>
            <div><span>{t('Direction')}</span><strong>{checkOutResult.direction}</strong></div>
            <div><span>{t('Occurred at')}</span><strong>{formatDateTime(checkOutResult.occurredAt)}</strong></div>
            <div><span>{t('Handled by')}</span><strong>{checkOutResult.handledByStaffName || '—'}</strong></div>
          </div>
        </Modal>
      )}
    </section>
  )
}

export default AccessPage