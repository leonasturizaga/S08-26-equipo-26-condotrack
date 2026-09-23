import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import {
  createMove,
  getMove,
  getMoveResidentOptions,
  getMoveUnitOptions,
  getMoves,
  updateMoveOwnerAuthorization,
  updateMoveStatus,
} from '../api/movesApi.js'

const PAGE_SIZE = 20
const REQUEST_TYPES = ['MOVE_IN', 'MOVE_OUT']
const STATUSES = ['PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SCHEDULED', 'COMPLETED', 'CANCELLED']

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(date)
}

function toLocal(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const offset = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

function fromLocal(value) {
  return value ? new Date(value).toISOString() : null
}

function statusClass(status) {
  if (status === 'PENDING_APPROVAL') return 'status-warning'
  if (status === 'APPROVED' || status === 'SCHEDULED') return 'status-info'
  if (status === 'COMPLETED') return 'status-success'
  if (status === 'REJECTED' || status === 'CANCELLED') return 'status-danger'
  return 'status-muted'
}

function defaultCreateForm() {
  return {
    unitId: '',
    residentId: '',
    requestType: 'MOVE_IN',
    scheduledStart: '',
    scheduledEnd: '',
    notes: '',
  }
}

function defaultStatusForm() {
  return {
    status: '',
    scheduledStart: '',
    scheduledEnd: '',
    notes: '',
  }
}

function MovePage() {
  const { t } = useTranslation()
  const { user } = useAuth()
const rawRole = user?.roles?.[0] ?? user?.role ?? ''
const role = String(rawRole).trim().toUpperCase()

const isAdmin = role === 'ADMINISTRATOR'
const isOwner = role === 'OWNER'
const canCreate = isAdmin || role === 'RESIDENT'
  const canList = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT', 'OWNER'].includes(role)

  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [unitOptions, setUnitOptions] = useState([])
  const [residentOptions, setResidentOptions] = useState([])
  const [optionsError, setOptionsError] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [createForm, setCreateForm] = useState(defaultCreateForm())
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const [selected, setSelected] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [statusOpen, setStatusOpen] = useState(false)
  const [statusForm, setStatusForm] = useState(defaultStatusForm())
  const [actionLoading, setActionLoading] = useState(false)
  const [actionError, setActionError] = useState('')
  const [success, setSuccess] = useState('')

  const loadMoves = useCallback(async (target = 0) => {
    if (!canList) return
    setLoading(true)
    setError('')
    try {
      const response = await getMoves(target, PAGE_SIZE)
      setItems(Array.isArray(response?.items) ? response.items : [])
      setPage(response?.page ?? target)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setItems([])
      setError(requestError.message || t('Unable to load move requests.'))
    } finally {
      setLoading(false)
    }
  }, [canList, t])

  const loadCreateOptions = useCallback(async () => {
    if (!canCreate) return
    setOptionsError('')
    try {
      const units = await getMoveUnitOptions()
      setUnitOptions(Array.isArray(units) ? units : [])
      setResidentOptions([])
    } catch (requestError) {
      setOptionsError(requestError.message || t('Unable to load move request options.'))
      setUnitOptions([])
    }
  }, [canCreate, t])

  useEffect(() => {
    loadMoves(0)
  }, [loadMoves])

  useEffect(() => {
    loadCreateOptions()
  }, [loadCreateOptions])

  const selectedUnit = useMemo(
    () => unitOptions.find((unit) => unit.id === createForm.unitId),
    [unitOptions, createForm.unitId],
  )

  useEffect(() => {
    if (!isAdmin || createForm.requestType !== 'MOVE_OUT' || !createForm.unitId) {
      setResidentOptions([])
      return
    }
    getMoveResidentOptions(createForm.unitId)
      .then((response) => setResidentOptions(Array.isArray(response) ? response : []))
      .catch(() => setResidentOptions([]))
  }, [isAdmin, createForm.requestType, createForm.unitId])

  const handleCreateChange = (event) => {
    const { name, value } = event.target
    setCreateForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'requestType' && value === 'MOVE_IN' ? { residentId: '' } : {}),
    }))
  }

  const openCreate = () => {
    setCreateForm(defaultCreateForm())
    setResidentOptions([])
    setFormError('')
    setOptionsError('')
    setSuccess('')
    setCreateOpen(true)
  }

  const submitCreate = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await createMove({
        unitId: createForm.unitId,
        residentId: isAdmin && createForm.residentId ? createForm.residentId : null,
        requestType: createForm.requestType,
        scheduledStart: fromLocal(createForm.scheduledStart),
        scheduledEnd: fromLocal(createForm.scheduledEnd),
        notes: createForm.notes.trim() || null,
      })
      setCreateOpen(false)
      setSuccess(t('Move request created successfully.'))
      await loadMoves(0)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to create move request.'))
    } finally {
      setSaving(false)
    }
  }

  const openDetails = async (moveId) => {
    setSelected(null)
    setDetailError('')
    setActionError('')
    setDetailLoading(true)
    try {
      setSelected(await getMove(moveId))
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load move request.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (actionLoading) return
    setSelected(null)
    setDetailError('')
    setActionError('')
    setStatusOpen(false)
  }

  const openStatus = (status) => {
    setStatusForm({
      status,
      scheduledStart: toLocal(selected?.scheduledStart),
      scheduledEnd: toLocal(selected?.scheduledEnd),
      notes: selected?.notes || '',
    })
    setActionError('')
    setStatusOpen(true)
  }

  const submitStatus = async (event) => {
    event.preventDefault()
    if (!selected) return
    setActionLoading(true)
    setActionError('')
    try {
      const updated = await updateMoveStatus(selected.id, {
        status: statusForm.status,
        scheduledStart: statusForm.scheduledStart ? fromLocal(statusForm.scheduledStart) : null,
        scheduledEnd: statusForm.scheduledEnd ? fromLocal(statusForm.scheduledEnd) : null,
        notes: statusForm.notes.trim() || null,
      })
      setSelected(updated)
      setStatusOpen(false)
      setSuccess(t('Move request updated successfully.'))
      await loadMoves(page)
    } catch (requestError) {
      setActionError(requestError.message || t('Unable to update move request.'))
    } finally {
      setActionLoading(false)
    }
  }

  const authorizeOwner = async () => {
    if (!selected) return
    setActionLoading(true)
    setActionError('')
    try {
      const updated = await updateMoveOwnerAuthorization(selected.id, {
        authorized: true,
        notes: null,
      })
      setSelected(updated)
      setSuccess(t('Owner authorization recorded.'))
      await loadMoves(page)
    } catch (requestError) {
      setActionError(requestError.message || t('Unable to authorize this move request.'))
    } finally {
      setActionLoading(false)
    }
  }

  const nextAdminAction = (status) => {
    if (status === 'PENDING_APPROVAL') return ['APPROVED', 'REJECTED', 'CANCELLED']
    if (status === 'APPROVED') return ['SCHEDULED', 'CANCELLED']
    if (status === 'SCHEDULED') return ['COMPLETED', 'CANCELLED']
    return []
  }

  return (
    <section className="module-page">
      <div className="page-header">
        <div>
          <p className="eyebrow">{t('OPERATIONS')}</p>
          <h1>{t('Move Requests')}</h1>
          <p className="page-description">{t('Manage move-in and move-out requests.')}</p>
        </div>
        {canCreate && (
          <button className="button button-primary" type="button" onClick={openCreate}>
            {t('Create move request')}
          </button>
        )}
      </div>

      {success && <div className="feedback feedback-success" role="status">{success}</div>}
      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <article className="data-card">
        <div className="data-card-header">
          <div>
            <h2>{t('Move requests')}</h2>
            <p>{t('Total')}: {totalElements}</p>
          </div>
        </div>

        {loading ? (
          <div className="feedback feedback-info">{t('Loading...')}</div>
        ) : items.length === 0 ? (
          <div className="empty-state">{t('No move requests found.')}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Building')}</th>
                  <th>{t('Unit')}</th>
                  <th>{t('Resident')}</th>
                  <th>{t('Type')}</th>
                  <th>{t('Status')}</th>
                  <th>{t('Requested')}</th>
                  <th>{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.buildingCode}</td>
                    <td>{item.unitNumber}</td>
                    <td>{item.residentName || '—'}</td>
                    <td>{t(item.requestType)}</td>
                    <td><span className={`status-pill ${statusClass(item.status)}`}>{t(item.status)}</span></td>
                    <td>{formatDateTime(item.requestedAt)}</td>
                    <td>
                      <button className="button button-ghost button-small" type="button" onClick={() => openDetails(item.id)}>
                        {t('View')}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button className="button button-secondary button-small" type="button" onClick={() => loadMoves(Math.max(page - 1, 0))} disabled={page === 0 || loading}>{t('Previous')}</button>
            <span>{t('Page')} {page + 1} {t('of')} {totalPages}</span>
            <button className="button button-secondary button-small" type="button" onClick={() => loadMoves(Math.min(page + 1, totalPages - 1))} disabled={page >= totalPages - 1 || loading}>{t('Next')}</button>
          </div>
        )}
      </article>

      <Modal open={createOpen} title={t('Create move request')} onClose={() => !saving && setCreateOpen(false)}>
        <form className="entity-form" onSubmit={submitCreate}>
          {formError && <div className="modal-feedback feedback feedback-error" role="alert">{formError}</div>}
          {optionsError && <div className="modal-feedback feedback feedback-error" role="alert">{optionsError}</div>}

          <label className="form-field">
            <span>{t('Unit')}</span>
            <select name="unitId" value={createForm.unitId} onChange={handleCreateChange} disabled={saving} required>
              <option value="">{t('Select unit')}</option>
              {unitOptions.map((unit) => <option key={unit.id} value={unit.id}>{unit.buildingCode} · {unit.unitNumber}</option>)}
            </select>
          </label>

          {isAdmin && createForm.requestType === 'MOVE_OUT' && (
            <label className="form-field">
              <span>{t('Resident')}</span>
              <select name="residentId" value={createForm.residentId} onChange={handleCreateChange} disabled={saving || !createForm.unitId}>
                <option value="">{t('Select resident')}</option>
                {residentOptions.map((resident) => <option key={resident.id} value={resident.id}>{resident.firstName} {resident.lastName} · {t(resident.residentType)}</option>)}
              </select>
              <small>{t('For move-out, select the current resident of the unit. For move-in, the incoming resident can be assigned after approval.')}</small>
            </label>
          )}

          {isAdmin && createForm.requestType === 'MOVE_IN' && (
            <div className="feedback feedback-info">
              {t('Move-in requests can be created for an active unit without an existing resident assignment. The incoming resident can be assigned after approval.')}
            </div>
          )}
          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('Request type')}</span>
              <select name="requestType" value={createForm.requestType} onChange={handleCreateChange} disabled={saving} required>
                {REQUEST_TYPES.map((type) => <option key={type} value={type}>{t(type)}</option>)}
              </select>
            </label>
            <label className="form-field">
              <span>{t('Building')}</span>
              <input value={selectedUnit?.buildingCode || '—'} readOnly />
            </label>
          </div>

          <div className="form-grid-2">
            <label className="form-field"><span>{t('Scheduled start')}</span><input type="datetime-local" name="scheduledStart" value={createForm.scheduledStart} onChange={handleCreateChange} disabled={saving}/></label>
            <label className="form-field"><span>{t('Scheduled end')}</span><input type="datetime-local" name="scheduledEnd" value={createForm.scheduledEnd} onChange={handleCreateChange} disabled={saving}/></label>
          </div>

          <label className="form-field"><span>{t('Notes')}</span><textarea name="notes" value={createForm.notes} onChange={handleCreateChange} rows={5} maxLength={10000} disabled={saving}/></label>

          <div className="entity-form-actions">
            <button className="button button-secondary" type="button" onClick={() => !saving && setCreateOpen(false)} disabled={saving}>{t('Cancel')}</button>
            <button className="button button-primary" type="submit" disabled={saving}>{saving ? t('Saving...') : t('Create move request')}</button>
          </div>
        </form>
      </Modal>

      <Modal open={detailLoading || Boolean(selected)} title={t('Move request')} onClose={closeDetails}>
        {detailLoading ? (
          <div className="feedback feedback-info">{t('Loading...')}</div>
        ) : detailError && !selected ? (
          <div className="feedback feedback-error" role="alert">{detailError}</div>
        ) : selected ? (
          <div className="detail-layout">
            {detailError && <div className="feedback feedback-error" role="alert">{detailError}</div>}

            <div className="detail-grid">
              <div><span>{t('Building')}</span><strong>{selected.buildingCode}</strong></div>
              <div><span>{t('Unit')}</span><strong>{selected.unitNumber}</strong></div>
              <div><span>{t('Resident')}</span><strong>{selected.residentName || '—'}</strong></div>
              <div><span>{t('Type')}</span><strong>{t(selected.requestType)}</strong></div>
              <div><span>{t('Status')}</span><strong><span className={`status-pill ${statusClass(selected.status)}`}>{t(selected.status)}</span></strong></div>
              <div><span>{t('Requested')}</span><strong>{formatDateTime(selected.requestedAt)}</strong></div>
              <div><span>{t('Scheduled start')}</span><strong>{formatDateTime(selected.scheduledStart)}</strong></div>
              <div><span>{t('Scheduled end')}</span><strong>{formatDateTime(selected.scheduledEnd)}</strong></div>
            </div>

            <div className="detail-section">
              <span>{t('Owner authorization')}</span>
              <p>{selected.ownerAuthorized ? t('Authorized') : t('Not authorized yet')}</p>
              {selected.ownerAuthorized && selected.ownerAuthorizedByUserName && <small>{selected.ownerAuthorizedByUserName} · {formatDateTime(selected.ownerAuthorizedAt)}</small>}
              {selected.ownerAuthorizationNotes && <p>{selected.ownerAuthorizationNotes}</p>}
            </div>

            {selected.notes && <div className="detail-section"><span>{t('Notes')}</span><p>{selected.notes}</p></div>}

            {isOwner && !selected.ownerAuthorized && !['REJECTED', 'COMPLETED', 'CANCELLED'].includes(selected.status) && (
              <div className="detail-actions-section">
                <h3>{t('Owner action')}</h3>
                <button className="button button-primary" type="button" onClick={authorizeOwner} disabled={actionLoading}>
                  {actionLoading ? t('Saving...') : t('Authorize move')}
                </button>
              </div>
            )}

            {isAdmin && !['REJECTED', 'COMPLETED', 'CANCELLED'].includes(selected.status) && (
              <div className="detail-actions-section">
                <h3>{t('Administrator actions')}</h3>
                {selected.status === 'PENDING_APPROVAL' && !selected.ownerAuthorized && selected.residentType && selected.residentType !== 'OWNER' && (
                  <div className="feedback feedback-warning">{t('Owner authorization is required before approval.')}</div>
                )}
                <button className="button button-secondary" type="button" onClick={() => setStatusOpen(true)} disabled={actionLoading}>{t('Change status')}</button>
                <div className="form-grid-2">
                  {nextAdminAction(selected.status).map((status) => (
                    <button key={status} className={`button ${status === 'CANCELLED' || status === 'REJECTED' ? 'button-danger' : 'button-primary'} button-small`} type="button" onClick={() => openStatus(status)} disabled={actionLoading}>{t(status)}</button>
                  ))}
                </div>
              </div>
            )}

            {actionError && <div className="feedback feedback-error" role="alert">{actionError}</div>}
          </div>
        ) : null}
      </Modal>

      <Modal open={statusOpen} title={t('Update move request status')} onClose={() => !actionLoading && setStatusOpen(false)}>
        <form className="entity-form" onSubmit={submitStatus}>
          {actionError && <div className="modal-feedback feedback feedback-error" role="alert">{actionError}</div>}
          <label className="form-field"><span>{t('Status')}</span><select value={statusForm.status} onChange={(event) => setStatusForm((current) => ({ ...current, status: event.target.value }))} disabled={actionLoading} required><option value="">{t('Select status')}</option>{(selected ? nextAdminAction(selected.status) : STATUSES).map((status) => <option key={status} value={status}>{t(status)}</option>)}</select></label>
          <div className="form-grid-2">
            <label className="form-field"><span>{t('Scheduled start')}</span><input type="datetime-local" value={statusForm.scheduledStart} onChange={(event) => setStatusForm((current) => ({ ...current, scheduledStart: event.target.value }))} disabled={actionLoading}/></label>
            <label className="form-field"><span>{t('Scheduled end')}</span><input type="datetime-local" value={statusForm.scheduledEnd} onChange={(event) => setStatusForm((current) => ({ ...current, scheduledEnd: event.target.value }))} disabled={actionLoading}/></label>
          </div>
          <label className="form-field"><span>{t('Notes')}</span><textarea value={statusForm.notes} onChange={(event) => setStatusForm((current) => ({ ...current, notes: event.target.value }))} rows={5} maxLength={10000} disabled={actionLoading}/></label>
          <div className="entity-form-actions"><button className="button button-secondary" type="button" onClick={() => setStatusOpen(false)} disabled={actionLoading}>{t('Cancel')}</button><button className="button button-primary" type="submit" disabled={actionLoading || !statusForm.status}>{actionLoading ? t('Saving...') : t('Save')}</button></div>
        </form>
      </Modal>
    </section>
  )
}

export default MovePage
