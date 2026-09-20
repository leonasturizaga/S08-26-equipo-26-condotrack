import { useCallback, useEffect, useMemo, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import { getBuildings } from '../api/buildingsApi.js'
import { getUnits } from '../api/unitsApi.js'
import { getResidents } from '../api/residentsApi.js'
import {
  createDelivery,
  getDeliveries,
  getDelivery,
  updateDeliveryStatus,
} from '../api/deliveriesApi.js'

const PAGE_SIZE = 20
const OPTION_PAGE_SIZE = 100

const DELIVERY_TYPES = ['PACKAGE', 'MAIL', 'FOOD', 'DOCUMENT', 'OTHER']

function formatDateTime(value) {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

function translateValue(t, value) {
  return value ? t(value) : '—'
}

function statusClass(status) {
  switch (status) {
    case 'RECEIVED':
      return 'status-warning'
    case 'NOTIFIED':
      return 'status-info'
    case 'COLLECTED':
      return 'status-success'
    case 'RETURNED':
    case 'CANCELLED':
      return 'status-danger'
    default:
      return 'status-muted'
  }
}

function createDefaultForm() {
  return {
    buildingId: '',
    unitId: '',
    residentId: '',
    carrierName: '',
    trackingNumber: '',
    deliveryType: 'PACKAGE',
    notes: '',
  }
}

function isTerminal(status) {
  return ['COLLECTED', 'RETURNED', 'CANCELLED'].includes(status)
}

function allowedStatusActions(status) {
  if (status === 'RECEIVED') {
    return ['NOTIFIED', 'RETURNED', 'CANCELLED']
  }

  if (status === 'NOTIFIED') {
    return ['COLLECTED', 'RETURNED', 'CANCELLED']
  }

  return []
}

function DeliveriesPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const role = user?.roles?.[0] || user?.role

  const canCreate = role === 'ADMINISTRATOR' || role === 'RECEPTION'
  const canOperate = canCreate
  const canView = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT'].includes(role)

  const [deliveries, setDeliveries] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedDelivery, setSelectedDelivery] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(createDefaultForm)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [actionLoadingId, setActionLoadingId] = useState('')

  const [buildings, setBuildings] = useState([])
  const [units, setUnits] = useState([])
  const [residents, setResidents] = useState([])
  const [optionsLoading, setOptionsLoading] = useState(false)
  const [optionsError, setOptionsError] = useState('')

  const loadDeliveries = useCallback(async (targetPage = page) => {
    if (!canView) {
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await getDeliveries(targetPage, PAGE_SIZE)
      setDeliveries(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? targetPage)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load deliveries.'))
      setDeliveries([])
    } finally {
      setLoading(false)
    }
  }, [canView, page, t])

  const loadCreateOptions = useCallback(async () => {
    if (!canCreate) {
      return
    }

    setOptionsLoading(true)
    setOptionsError('')

    try {
      const [buildingsResponse, unitsResponse, residentsResponse] = await Promise.all([
        getBuildings(0, OPTION_PAGE_SIZE),
        getUnits(0, OPTION_PAGE_SIZE),
        getResidents(0, OPTION_PAGE_SIZE),
      ])

      setBuildings(
        Array.isArray(buildingsResponse?.content)
          ? buildingsResponse.content.filter((item) => item.active !== false)
          : [],
      )
      setUnits(
        Array.isArray(unitsResponse?.content)
          ? unitsResponse.content.filter((item) => item.active !== false)
          : [],
      )
      setResidents(
        Array.isArray(residentsResponse?.content)
          ? residentsResponse.content.filter((item) => item.active !== false && item.unitId)
          : [],
      )
    } catch (requestError) {
      setOptionsError(requestError.message || t('Unable to load delivery options.'))
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, t])

  useEffect(() => {
    loadDeliveries(page)
  }, [loadDeliveries])

  useEffect(() => {
    loadCreateOptions()
  }, [loadCreateOptions])

  const filteredUnits = useMemo(
    () => units.filter((unit) => !form.buildingId || unit.buildingId === form.buildingId),
    [form.buildingId, units],
  )

  const filteredResidents = useMemo(
    () => residents.filter((resident) => !form.unitId || resident.unitId === form.unitId),
    [form.unitId, residents],
  )

  useEffect(() => {
    if (!form.unitId) return

    const unitStillValid = filteredUnits.some((unit) => unit.id === form.unitId)
    if (!unitStillValid) {
      setForm((current) => ({ ...current, unitId: '', residentId: '' }))
      return
    }

    const residentStillValid = filteredResidents.some((resident) => resident.id === form.residentId)
    if (!residentStillValid && form.residentId) {
      setForm((current) => ({ ...current, residentId: '' }))
    }
  }, [filteredResidents, filteredUnits, form.residentId, form.unitId])

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'buildingId' ? { unitId: '', residentId: '' } : {}),
      ...(name === 'unitId' ? { residentId: '' } : {}),
    }))
  }

  const resetForm = () => {
    setForm(createDefaultForm())
    setFormError('')
  }

  const openCreate = () => {
    resetForm()
    setSuccessMessage('')
    setOptionsError('')
    setShowCreate(true)
  }

  const closeCreate = () => {
    if (!saving) {
      setShowCreate(false)
      resetForm()
    }
  }

  const handleCreate = async (event) => {
    event.preventDefault()

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await createDelivery({
        buildingId: form.buildingId,
        unitId: form.unitId,
        residentId: form.residentId,
        carrierName: form.carrierName.trim() || null,
        trackingNumber: form.trackingNumber.trim() || null,
        deliveryType: form.deliveryType,
        notes: form.notes.trim() || null,
      })

      setShowCreate(false)
      resetForm()
      setSuccessMessage(t('Delivery registered successfully.'))
      await loadDeliveries(0)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to register delivery.'))
    } finally {
      setSaving(false)
    }
  }

  const openDelivery = async (deliveryId) => {
    setSelectedDelivery(null)
    setDetailLoading(true)
    setDetailError('')

    try {
      const response = await getDelivery(deliveryId)
      if (!response || typeof response !== 'object') {
        throw new Error(t('The delivery details could not be loaded.'))
      }
      setSelectedDelivery(response)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load delivery details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (!detailLoading) {
      setSelectedDelivery(null)
      setDetailError('')
    }
  }

  const updateStatus = async (delivery, status) => {
    if (!delivery?.id || !status) return

    setActionLoadingId(delivery.id)
    setError('')
    setSuccessMessage('')

    try {
      const updated = await updateDeliveryStatus(delivery.id, status)
      setSelectedDelivery((current) => (current?.id === delivery.id ? updated : current))
      setSuccessMessage(t('Delivery status updated successfully.'))
      await loadDeliveries(page)
    } catch (requestError) {
      setError(requestError.message || t('Unable to update delivery status.'))
    } finally {
      setActionLoadingId('')
    }
  }

  const handleStatusAction = async (delivery, status) => {
    if (['RETURNED', 'CANCELLED'].includes(status)) {
      const confirmed = window.confirm(
        t(status === 'RETURNED' ? 'Are you sure you want to mark this delivery as returned?' : 'Are you sure you want to cancel this delivery?'),
      )
      if (!confirmed) return
    }

    await updateStatus(delivery, status)
  }

  if (!canView) {
    return (
      <section className="module-page">
        <div className="module-page-header">
          <div>
            <p className="eyebrow">{t('DELIVERIES / MAIL')}</p>
            <h2>{t('Deliveries / Mail')}</h2>
          </div>
        </div>
        <article className="panel panel-wide">
          <div className="feedback feedback-info">{t('Your role does not currently have access to this module.')}</div>
        </article>
      </section>
    )
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('DELIVERIES / MAIL')}</p>
          <h2>{t('Deliveries / Mail')}</h2>
          <p className="muted">{t('Register incoming deliveries, notify residents and track collection status.')}</p>
        </div>
        {canCreate && (
          <button className="button button-primary" type="button" onClick={openCreate}>
            + {t('Register delivery')}
          </button>
        )}
      </div>

      {successMessage && <div className="feedback feedback-success" role="status">{successMessage}</div>}
      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <section className="data-panel">
        <div className="data-panel-header">
          <div>
            <p className="eyebrow">{t('DELIVERY DIRECTORY')}</p>
            <h3>{t('Deliveries')}</h3>
          </div>
          <span className="status-pill">{totalElements} {t('deliveries')}</span>
        </div>

        {loading ? (
          <div className="module-empty-state compact-empty-state">{t('Loading...')}</div>
        ) : deliveries.length === 0 ? (
          <div className="module-empty-state compact-empty-state">
            <h3>{t('No deliveries found')}</h3>
            <p className="muted">{t('There are no delivery records available for your role.')}</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Unit')}</th>
                  <th>{t('Resident')}</th>
                  <th>{t('Type')}</th>
                  <th>{t('Carrier')}</th>
                  <th>{t('Received')}</th>
                  <th>{t('Status')}</th>
                  <th className="table-actions-column">{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {deliveries.map((delivery) => {
                  const actions = canOperate ? allowedStatusActions(delivery.status) : []
                  return (
                    <tr key={delivery.id}>
                      <td><strong>{delivery.unitNumber}</strong></td>
                      <td>{delivery.residentName || '—'}</td>
                      <td><span className="status-badge status-info">{translateValue(t, delivery.deliveryType)}</span></td>
                      <td>{delivery.carrierName || '—'}</td>
                      <td>{formatDateTime(delivery.receivedAt)}</td>
                      <td><span className={`status-badge ${statusClass(delivery.status)}`}>{translateValue(t, delivery.status)}</span></td>
                      <td>
                        <div className="table-actions">
                          <button className="button button-ghost" type="button" onClick={() => openDelivery(delivery.id)}>
                            {t('View')}
                          </button>
                          {actions.includes('NOTIFIED') && (
                            <button className="button button-primary button-small" type="button" onClick={() => updateStatus(delivery, 'NOTIFIED')} disabled={actionLoadingId === delivery.id}>
                              {t('Notify')}
                            </button>
                          )}
                          {actions.includes('COLLECTED') && (
                            <button className="button button-primary button-small" type="button" onClick={() => updateStatus(delivery, 'COLLECTED')} disabled={actionLoadingId === delivery.id}>
                              {t('Collect')}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination-bar">
            <button className="button button-secondary button-small" type="button" onClick={() => loadDeliveries(Math.max(page - 1, 0))} disabled={loading || page === 0}>
              {t('Previous')}
            </button>
            <span className="muted">{t('Page')} {page + 1} {t('of')} {totalPages}</span>
            <button className="button button-secondary button-small" type="button" onClick={() => loadDeliveries(Math.min(page + 1, totalPages - 1))} disabled={loading || page >= totalPages - 1}>
              {t('Next')}
            </button>
          </div>
        )}
      </section>

      {showCreate && (
        <Modal open title={t('Register delivery')} onClose={closeCreate} size="large">
          {optionsLoading ? (
            <div className="modal-loading-state">{t('Loading delivery options...')}</div>
          ) : (
            <form className="entity-form" onSubmit={handleCreate}>
              {optionsError && <div className="feedback feedback-error">{optionsError}</div>}
              {formError && <div className="feedback feedback-error">{formError}</div>}

              <div className="form-section-label">{t('Delivery destination')}</div>
              <div className="form-grid-2">
                <label className="form-field">
                  <span>{t('Building')}</span>
                  <select name="buildingId" value={form.buildingId} onChange={handleChange} required>
                    <option value="">{t('Select building')}</option>
                    {buildings.map((building) => (
                      <option key={building.id} value={building.id}>{building.code} — {building.name}</option>
                    ))}
                  </select>
                </label>

                <label className="form-field">
                  <span>{t('Unit')}</span>
                  <select name="unitId" value={form.unitId} onChange={handleChange} required disabled={!form.buildingId}>
                    <option value="">{t('Select unit')}</option>
                    {filteredUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>{unit.unitNumber}</option>
                    ))}
                  </select>
                </label>

                <label className="form-field">
                  <span>{t('Resident')}</span>
                  <select name="residentId" value={form.residentId} onChange={handleChange} required disabled={!form.unitId}>
                    <option value="">{t('Select resident')}</option>
                    {filteredResidents.map((resident) => (
                      <option key={resident.id} value={resident.id}>{resident.firstName} {resident.lastName}</option>
                    ))}
                  </select>
                </label>

                <label className="form-field">
                  <span>{t('Delivery type')}</span>
                  <select name="deliveryType" value={form.deliveryType} onChange={handleChange} required>
                    {DELIVERY_TYPES.map((type) => (
                      <option key={type} value={type}>{translateValue(t, type)}</option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="form-section-label">{t('Delivery details')}</div>
              <div className="form-grid-2">
                <label className="form-field">
                  <span>{t('Carrier')}</span>
                  <input name="carrierName" value={form.carrierName} onChange={handleChange} placeholder={t('Optional')} />
                </label>
                <label className="form-field">
                  <span>{t('Tracking number')}</span>
                  <input name="trackingNumber" value={form.trackingNumber} onChange={handleChange} placeholder={t('Optional')} />
                </label>
                <label className="form-field form-field-full">
                  <span>{t('Notes')}</span>
                  <textarea name="notes" value={form.notes} onChange={handleChange} rows="4" placeholder={t('Optional')} />
                </label>
              </div>

              <div className="table-actions">
                <button className="button button-primary" type="submit" disabled={saving || optionsLoading}>
                  {saving ? t('Registering...') : t('Register delivery')}
                </button>
                <button className="button button-secondary" type="button" onClick={closeCreate} disabled={saving}>
                  {t('Cancel')}
                </button>
              </div>
            </form>
          )}
        </Modal>
      )}

      {detailLoading && (
        <Modal open title={t('Delivery details')} onClose={closeDetails} size="medium">
          <div className="modal-loading-state">{t('Loading...')}</div>
        </Modal>
      )}

      {!detailLoading && selectedDelivery && (
        <Modal open title={t('Delivery details')} onClose={closeDetails} size="medium">
          <div className="details-grid">
            <div><span>{t('Unit')}</span><strong>{selectedDelivery.unitNumber}</strong></div>
            <div><span>{t('Resident')}</span><strong>{selectedDelivery.residentName || '—'}</strong></div>
            <div><span>{t('Type')}</span><strong>{translateValue(t, selectedDelivery.deliveryType)}</strong></div>
            <div><span>{t('Status')}</span><strong>{translateValue(t, selectedDelivery.status)}</strong></div>
            <div><span>{t('Carrier')}</span><strong>{selectedDelivery.carrierName || '—'}</strong></div>
            <div><span>{t('Tracking number')}</span><strong>{selectedDelivery.trackingNumber || '—'}</strong></div>
            <div><span>{t('Received')}</span><strong>{formatDateTime(selectedDelivery.receivedAt)}</strong></div>
            <div><span>{t('Notified')}</span><strong>{formatDateTime(selectedDelivery.notifiedAt)}</strong></div>
            <div><span>{t('Collected')}</span><strong>{formatDateTime(selectedDelivery.collectedAt)}</strong></div>
            <div className="form-field-full"><span>{t('Notes')}</span><strong>{selectedDelivery.notes || '—'}</strong></div>
          </div>

          {detailError && <div className="feedback feedback-error">{detailError}</div>}

          {canOperate && !isTerminal(selectedDelivery.status) && (
            <div className="table-actions">
              {allowedStatusActions(selectedDelivery.status).map((status) => (
                <button
                  key={status}
                  className={status === 'CANCELLED' || status === 'RETURNED' ? 'button button-danger' : 'button button-primary'}
                  type="button"
                  disabled={actionLoadingId === selectedDelivery.id}
                  onClick={() => handleStatusAction(selectedDelivery, status)}
                >
                  {translateValue(t, status)}
                </button>
              ))}
            </div>
          )}
        </Modal>
      )}

      {!detailLoading && detailError && !selectedDelivery && (
        <div className="feedback feedback-error" role="alert">{detailError}</div>
      )}
    </section>
  )
}

export default DeliveriesPage
