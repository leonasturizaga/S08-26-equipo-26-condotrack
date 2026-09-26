import { useCallback, useEffect, useMemo, useState } from 'react'
import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import { getUnits } from '../api/unitsApi.js'
import {
  createResidentAssignment,
  getResident,
  getResidents,
  reassignResident,
  updateResident,
} from '../api/residentsApi.js'
import { getUsers } from '../api/usersApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 20

const residentTypes = [
  'OWNER',
  'TENANT',
  'OCCUPANT',
  'AUTHORIZED_RESIDENT',
]

const emptyProfileForm = {
  firstName: '',
  lastName: '',
  phone: '',
}

const emptyAssignmentForm = {
  userId: '',
  unitId: '',
  residentType: 'TENANT',
  moveInDate: '',
  moveOutDate: '',
  primaryContact: false,
}

function ResidentTypeLabel({ value, t }) {
  const labels = {
    OWNER: t('Owner'),
    TENANT: t('Tenant'),
    OCCUPANT: t('Occupant'),
    AUTHORIZED_RESIDENT: t('Authorized resident'),
  }

  return labels[value] || value
}

function ResidentsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const role = user?.roles?.[0] || user?.role
  const isAdministrator = role === 'ADMINISTRATOR'
  const canEdit = ['ADMINISTRATOR', 'RESIDENT', 'OWNER'].includes(role)

  const [residents, setResidents] = useState([])
  const [users, setUsers] = useState([])
  const [units, setUnits] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [loadingOptions, setLoadingOptions] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [selectedResident, setSelectedResident] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const [profileForm, setProfileForm] = useState(emptyProfileForm)
  const [profileResidentId, setProfileResidentId] = useState(null)
  const [showProfileForm, setShowProfileForm] = useState(false)

  const [assignmentForm, setAssignmentForm] = useState(emptyAssignmentForm)
  const [showAssignmentForm, setShowAssignmentForm] = useState(false)

  const [reassignResidentId, setReassignResidentId] = useState(null)
  const [reassignUnitId, setReassignUnitId] = useState('')
  const [showReassignForm, setShowReassignForm] = useState(false)

  const loadResidents = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')

    try {
      const response = await getResidents(pageToLoad, PAGE_SIZE)
      setResidents(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load residents.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  const loadAdminOptions = useCallback(async () => {
    if (!isAdministrator) {
      return
    }

    setLoadingOptions(true)

    try {
      const [usersResponse, unitsResponse] = await Promise.all([
        getUsers(0, 100),
        getUnits(0, 100),
      ])

      setUsers(Array.isArray(usersResponse?.content) ? usersResponse.content : [])
      setUnits(Array.isArray(unitsResponse?.content) ? unitsResponse.content : [])
    } catch (requestError) {
      setError(requestError.message || t('Unable to load resident assignment options.'))
    } finally {
      setLoadingOptions(false)
    }
  }, [isAdministrator, t])

  useEffect(() => {
    loadResidents(0)
  }, [loadResidents])

  useEffect(() => {
    loadAdminOptions()
  }, [loadAdminOptions])

  const eligibleUsers = useMemo(
    () => users.filter(
      (candidate) => candidate.active
        && candidate.roles?.some((candidateRole) => ['RESIDENT', 'OWNER'].includes(candidateRole)),
    ),
    [users],
  )

  const activeUnits = useMemo(
    () => units.filter((unit) => unit.active),
    [units],
  )

  const resetProfileForm = () => {
    setProfileForm(emptyProfileForm)
    setProfileResidentId(null)
    setFormError('')
  }

  const resetAssignmentForm = () => {
    setAssignmentForm(emptyAssignmentForm)
    setFormError('')
  }

  const viewResident = async (residentId) => {
    setSelectedResident(null)
    setDetailError('')
    setDetailLoading(true)
    setError('')

    try {
      const detail = await getResident(residentId)

      if (!detail || typeof detail !== 'object') {
        throw new Error(t('The resident details could not be loaded.'))
      }

      setSelectedResident(detail)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load resident details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (!detailLoading) {
      setSelectedResident(null)
      setDetailError('')
    }
  }

  const startProfileEdit = (resident) => {
    setProfileResidentId(resident.id)
    setProfileForm({
      firstName: resident.firstName || '',
      lastName: resident.lastName || '',
      phone: resident.phone || '',
    })
    setFormError('')
    setSuccessMessage('')
    setShowProfileForm(true)
  }

  const startAssignment = () => {
    resetAssignmentForm()
    setSuccessMessage('')
    setShowAssignmentForm(true)
  }

  const startReassign = (resident) => {
    setReassignResidentId(resident.id)
    setReassignUnitId(resident.unitId || '')
    setFormError('')
    setSuccessMessage('')
    setShowReassignForm(true)
  }

  const closeProfileForm = () => {
    if (!saving) {
      setShowProfileForm(false)
      resetProfileForm()
    }
  }

  const closeAssignmentForm = () => {
    if (!saving) {
      setShowAssignmentForm(false)
      resetAssignmentForm()
    }
  }

  const closeReassignForm = () => {
    if (!saving) {
      setShowReassignForm(false)
      setReassignResidentId(null)
      setReassignUnitId('')
      setFormError('')
    }
  }

  const handleProfileChange = (event) => {
    const { name, value } = event.target
    setProfileForm((current) => ({ ...current, [name]: value }))
  }

  const handleAssignmentChange = (event) => {
    const { name, value, type, checked } = event.target
    setAssignmentForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const handleProfileSubmit = async (event) => {
    event.preventDefault()
    if (!profileResidentId) {
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await updateResident(profileResidentId, {
        firstName: profileForm.firstName.trim(),
        lastName: profileForm.lastName.trim(),
        phone: profileForm.phone.trim() || null,
      })
      setShowProfileForm(false)
      resetProfileForm()
      setSuccessMessage(t('Resident profile updated successfully.'))
      await loadResidents(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to update resident profile.'))
    } finally {
      setSaving(false)
    }
  }

  const handleAssignmentSubmit = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await createResidentAssignment({
        userId: assignmentForm.userId,
        unitId: assignmentForm.unitId,
        residentType: assignmentForm.residentType,
        moveInDate: assignmentForm.moveInDate || null,
        moveOutDate: assignmentForm.moveOutDate || null,
        primaryContact: assignmentForm.primaryContact,
      })

      setShowAssignmentForm(false)
      resetAssignmentForm()
      setSuccessMessage(t('Resident assignment created successfully.'))
      await loadResidents(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to create resident assignment.'))
    } finally {
      setSaving(false)
    }
  }

  const handleReassignSubmit = async (event) => {
    event.preventDefault()
    if (!reassignResidentId || !reassignUnitId) {
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await reassignResident(reassignResidentId, reassignUnitId)
      setShowReassignForm(false)
      setReassignResidentId(null)
      setReassignUnitId('')
      setSuccessMessage(t('Resident unit assignment updated successfully.'))
      await loadResidents(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to update the resident unit assignment.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('RESIDENTS / OWNERS')}</p>
          <h2>{t('Residents / Owners')}</h2>
          <p className="muted">
            {t('View residents and maintain their profile and unit relationships.')}
          </p>
        </div>

        {isAdministrator && (
          <button className="button button-primary" type="button" onClick={startAssignment}>
            + {t('Assign resident')}
          </button>
        )}
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

      <section className="data-panel">
        <div className="data-panel-header">
          <div>
            <p className="eyebrow">{t('RESIDENT DIRECTORY')}</p>
            <h3>{t('Connected residents')}</h3>
          </div>

          <span className="status-pill">
            {totalElements} {t('residents')}
          </span>
        </div>

        {loading ? (
          <div className="module-empty-state compact-empty-state">
            {t('Loading...')}
          </div>
        ) : residents.length === 0 ? (
          <div className="module-empty-state compact-empty-state">
            <h3>{t('No residents found')}</h3>
            <p className="muted">
              {t('There are no resident records available for your role.')}
            </p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Resident')}</th>
                  <th>{t('Unit')}</th>
                  <th>{t('Type')}</th>
                  <th>{t('Move in')}</th>
                  <th>{t('Primary contact')}</th>
                  <th>{t('Status')}</th>
                  <th className="table-actions-column">{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {residents.map((resident) => (
                  <tr key={resident.id}>
                    <td>
                      <strong>{resident.firstName} {resident.lastName}</strong>
                      <span className="table-meta">{resident.email}</span>
                    </td>
                    <td>{resident.unitNumber}</td>
                    <td>
                      <span className="status-badge status-info">
                        <ResidentTypeLabel value={resident.residentType} t={t} />
                      </span>
                    </td>
                    <td>{resident.moveInDate || '—'}</td>
                    <td>{resident.primaryContact ? t('Yes') : t('No')}</td>
                    <td>
                      <span className={`status-badge ${resident.active ? 'status-success' : 'status-muted'}`}>
                        {resident.active ? t('Active') : t('Inactive')}
                      </span>
                    </td>
                    <td>
                      <TableActions moreLabel={t('More')}>
                        <TableAction icon="eye" label={t('View')} variant="view" onClick={() => viewResident(resident.id)} />
                        {canEdit && <TableAction icon="edit" label={t('Edit')} variant="edit" onClick={() => startProfileEdit(resident)} />}
                        {isAdministrator && resident.active && <TableAction icon="shuffle" label={t('Reassign unit')} variant="edit" onClick={() => startReassign(resident)} />}
                      </TableActions>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="pagination-bar">
          <span className="table-meta">
            {t('Page')} {page + 1} {t('of')} {Math.max(totalPages, 1)}
          </span>

          <div className="pagination-actions">
            <button
              className="button button-secondary"
              type="button"
              disabled={page <= 0 || loading}
              onClick={() => loadResidents(page - 1)}
            >
              {t('Previous')}
            </button>
            <button
              className="button button-secondary"
              type="button"
              disabled={page + 1 >= totalPages || loading}
              onClick={() => loadResidents(page + 1)}
            >
              {t('Next')}
            </button>
          </div>
        </div>
      </section>

      <Modal
        open={Boolean(selectedResident) || detailLoading || Boolean(detailError)}
        onClose={closeDetails}
        eyebrow={t('RESIDENT DETAILS')}
        title={selectedResident ? `${selectedResident.firstName} ${selectedResident.lastName}` : t('Loading...')}
        size="large"
        closeOnBackdrop={!detailLoading}
      >
        {detailLoading ? (
          <div className="modal-loading-state">{t('Loading resident details...')}</div>
        ) : selectedResident ? (
          <div className="detail-grid">
            <div><span>{t('Email')}</span><strong>{selectedResident.email}</strong></div>
            <div><span>{t('Phone')}</span><strong>{selectedResident.phone || '—'}</strong></div>
            <div><span>{t('Unit')}</span><strong>{selectedResident.unitNumber}</strong></div>
            <div><span>{t('Resident type')}</span><strong><ResidentTypeLabel value={selectedResident.residentType} t={t} /></strong></div>
            <div><span>{t('Move in')}</span><strong>{selectedResident.moveInDate || '—'}</strong></div>
            <div><span>{t('Move out')}</span><strong>{selectedResident.moveOutDate || '—'}</strong></div>
            <div><span>{t('Primary contact')}</span><strong>{selectedResident.primaryContact ? t('Yes') : t('No')}</strong></div>
            <div><span>{t('Status')}</span><strong>{selectedResident.active ? t('Active') : t('Inactive')}</strong></div>
          </div>
        ) : (
          <div className="feedback feedback-error" role="alert">
            {detailError || t('Unable to load resident details.')}
          </div>
        )}
      </Modal>

      <Modal
        open={showProfileForm && Boolean(profileResidentId)}
        onClose={closeProfileForm}
        eyebrow={t('EDIT RESIDENT PROFILE')}
        title={t('Edit resident profile')}
        size="medium"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div className="feedback feedback-error modal-feedback" role="alert">{formError}</div>
        )}
        <form className="entity-form" onSubmit={handleProfileSubmit}>
          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('First name')}</span>
              <input name="firstName" value={profileForm.firstName} onChange={handleProfileChange} maxLength={100} required />
            </label>
            <label className="form-field">
              <span>{t('Last name')}</span>
              <input name="lastName" value={profileForm.lastName} onChange={handleProfileChange} maxLength={100} required />
            </label>
          </div>
          <label className="form-field">
            <span>{t('Phone')}</span>
            <input name="phone" value={profileForm.phone} onChange={handleProfileChange} maxLength={50} />
          </label>
          <div className="entity-form-actions">
            <button className="button button-primary" type="submit" disabled={saving}>
              {saving ? t('Saving...') : t('Save profile')}
            </button>
            <button className="button button-secondary" type="button" onClick={closeProfileForm} disabled={saving}>
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={showAssignmentForm && isAdministrator}
        onClose={closeAssignmentForm}
        eyebrow={t('NEW RESIDENT ASSIGNMENT')}
        title={t('Assign resident to unit')}
        size="large"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div className="feedback feedback-error modal-feedback" role="alert">{formError}</div>
        )}
        <form className="entity-form" onSubmit={handleAssignmentSubmit}>
          {loadingOptions ? (
            <div className="modal-loading-state">{t('Loading assignment options...')}</div>
          ) : (
            <>
              <div className="form-grid-2">
                <label className="form-field">
                  <span>{t('User')}</span>
                  <select name="userId" value={assignmentForm.userId} onChange={handleAssignmentChange} required>
                    <option value="">{t('Select user')}</option>
                    {eligibleUsers.map((candidate) => (
                      <option key={candidate.id} value={candidate.id}>
                        {candidate.firstName} {candidate.lastName} — {candidate.email}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="form-field">
                  <span>{t('Unit')}</span>
                  <select name="unitId" value={assignmentForm.unitId} onChange={handleAssignmentChange} required>
                    <option value="">{t('Select active unit')}</option>
                    {activeUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>
                        {unit.unitNumber} — {unit.unitType}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <div className="form-grid-3">
                <label className="form-field">
                  <span>{t('Resident type')}</span>
                  <select name="residentType" value={assignmentForm.residentType} onChange={handleAssignmentChange} required>
                    {residentTypes.map((type) => (
                      <option key={type} value={type}>
                        <ResidentTypeLabel value={type} t={t} />
                      </option>
                    ))}
                  </select>
                </label>
                <label className="form-field">
                  <span>{t('Move in')}</span>
                  <input type="date" name="moveInDate" value={assignmentForm.moveInDate} onChange={handleAssignmentChange} />
                </label>
                <label className="form-field">
                  <span>{t('Move out')}</span>
                  <input type="date" name="moveOutDate" value={assignmentForm.moveOutDate} onChange={handleAssignmentChange} />
                </label>
              </div>
              <label className="checkbox-field">
                <input type="checkbox" name="primaryContact" checked={assignmentForm.primaryContact} onChange={handleAssignmentChange} />
                <span>{t('Primary contact')}</span>
              </label>
              <div className="entity-form-actions">
                <button className="button button-primary" type="submit" disabled={saving}>
                  {saving ? t('Saving...') : t('Assign resident')}
                </button>
                <button className="button button-secondary" type="button" onClick={closeAssignmentForm} disabled={saving}>
                  {t('Cancel')}
                </button>
              </div>
            </>
          )}
        </form>
      </Modal>

      <Modal
        open={showReassignForm && isAdministrator}
        onClose={closeReassignForm}
        eyebrow={t('REASSIGN UNIT')}
        title={t('Change resident unit')}
        size="medium"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div className="feedback feedback-error modal-feedback" role="alert">{formError}</div>
        )}
        <form className="entity-form" onSubmit={handleReassignSubmit}>
          <label className="form-field">
            <span>{t('New active unit')}</span>
            <select value={reassignUnitId} onChange={(event) => setReassignUnitId(event.target.value)} required>
              <option value="">{t('Select active unit')}</option>
              {activeUnits.map((unit) => (
                <option key={unit.id} value={unit.id}>{unit.unitNumber} — {unit.unitType}</option>
              ))}
            </select>
          </label>
          <div className="entity-form-actions">
            <button className="button button-primary" type="submit" disabled={saving}>
              {saving ? t('Saving...') : t('Save assignment')}
            </button>
            <button className="button button-secondary" type="button" onClick={closeReassignForm} disabled={saving}>
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  )
}

export default ResidentsPage
