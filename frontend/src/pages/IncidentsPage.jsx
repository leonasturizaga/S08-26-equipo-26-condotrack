import { useCallback, useEffect, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import {
  createIncident,
  getAssignableStaff,
  getIncident,
  getIncidentUnitOptions,
  getIncidents,
  updateIncident,
  updateIncidentAssignment,
  updateIncidentStatus,
} from '../api/incidentsApi.js'

const PAGE_SIZE = 20
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

function toLocalDateTimeInput(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  const offsetMs = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16)
}

function fromLocalDateTimeInput(value) {
  return value ? new Date(value).toISOString() : null
}

function translateValue(t, value) {
  return value ? t(value) : '—'
}

function statusClass(status) {
  switch (status) {
    case 'CREATED':
      return 'status-warning'
    case 'ASSIGNED':
    case 'IN_PROGRESS':
      return 'status-info'
    case 'RESOLVED':
    case 'CLOSED':
      return 'status-success'
    case 'CANCELLED':
      return 'status-danger'
    default:
      return 'status-muted'
  }
}

function severityClass(severity) {
  switch (severity) {
    case 'CRITICAL':
    case 'HIGH':
      return 'status-danger'
    case 'MEDIUM':
      return 'status-warning'
    case 'LOW':
      return 'status-success'
    default:
      return 'status-muted'
  }
}

function createDefaultCreateForm() {
  return {
    unitId: '',
    title: '',
    description: '',
    severity: 'MEDIUM',
    occurredAt: '',
  }
}

function createDefaultEditForm() {
  return {
    title: '',
    description: '',
    severity: 'MEDIUM',
    occurredAt: '',
  }
}

function IncidentForm({ form, onChange, unitOptions, error, saving, onSubmit, onCancel, t, mode = 'create' }) {
  return (
    <form className="entity-form" onSubmit={onSubmit}>
      {error && <div className="modal-feedback feedback feedback-error" role="alert">{error}</div>}

      {mode === 'create' && (
        <label className="form-field">
          <span>{t('Unit')}</span>
          <select name="unitId" value={form.unitId} onChange={onChange} disabled={saving} required>
            <option value="">{t('Select unit')}</option>
            {unitOptions.map((unit) => (
              <option key={unit.unitId} value={unit.unitId}>
                {unit.buildingCode} · {unit.unitNumber}
              </option>
            ))}
          </select>
        </label>
      )}

      <div className="form-grid-2">
        <label className="form-field">
          <span>{t('Severity')}</span>
          <select name="severity" value={form.severity} onChange={onChange} disabled={saving} required>
            {SEVERITIES.map((severity) => (
              <option key={severity} value={severity}>{t(severity)}</option>
            ))}
          </select>
        </label>

        <label className="form-field">
          <span>{t('Occurred at')}</span>
          <input
            type="datetime-local"
            name="occurredAt"
            value={form.occurredAt}
            onChange={onChange}
            disabled={saving}
          />
        </label>
      </div>

      <label className="form-field">
        <span>{t('Title')}</span>
        <input type="text" name="title" value={form.title} onChange={onChange} maxLength={200} disabled={saving} required />
      </label>

      <label className="form-field">
        <span>{t('Description')}</span>
        <textarea name="description" value={form.description} onChange={onChange} rows={6} maxLength={10000} disabled={saving} required />
      </label>

      <div className="entity-form-actions">
        <button className="button button-secondary" type="button" onClick={onCancel} disabled={saving}>{t('Cancel')}</button>
        <button className="button button-primary" type="submit" disabled={saving}>
          {saving ? t('Saving...') : t('Save')}
        </button>
      </div>
    </form>
  )
}

function IncidentsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const role = user?.roles?.[0] || user?.role

  const canCreate = ['ADMINISTRATOR', 'RECEPTION', 'RESIDENT'].includes(role)
  const canManage = role === 'ADMINISTRATOR'
  const canProviderUpdate = role === 'PROVIDER'
  const canList = ['ADMINISTRATOR', 'RESIDENT', 'OWNER', 'PROVIDER'].includes(role)
  const canAccess = canCreate || canList

  const [incidents, setIncidents] = useState([])
  const [unitOptions, setUnitOptions] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [optionsLoading, setOptionsLoading] = useState(false)
  const [error, setError] = useState('')
  const [optionsError, setOptionsError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [createForm, setCreateForm] = useState(createDefaultCreateForm())
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const [selectedIncident, setSelectedIncident] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [editMode, setEditMode] = useState(false)
  const [editForm, setEditForm] = useState(createDefaultEditForm())
  const [staffOptions, setStaffOptions] = useState([])
  const [staffLoading, setStaffLoading] = useState(false)
  const [assignedStaffId, setAssignedStaffId] = useState('')
  const [statusTarget, setStatusTarget] = useState('')
  const [statusResolution, setStatusResolution] = useState('')
  const [actionLoading, setActionLoading] = useState(false)

  const loadIncidents = useCallback(async (targetPage = 0) => {
    if (!canList) return

    setLoading(true)
    setError('')

    try {
      const response = await getIncidents(targetPage, PAGE_SIZE)
      setIncidents(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? targetPage)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load incidents.'))
      setIncidents([])
    } finally {
      setLoading(false)
    }
  }, [canList, t])

  const loadUnitOptions = useCallback(async () => {
    if (!canCreate) return

    setOptionsLoading(true)
    setOptionsError('')

    try {
      const response = await getIncidentUnitOptions()
      setUnitOptions(Array.isArray(response) ? response : [])
    } catch (requestError) {
      setOptionsError(requestError.message || t('Unable to load incident units.'))
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, t])

  useEffect(() => {
    if (canList) {
      loadIncidents(0)
    }
  }, [canList, loadIncidents])

  useEffect(() => {
    loadUnitOptions()
  }, [loadUnitOptions])

  const resetCreate = () => {
    setCreateForm(createDefaultCreateForm())
    setFormError('')
  }

  const openCreate = () => {
    resetCreate()
    setSuccessMessage('')
    setShowCreate(true)
  }

  const closeCreate = () => {
    if (!saving) {
      setShowCreate(false)
      resetCreate()
    }
  }

  const handleCreateChange = (event) => {
    const { name, value } = event.target
    setCreateForm((current) => ({ ...current, [name]: value }))
  }

  const handleCreate = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await createIncident({
        unitId: createForm.unitId,
        title: createForm.title.trim(),
        description: createForm.description.trim(),
        severity: createForm.severity,
        occurredAt: fromLocalDateTimeInput(createForm.occurredAt),
      })

      setShowCreate(false)
      resetCreate()
      setSuccessMessage(t('Incident created successfully.'))
      if (canList) {
        await loadIncidents(0)
      }
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to create incident.'))
    } finally {
      setSaving(false)
    }
  }

  const openIncident = async (incidentId) => {
    setSelectedIncident(null)
    setDetailLoading(true)
    setDetailError('')
    setEditMode(false)
    setStatusTarget('')
    setStatusResolution('')
    setStaffOptions([])
    setAssignedStaffId('')

    try {
      const response = await getIncident(incidentId)
      if (!response || typeof response !== 'object') {
        throw new Error(t('The incident details could not be loaded.'))
      }

      setSelectedIncident(response)
      setEditForm({
        title: response.title || '',
        description: response.description || '',
        severity: response.severity || 'MEDIUM',
        occurredAt: toLocalDateTimeInput(response.occurredAt),
      })
      setAssignedStaffId(response.assignedToStaffId || '')

      if (canManage) {
        setStaffLoading(true)
        try {
          const staff = await getAssignableStaff(response.buildingId)
          setStaffOptions(Array.isArray(staff) ? staff : [])
        } catch (staffError) {
          setDetailError(staffError.message || t('Unable to load assignable staff.'))
        } finally {
          setStaffLoading(false)
        }
      }
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load incident details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (!detailLoading && !actionLoading) {
      setSelectedIncident(null)
      setDetailError('')
      setEditMode(false)
      setStatusTarget('')
    }
  }

  const saveEdit = async (event) => {
    event.preventDefault()
    if (!selectedIncident?.id) return

    setActionLoading(true)
    setDetailError('')
    setSuccessMessage('')

    try {
      const updated = await updateIncident(selectedIncident.id, {
        title: editForm.title.trim(),
        description: editForm.description.trim(),
        severity: editForm.severity,
        occurredAt: fromLocalDateTimeInput(editForm.occurredAt),
      })
      setSelectedIncident(updated)
      setEditMode(false)
      setSuccessMessage(t('Incident updated successfully.'))
      await loadIncidents(page)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to update incident.'))
    } finally {
      setActionLoading(false)
    }
  }

  const saveAssignment = async () => {
    if (!selectedIncident?.id) return

    setActionLoading(true)
    setDetailError('')
    setSuccessMessage('')

    try {
      const updated = await updateIncidentAssignment(selectedIncident.id, assignedStaffId || null)
      setSelectedIncident(updated)
      setSuccessMessage(t('Incident assignment updated successfully.'))
      await loadIncidents(page)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to update incident assignment.'))
    } finally {
      setActionLoading(false)
    }
  }

  const submitStatus = async (targetStatus) => {
    if (!selectedIncident?.id || !targetStatus) return

    if (targetStatus === 'RESOLVED' && !statusResolution.trim()) {
      setStatusTarget(targetStatus)
      setDetailError(t('A resolution is required before resolving the incident.'))
      return
    }

    setActionLoading(true)
    setDetailError('')
    setSuccessMessage('')

    try {
      const updated = await updateIncidentStatus(
        selectedIncident.id,
        targetStatus,
        statusResolution.trim() || null,
      )
      setSelectedIncident(updated)
      setStatusTarget('')
      setStatusResolution('')
      setSuccessMessage(t('Incident status updated successfully.'))
      await loadIncidents(page)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to update incident status.'))
    } finally {
      setActionLoading(false)
    }
  }

  const availableStatusActions = (() => {
    if (!selectedIncident?.status) return []

    if (canProviderUpdate) {
      if (selectedIncident.status === 'ASSIGNED') return ['IN_PROGRESS']
      if (selectedIncident.status === 'IN_PROGRESS') return ['RESOLVED']
      if (selectedIncident.status === 'RESOLVED') return ['CLOSED']
      return []
    }

    if (!canManage) return []

    if (selectedIncident.status === 'ASSIGNED') return ['IN_PROGRESS', 'CANCELLED']
    if (selectedIncident.status === 'IN_PROGRESS') return ['RESOLVED', 'CANCELLED']
    if (selectedIncident.status === 'RESOLVED') return ['CLOSED', 'IN_PROGRESS']
    if (selectedIncident.status === 'CREATED') return ['CANCELLED']
    return []
  })()

  if (!canAccess) {
    return (
      <section className="module-page">
        <div className="module-page-header">
          <div>
            <p className="eyebrow">{t('INCIDENTS')}</p>
            <h2>{t('Incidents')}</h2>
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
          <p className="eyebrow">{t('INCIDENTS')}</p>
          <h2>{t('Incidents')}</h2>
          <p className="muted">{t('Report, track and resolve operational incidents with full traceability.')}</p>
        </div>
        {canCreate && (
          <button className="button button-primary" type="button" onClick={openCreate}>
            + {t('Report incident')}
          </button>
        )}
      </div>

      {successMessage && <div className="feedback feedback-success" role="status">{successMessage}</div>}
      {error && <div className="feedback feedback-error" role="alert">{error}</div>}
      {optionsError && <div className="feedback feedback-warning" role="alert">{optionsError}</div>}

      {!canList ? (
        <section className="data-panel">
          <div className="module-empty-state compact-empty-state">
            <h3>{t('Incident reporting available')}</h3>
            <p className="muted">{t('Reception can report an incident. The incident directory is available to authorized management, resident, owner and provider roles.')}</p>
          </div>
        </section>
      ) : (
        <section className="data-panel">
          <div className="data-panel-header">
            <div>
              <p className="eyebrow">{t('INCIDENT DIRECTORY')}</p>
              <h3>{t('Incidents')}</h3>
            </div>
            <span className="status-pill">{totalElements} {t('incidents')}</span>
          </div>

          {loading ? (
            <div className="module-empty-state compact-empty-state">{t('Loading...')}</div>
          ) : incidents.length === 0 ? (
            <div className="module-empty-state compact-empty-state">
              <h3>{t('No incidents found')}</h3>
              <p className="muted">{t('There are no incident records available for your role.')}</p>
            </div>
          ) : (
            <div className="table-scroll">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>{t('Incident')}</th>
                    <th>{t('Unit')}</th>
                    <th>{t('Severity')}</th>
                    <th>{t('Status')}</th>
                    <th>{t('Assigned to')}</th>
                    <th>{t('Created')}</th>
                    <th>{t('Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {incidents.map((incident) => (
                    <tr key={incident.id}>
                      <td>
                        <strong>{incident.title}</strong>
                        <span className="table-meta">{incident.buildingCode} · {incident.description?.slice(0, 70)}{incident.description?.length > 70 ? '…' : ''}</span>
                      </td>
                      <td>{incident.unitNumber}</td>
                      <td><span className={`status-badge ${severityClass(incident.severity)}`}>{translateValue(t, incident.severity)}</span></td>
                      <td><span className={`status-badge ${statusClass(incident.status)}`}>{translateValue(t, incident.status)}</span></td>
                      <td>{incident.assignedToName || '—'}</td>
                      <td>{formatDateTime(incident.createdAt)}</td>
                      <td>
                        <button className="button button-ghost" type="button" onClick={() => openIncident(incident.id)}>{t('View')}</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {totalPages > 1 && (
            <div className="pagination-bar">
              <span>{t('Page')} {page + 1} {t('of')} {totalPages}</span>
              <div className="table-actions">
                <button className="button button-secondary button-small" type="button" onClick={() => loadIncidents(page - 1)} disabled={page <= 0 || loading}>{t('Previous')}</button>
                <button className="button button-secondary button-small" type="button" onClick={() => loadIncidents(page + 1)} disabled={page >= totalPages - 1 || loading}>{t('Next')}</button>
              </div>
            </div>
          )}
        </section>
      )}

      <Modal open={showCreate} onClose={closeCreate} eyebrow={t('INCIDENTS')} title={t('Report incident')} size="large">
        {optionsLoading ? (
          <div className="modal-loading-state">{t('Loading units...')}</div>
        ) : (
          <IncidentForm
            mode="create"
            form={createForm}
            onChange={handleCreateChange}
            unitOptions={unitOptions}
            error={formError || optionsError}
            saving={saving}
            onSubmit={handleCreate}
            onCancel={closeCreate}
            t={t}
          />
        )}
      </Modal>

      <Modal
        open={detailLoading || Boolean(selectedIncident) || Boolean(detailError)}
        onClose={closeDetails}
        eyebrow={t('INCIDENTS')}
        title={editMode ? t('Edit incident') : t('Incident details')}
        size="large"
      >
        {detailLoading ? (
          <div className="modal-loading-state">{t('Loading...')}</div>
        ) : detailError && !selectedIncident ? (
          <div className="modal-feedback feedback feedback-error" role="alert">{detailError}</div>
        ) : selectedIncident ? (
          editMode ? (
            <IncidentForm
              mode="edit"
              form={editForm}
              onChange={(event) => setEditForm((current) => ({ ...current, [event.target.name]: event.target.value }))}
              unitOptions={[]}
              error={detailError}
              saving={actionLoading}
              onSubmit={saveEdit}
              onCancel={() => setEditMode(false)}
              t={t}
            />
          ) : (
            <div>
              {detailError && <div className="modal-feedback feedback feedback-error" role="alert">{detailError}</div>}

              <div className="details-grid">
                <div><span>{t('Incident')}</span><strong>{selectedIncident.title}</strong></div>
                <div><span>{t('Status')}</span><strong><span className={`status-badge ${statusClass(selectedIncident.status)}`}>{translateValue(t, selectedIncident.status)}</span></strong></div>
                <div><span>{t('Severity')}</span><strong><span className={`status-badge ${severityClass(selectedIncident.severity)}`}>{translateValue(t, selectedIncident.severity)}</span></strong></div>
                <div><span>{t('Unit')}</span><strong>{selectedIncident.buildingCode} · {selectedIncident.unitNumber}</strong></div>
                <div><span>{t('Reported by')}</span><strong>{selectedIncident.reportedByName || '—'}</strong></div>
                <div><span>{t('Assigned to')}</span><strong>{selectedIncident.assignedToName || t('Unassigned')}</strong></div>
                <div><span>{t('Occurred at')}</span><strong>{formatDateTime(selectedIncident.occurredAt)}</strong></div>
                <div><span>{t('Created')}</span><strong>{formatDateTime(selectedIncident.createdAt)}</strong></div>
                <div><span>{t('Resolved at')}</span><strong>{formatDateTime(selectedIncident.resolvedAt)}</strong></div>
              </div>

              <div className="panel-section" style={{ marginTop: '20px' }}>
                <p className="form-section-label">{t('Description')}</p>
                <p className="muted" style={{ whiteSpace: 'pre-wrap' }}>{selectedIncident.description}</p>
              </div>

              {selectedIncident.resolution && (
                <div className="panel-section" style={{ marginTop: '16px' }}>
                  <p className="form-section-label">{t('Resolution')}</p>
                  <p className="muted" style={{ whiteSpace: 'pre-wrap' }}>{selectedIncident.resolution}</p>
                </div>
              )}

              {canManage && (
                <div className="panel-section" style={{ marginTop: '20px' }}>
                  <p className="form-section-label">{t('Assignment')}</p>
                  <div className="form-grid-2">
                    <label className="form-field">
                      <span>{t('Assign to')}</span>
                      <select value={assignedStaffId} onChange={(event) => setAssignedStaffId(event.target.value)} disabled={actionLoading || staffLoading}>
                        <option value="">{t('Unassigned')}</option>
                        {staffOptions.map((staff) => (
                          <option key={staff.staffId} value={staff.staffId}>
                            {staff.firstName} {staff.lastName} — {t(staff.staffType)}{staff.employeeCode ? ` (${staff.employeeCode})` : ''}
                          </option>
                        ))}
                      </select>
                    </label>
                    <div className="form-align-end">
                      <button className="button button-secondary" type="button" onClick={saveAssignment} disabled={actionLoading || staffLoading}>
                        {actionLoading ? t('Saving...') : t('Save assignment')}
                      </button>
                    </div>
                  </div>
                  {staffLoading && <p className="muted">{t('Loading assignable staff...')}</p>}
                  {!staffLoading && staffOptions.length === 0 && <p className="muted">{t('No active staff are available for this building.')}</p>}
                </div>
              )}

              {statusTarget === 'RESOLVED' && (
                <div className="panel-section" style={{ marginTop: '20px' }}>
                  <label className="form-field">
                    <span>{t('Resolution')}</span>
                    <textarea value={statusResolution} onChange={(event) => setStatusResolution(event.target.value)} rows={5} maxLength={10000} disabled={actionLoading} required />
                  </label>
                  <div className="entity-form-actions">
                    <button className="button button-secondary" type="button" onClick={() => setStatusTarget('')} disabled={actionLoading}>{t('Cancel')}</button>
                    <button className="button button-primary" type="button" onClick={() => submitStatus('RESOLVED')} disabled={actionLoading || !statusResolution.trim()}>
                      {actionLoading ? t('Saving...') : t('Resolve incident')}
                    </button>
                  </div>
                </div>
              )}

              <div className="entity-form-actions" style={{ marginTop: '24px' }}>
                {canManage && (
                  <button className="button button-secondary" type="button" onClick={() => setEditMode(true)} disabled={actionLoading}>{t('Edit')}</button>
                )}
                {availableStatusActions.filter((status) => status !== 'RESOLVED').map((status) => (
                  <button
                    key={status}
                    className={status === 'CANCELLED' ? 'button button-danger' : 'button button-primary'}
                    type="button"
                    onClick={() => submitStatus(status)}
                    disabled={actionLoading}
                  >
                    {t(status === 'IN_PROGRESS' ? 'Start work' : status === 'CLOSED' ? 'Close incident' : status === 'CANCELLED' ? 'Cancel incident' : status)}
                  </button>
                ))}
                {availableStatusActions.includes('RESOLVED') && statusTarget !== 'RESOLVED' && (
                  <button className="button button-primary" type="button" onClick={() => setStatusTarget('RESOLVED')} disabled={actionLoading}>{t('Resolve incident')}</button>
                )}
              </div>
            </div>
          )
        ) : (
          <div className="modal-loading-state">{t('Loading...')}</div>
        )}
      </Modal>
    </section>
  )
}

export default IncidentsPage
