import { useCallback, useEffect, useMemo, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import { getUnits } from '../api/unitsApi.js'
import { getResidents } from '../api/residentsApi.js'
import { getCommonAreas } from '../api/commonAreasApi.js'
import {
  createBooking,
  getBooking,
  getBookings,
  updateBooking,
  updateBookingStatus,
} from '../api/bookingsApi.js'

const PAGE_SIZE = 20
const OPTION_PAGE_SIZE = 100

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(date)
}

function toLocalInputValue(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toIso(value) {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date.toISOString()
}

function statusClass(status) {
  switch (status) {
    case 'APPROVED': return 'status-success'
    case 'PENDING': return 'status-warning'
    case 'REJECTED':
    case 'CANCELLED': return 'status-danger'
    case 'COMPLETED': return 'status-success'
    case 'NO_SHOW': return 'status-danger'
    default: return 'status-muted'
  }
}

function createDefaultForm() {
  return {
    buildingId: '',
    commonAreaId: '',
    unitId: '',
    residentId: '',
    startAt: '',
    endAt: '',
    purpose: '',
  }
}

function allowedAdminTransitions(status) {
  if (status === 'PENDING') return ['APPROVED', 'REJECTED', 'CANCELLED']
  if (status === 'APPROVED') return ['COMPLETED', 'CANCELLED', 'NO_SHOW']
  return []
}

function BookingsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const role = String(user?.roles?.[0] ?? user?.role ?? '').trim().toUpperCase()

  const canCreate = role === 'ADMINISTRATOR' || role === 'RESIDENT'
  const canOperate = role === 'ADMINISTRATOR'
  const canView = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT'].includes(role)

  const [commonAreas, setCommonAreas] = useState([])
  const [bookings, setBookings] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [commonAreaLoading, setCommonAreaLoading] = useState(true)
  const [commonAreaError, setCommonAreaError] = useState('')

  const [units, setUnits] = useState([])
  const [residents, setResidents] = useState([])
  const [optionsLoading, setOptionsLoading] = useState(false)
  const [optionsError, setOptionsError] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [showEdit, setShowEdit] = useState(false)
  const [form, setForm] = useState(createDefaultForm)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [selectedBooking, setSelectedBooking] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [actionLoadingId, setActionLoadingId] = useState('')

  const loadBookings = useCallback(async (targetPage = page) => {
    if (!canView) {
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    try {
      const response = await getBookings(targetPage, PAGE_SIZE)
      setBookings(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? targetPage)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load bookings.'))
      setBookings([])
    } finally {
      setLoading(false)
    }
  }, [canView, page, t])

  const loadCommonAreas = useCallback(async () => {
    if (!canView) {
      setCommonAreaLoading(false)
      return
    }
    setCommonAreaLoading(true)
    setCommonAreaError('')
    try {
      const response = await getCommonAreas('', 0, 100)
      setCommonAreas(Array.isArray(response?.content) ? response.content : [])
    } catch (requestError) {
      setCommonAreaError(requestError.message || t('Unable to load common areas.'))
      setCommonAreas([])
    } finally {
      setCommonAreaLoading(false)
    }
  }, [canView, t])

  const loadOptions = useCallback(async () => {
    if (!canCreate) return
    setOptionsLoading(true)
    setOptionsError('')
    try {
      const [unitsResponse, residentsResponse] = await Promise.all([
        getUnits(0, OPTION_PAGE_SIZE),
        getResidents(0, OPTION_PAGE_SIZE),
      ])
      setUnits(Array.isArray(unitsResponse?.content) ? unitsResponse.content.filter((item) => item.active !== false) : [])
      setResidents(Array.isArray(residentsResponse?.content) ? residentsResponse.content.filter((item) => item.active !== false && item.unitId) : [])
    } catch (requestError) {
      setOptionsError(requestError.message || t('Unable to load booking options.'))
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, t])

  useEffect(() => { loadBookings(page) }, [loadBookings])
  useEffect(() => { loadCommonAreas() }, [loadCommonAreas])
  useEffect(() => { loadOptions() }, [loadOptions])

  const buildingOptions = useMemo(() => {
    const seen = new Set()
    return commonAreas
      .filter((area) => area.buildingId && area.buildingCode)
      .filter((area) => {
        if (seen.has(area.buildingId)) return false
        seen.add(area.buildingId)
        return true
      })
      .sort((a, b) => String(a.buildingCode).localeCompare(String(b.buildingCode)))
  }, [commonAreas])

  const filteredCommonAreas = useMemo(
    () => commonAreas.filter((area) => !form.buildingId || area.buildingId === form.buildingId),
    [commonAreas, form.buildingId],
  )

  const filteredUnits = useMemo(
    () => units.filter((unit) => !form.buildingId || unit.buildingId === form.buildingId),
    [form.buildingId, units],
  )
  const filteredResidents = useMemo(
    () => residents.filter((resident) => !form.unitId || resident.unitId === form.unitId),
    [form.unitId, residents],
  )

  const resetBookingForm = () => {
    setForm(createDefaultForm())
    setFormError('')
  }

  const closeCreate = () => {
    if (!saving) {
      setShowCreate(false)
      resetBookingForm()
    }
  }
  const openCreate = () => {
    let ownBuildingId = ''
    let ownUnitId = ''
    let ownResidentId = ''

    if (role === 'RESIDENT') {
      const ownUnits = units
      const uniqueBuildingIds = [...new Set(ownUnits.map((unit) => unit.buildingId).filter(Boolean))]
      ownBuildingId = uniqueBuildingIds.length === 1 ? uniqueBuildingIds[0] : ''
      ownUnitId = ownUnits.length === 1 ? ownUnits[0].id : ''
      const ownResidentOptions = residents.filter((resident) => !ownUnitId || resident.unitId === ownUnitId)
      ownResidentId = ownResidentOptions.length === 1 ? ownResidentOptions[0].id : ''
    }
    setForm({
      ...createDefaultForm(),
      buildingId: ownBuildingId,
      unitId: ownUnitId,
      residentId: ownResidentId,
    })
    setFormError('')
    setSuccessMessage('')
    setShowCreate(true)
  }

  const handleChange = (event) => {
    const { name, value } = event.target

    if (name === 'buildingId') {
      setForm((current) => ({
        ...current,
        buildingId: value,
        commonAreaId: '',
        unitId: '',
        residentId: '',
      }))
      return
    }

    if (name === 'commonAreaId') {
      const selected = commonAreas.find((area) => area.id === value)
      setForm((current) => ({
        ...current,
        commonAreaId: value,
        buildingId: selected?.buildingId || current.buildingId,
      }))
      return
    }

    if (name === 'unitId') {
      const selected = units.find((unit) => unit.id === value)
      setForm((current) => ({
        ...current,
        unitId: value,
        residentId: '',
        buildingId: selected?.buildingId || current.buildingId,
      }))
      return
    }

    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleCreate = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (!form.buildingId || !form.commonAreaId || !form.unitId || !form.residentId || !form.startAt || !form.endAt) {
        throw new Error(t('Please complete all required booking fields.'))
      }
      await createBooking({
        buildingId: form.buildingId,
        commonAreaId: form.commonAreaId,
        unitId: form.unitId,
        residentId: form.residentId,
        startAt: toIso(form.startAt),
        endAt: toIso(form.endAt),
        purpose: form.purpose.trim() || null,
      })
      setShowCreate(false)
      resetBookingForm()
      setSuccessMessage(t('Booking created successfully.'))
      await loadBookings(0)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to create booking.'))
    } finally {
      setSaving(false)
    }
  }

  const openBooking = async (bookingId) => {
    setSelectedBooking(null)
    setDetailLoading(true)
    setDetailError('')
    try {
      const response = await getBooking(bookingId)
      if (!response || typeof response !== 'object') throw new Error(t('The booking details could not be loaded.'))
      setSelectedBooking(response)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load booking details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const openEdit = () => {
    if (!selectedBooking) return
    setForm({
      buildingId: selectedBooking.buildingId || '',
      commonAreaId: selectedBooking.commonAreaId || '',
      unitId: selectedBooking.unitId || '',
      residentId: selectedBooking.residentId || '',
      startAt: toLocalInputValue(selectedBooking.startAt),
      endAt: toLocalInputValue(selectedBooking.endAt),
      purpose: selectedBooking.purpose || '',
    })
    setFormError('')
    setShowEdit(true)
  }

  const closeEdit = () => {
    if (!saving) {
      setShowEdit(false)
      setFormError('')
    }
  }

  const handleEdit = async (event) => {
    event.preventDefault()
    if (!selectedBooking) return
    setSaving(true)
    setFormError('')
    try {
      if (!form.startAt || !form.endAt) {
        throw new Error(t('Please complete all required booking fields.'))
      }
      const response = await updateBooking(selectedBooking.id, {
        startAt: toIso(form.startAt),
        endAt: toIso(form.endAt),
        purpose: form.purpose.trim() || null,
      })
      setSelectedBooking(response)
      setShowEdit(false)
      setSuccessMessage(t('Booking updated successfully.'))
      await loadBookings(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to update booking.'))
    } finally {
      setSaving(false)
    }
  }
  const handleStatus = async (booking, status) => {
    setActionLoadingId(booking.id)
    setError('')
    try {
      const response = await updateBookingStatus(booking.id, { status })
      setSelectedBooking(response)
      setSuccessMessage(t('Booking status updated successfully.'))
      await loadBookings(page)
    } catch (requestError) {
      setError(requestError.message || t('Unable to update booking status.'))
    } finally {
      setActionLoadingId('')
    }
  }

  const ownCancellationAllowed = role === 'RESIDENT' && ['PENDING', 'APPROVED'].includes(selectedBooking?.status)
  const ownEditAllowed = role === 'RESIDENT' && selectedBooking?.status === 'PENDING'
  const adminActions = selectedBooking ? allowedAdminTransitions(selectedBooking.status) : []

  if (!canView) {
    return <div className="empty-state">{t('You do not have access to Common Area Bookings.')}</div>
  }

  return (
    <div className="module-page">
      <section className="module-header">
        <div>
          <p className="eyebrow">{t('AMENITIES')}</p>
          <h2>{t('Common Area Bookings')}</h2>
          <p className="muted">{t('View common areas and manage booking requests within your role.')}</p>
        </div>
        {canCreate && (
          <button className="button button-primary" type="button" onClick={openCreate}>
            {t('New booking')}
          </button>
        )}
      </section>

      {successMessage && <div className="feedback feedback-success" role="status">{successMessage}</div>}
      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <section className="dashboard-grid">
        <article className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">{t('COMMON AREAS')}</p>
              <h3>{t('Available amenities')}</h3>
            </div>
            <span className="status-pill">{commonAreas.length} {t('active areas')}</span>
          </div>

          {commonAreaLoading ? (
            <div className="feedback feedback-info">{t('Loading common areas...')}</div>
          ) : commonAreaError ? (
            <div className="feedback feedback-error">{commonAreaError}</div>
          ) : commonAreas.length === 0 ? (
            <div className="empty-state">{t('No common areas found.')}</div>
          ) : (
            <div className="module-link-grid">
              {commonAreas.map((area) => (
                <div key={area.id} className="module-link">
                  <span className="module-link-icon"><span aria-hidden="true">◆</span></span>
                  <span className="module-link-content">
                    <strong>{area.name}</strong>
                    <small>{t(area.areaType)} · {area.capacity ?? '—'} {t('capacity')}</small>
                  </span>
                  <span className="module-link-arrow">{area.bookingDurationMinutes ? `${area.bookingDurationMinutes} min` : ''}</span>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">{t('BOOKINGS')}</p>
            <h3>{t('Booking requests')}</h3>
          </div>
          <span className="status-pill">{totalElements} {t('total')}</span>
        </div>

        {loading ? (
          <div className="feedback feedback-info">{t('Loading bookings...')}</div>
        ) : bookings.length === 0 ? (
          <div className="empty-state">{t('No bookings found.')}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Common area')}</th>
                  <th>{t('Unit')}</th>
                  <th>{t('Resident')}</th>
                  <th>{t('Start')}</th>
                  <th>{t('End')}</th>
                  <th>{t('Status')}</th>
                  <th>{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.commonAreaName}</td>
                    <td>{booking.unitNumber}</td>
                    <td>{booking.residentName || '—'}</td>
                    <td>{formatDateTime(booking.startAt)}</td>
                    <td>{formatDateTime(booking.endAt)}</td>
                    <td><span className={`status-pill ${statusClass(booking.status)}`}>{t(booking.status)}</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="button button-ghost button-small" type="button" onClick={() => openBooking(booking.id)}>{t('View')}</button>
                        {canOperate && allowedAdminTransitions(booking.status).slice(0, 1).map((status) => (
                          <button
                            key={status}
                            className="button button-primary button-small"
                            type="button"
                            disabled={actionLoadingId === booking.id}
                            onClick={() => handleStatus(booking, status)}
                          >
                            {t(status)}
                          </button>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button className="button button-secondary button-small" type="button" disabled={page <= 0} onClick={() => loadBookings(page - 1)}>{t('Previous')}</button>
            <span>{t('Page')} {page + 1} {t('of')} {totalPages}</span>
            <button className="button button-secondary button-small" type="button" disabled={page >= totalPages - 1} onClick={() => loadBookings(page + 1)}>{t('Next')}</button>
          </div>
        )}
      </section>

      <Modal open={showCreate} title={t('New booking')} onClose={closeCreate} size="medium" closeOnBackdrop={!saving}>
        <form className="form-grid" onSubmit={handleCreate}>
          {optionsError && <div className="feedback feedback-error form-grid-full">{optionsError}</div>}

          <label className="form-field">
            <span>{t('Building')}</span>
            <select name="buildingId" value={form.buildingId} onChange={handleChange} disabled={optionsLoading || role === 'RESIDENT' && Boolean(form.buildingId)} required>
              <option value="">{t('Select building')}</option>
              {buildingOptions.map((building) => <option key={building.buildingId} value={building.buildingId}>{building.buildingCode}</option>)}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Common area')}</span>
            <select name="commonAreaId" value={form.commonAreaId} onChange={handleChange} disabled={optionsLoading || commonAreaLoading || !form.buildingId} required>
              <option value="">{t('Select common area')}</option>
              {filteredCommonAreas.map((area) => <option key={area.id} value={area.id}>{area.name}</option>)}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Unit')}</span>
            <select name="unitId" value={form.unitId} onChange={handleChange} disabled={optionsLoading || role === 'RESIDENT' && filteredUnits.length <= 1 || !form.buildingId} required>
              <option value="">{t('Select unit')}</option>
              {filteredUnits.map((unit) => <option key={unit.id} value={unit.id}>{unit.unitNumber}</option>)}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Resident')}</span>
            <select name="residentId" value={form.residentId} onChange={handleChange} disabled={optionsLoading || role === 'RESIDENT' && filteredResidents.length <= 1 || !form.unitId} required>
              <option value="">{t('Select resident')}</option>
              {filteredResidents.map((resident) => <option key={resident.id} value={resident.id}>{resident.firstName} {resident.lastName}</option>)}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Start')}</span>
            <input type="datetime-local" name="startAt" value={form.startAt} onChange={handleChange} required />
          </label>

          <label className="form-field">
            <span>{t('End')}</span>
            <input type="datetime-local" name="endAt" value={form.endAt} onChange={handleChange} required />
          </label>

          <label className="form-field form-grid-full">
            <span>{t('Purpose')}</span>
            <input name="purpose" value={form.purpose} onChange={handleChange} maxLength={255} />
          </label>

          {formError && <div className="feedback feedback-error form-grid-full" role="alert">{formError}</div>}

          <div className="modal-actions form-grid-full">
            <button className="button button-secondary" type="button" disabled={saving} onClick={closeCreate}>{t('Cancel')}</button>
            <button className="button button-primary" type="submit" disabled={saving || optionsLoading}>{saving ? t('Saving...') : t('Create booking')}</button>
          </div>
        </form>
      </Modal>

      <Modal open={showEdit} title={t('Edit booking')} onClose={closeEdit} size="medium" closeOnBackdrop={!saving}>
        <form className="form-grid" onSubmit={handleEdit}>
          <div className="booking-edit-context form-grid-full">
            <div><span>{t('Building')}</span><strong>{selectedBooking?.buildingCode || '—'}</strong></div>
            <div><span>{t('Common area')}</span><strong>{selectedBooking?.commonAreaName || '—'}</strong></div>
            <div><span>{t('Unit')}</span><strong>{selectedBooking?.unitNumber || '—'}</strong></div>
          </div>

          <label className="form-field">
            <span>{t('Start')}</span>
            <input type="datetime-local" name="startAt" value={form.startAt} onChange={handleChange} required />
          </label>

          <label className="form-field">
            <span>{t('End')}</span>
            <input type="datetime-local" name="endAt" value={form.endAt} onChange={handleChange} required />
          </label>

          <label className="form-field form-grid-full">
            <span>{t('Purpose')}</span>
            <input name="purpose" value={form.purpose} onChange={handleChange} maxLength={255} />
          </label>

          {formError && <div className="feedback feedback-error form-grid-full" role="alert">{formError}</div>}

          <div className="modal-actions form-grid-full">
            <button className="button button-secondary" type="button" disabled={saving} onClick={closeEdit}>{t('Cancel')}</button>
            <button className="button button-primary" type="submit" disabled={saving}>{saving ? t('Saving...') : t('Save changes')}</button>
          </div>
        </form>
      </Modal>

      <Modal open={detailLoading || Boolean(selectedBooking)} title={t('Booking details')} onClose={() => !detailLoading && setSelectedBooking(null)} size="medium">
        {detailLoading ? (
          <div className="feedback feedback-info">{t('Loading booking details...')}</div>
        ) : selectedBooking ? (
          <div className="detail-grid">
            <div><span>{t('Common area')}</span><strong>{selectedBooking.commonAreaName}</strong></div>
            <div><span>{t('Building')}</span><strong>{selectedBooking.buildingCode || '—'}</strong></div>
            <div><span>{t('Unit')}</span><strong>{selectedBooking.unitNumber}</strong></div>
            <div><span>{t('Resident')}</span><strong>{selectedBooking.residentName || '—'}</strong></div>
            <div><span>{t('Status')}</span><strong className={`status-pill ${statusClass(selectedBooking.status)}`}>{t(selectedBooking.status)}</strong></div>
            <div><span>{t('Start')}</span><strong>{formatDateTime(selectedBooking.startAt)}</strong></div>
            <div><span>{t('End')}</span><strong>{formatDateTime(selectedBooking.endAt)}</strong></div>
            <div><span>{t('Purpose')}</span><strong>{selectedBooking.purpose || '—'}</strong></div>
            {selectedBooking.cancellationReason && <div className="field-span-2"><span>{t('Cancellation reason')}</span><strong>{selectedBooking.cancellationReason}</strong></div>}
          </div>
        ) : (
          <div className="feedback feedback-error">{detailError}</div>
        )}

        {selectedBooking && (canOperate || ownEditAllowed || ownCancellationAllowed) && (
          <div className="modal-actions">
            {ownEditAllowed && (
              <button className="button button-secondary" type="button" disabled={actionLoadingId === selectedBooking.id} onClick={openEdit}>{t('Edit booking')}</button>
            )}
            {canOperate && adminActions.map((status) => (
              <button key={status} className={`button ${status === 'CANCELLED' || status === 'REJECTED' ? 'button-danger' : 'button-primary'}`} type="button" disabled={actionLoadingId === selectedBooking.id} onClick={() => handleStatus(selectedBooking, status)}>{t(status)}</button>
            ))}
            {ownCancellationAllowed && (
              <button className="button button-danger" type="button" disabled={actionLoadingId === selectedBooking.id} onClick={() => handleStatus(selectedBooking, 'CANCELLED')}>{t('Cancel booking')}</button>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default BookingsPage

