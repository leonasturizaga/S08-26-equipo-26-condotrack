//--------------------- M24.8.2 ----------------------
import { useCallback, useEffect, useState } from 'react'

import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import {
  createUser,
  getUser,
  getUsers,
  updateUser,
  updateUserRoles,
  updateUserStatus,
} from '../api/usersApi.js'
import { apiRequest } from '../api/apiClient.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { roleLabels } from '../auth/rolePermissions.js'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 20

const roleOptions = [
  'ADMINISTRATOR',
  'RECEPTION',
  'RESIDENT',
  'OWNER',
  'PROVIDER',
]

const staffTypeOptions = [
  'OTHER',
  'MAINTENANCE',
  'SECURITY',
  'RECEPTION',
  'ADMINISTRATOR',
]

const emptyCreateForm = {
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phone: '',
  roles: ['RESIDENT'],
}

const emptyEditForm = {
  email: '',
  firstName: '',
  lastName: '',
  phone: '',
}

const emptyStaffForm = {
  buildingId: '',
  staffType: 'OTHER',
  employeeCode: '',
  active: true,
}

function UserManagementPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const currentUserId = user?.id

  const [users, setUsers] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [selectedUser, setSelectedUser] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createForm, setCreateForm] = useState(emptyCreateForm)

  const [showEditForm, setShowEditForm] = useState(false)
  const [editUserId, setEditUserId] = useState(null)
  const [editForm, setEditForm] = useState(emptyEditForm)

  const [showRolesForm, setShowRolesForm] = useState(false)
  const [rolesUserId, setRolesUserId] = useState(null)
  const [rolesForm, setRolesForm] = useState([])
  const [staffForm, setStaffForm] = useState(emptyStaffForm)
  const [buildings, setBuildings] = useState([])
  const [buildingsLoading, setBuildingsLoading] = useState(false)
  const [buildingsError, setBuildingsError] = useState('')

  const [statusUser, setStatusUser] = useState(null)

  const loadUsers = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')

    try {
      const response = await getUsers(pageToLoad, PAGE_SIZE)
      setUsers(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load users.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadUsers(0)
  }, [loadUsers])

  const resetCreateForm = () => {
    setCreateForm(emptyCreateForm)
    setFormError('')
  }

  const closeCreateForm = () => {
    if (!saving) {
      setShowCreateForm(false)
      resetCreateForm()
    }
  }

  const viewUser = async (userId) => {
    setSelectedUser(null)
    setDetailError('')
    setDetailLoading(true)
    setError('')

    try {
      const detail = await getUser(userId)

      if (!detail || typeof detail !== 'object') {
        throw new Error(t('The user details could not be loaded.'))
      }

      setSelectedUser(detail)
    } catch (requestError) {
      setDetailError(
        requestError.message || t('Unable to load user details.'),
      )
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (!detailLoading) {
      setSelectedUser(null)
      setDetailError('')
    }
  }

  const startEdit = (account) => {
    setEditUserId(account.id)
    setEditForm({
      email: account.email || '',
      firstName: account.firstName || '',
      lastName: account.lastName || '',
      phone: account.phone || '',
    })
    setFormError('')
    setSuccessMessage('')
    setShowEditForm(true)
  }

  const loadBuildings = async () => {
    setBuildingsLoading(true)
    setBuildingsError('')

    try {
      const response = await apiRequest(
        '/api/buildings?page=0&size=100',
        {
          method: 'GET',
        },
      )

      const activeBuildings = Array.isArray(response?.content)
        ? response.content.filter((building) => building.active !== false)
        : []

      setBuildings(activeBuildings)
    } catch (requestError) {
      setBuildings([])
      setBuildingsError(
        requestError.message || t('Unable to load buildings.'),
      )
    } finally {
      setBuildingsLoading(false)
    }
  }

  const startRoles = async (account) => {
    const currentRoles = Array.isArray(account.roles)
      ? account.roles
      : []

    setRolesUserId(account.id)
    setRolesForm(currentRoles)
    setStaffForm(emptyStaffForm)
    setBuildings([])
    setBuildingsError('')
    setFormError('')
    setSuccessMessage('')
    setShowRolesForm(true)

    if (currentRoles.includes('PROVIDER')) {
      await loadBuildings()
    }
  }

  const closeRolesForm = () => {
    if (!saving) {
      setShowRolesForm(false)
      setRolesUserId(null)
      setRolesForm([])
      setStaffForm(emptyStaffForm)
      setBuildings([])
      setBuildingsError('')
      setFormError('')
    }
  }

  const startStatusChange = (account) => {
    setStatusUser(account)
    setFormError('')
    setSuccessMessage('')
  }

  const closeEditForm = () => {
    if (!saving) {
      setShowEditForm(false)
      setEditUserId(null)
      setEditForm(emptyEditForm)
      setFormError('')
    }
  }

  const handleCreateChange = (event) => {
    const { name, value } = event.target
    setCreateForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  const handleEditChange = (event) => {
    const { name, value } = event.target
    setEditForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  const handleStaffChange = (event) => {
    const { name, value, type, checked } = event.target

    setStaffForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const toggleRole = (role) => {
    setRolesForm((current) => {
      const nextRoles = current.includes(role)
        ? current.filter((currentRole) => currentRole !== role)
        : [...current, role]

      if (
        role === 'PROVIDER' &&
        nextRoles.includes('PROVIDER') &&
        !current.includes('PROVIDER')
      ) {
        loadBuildings()
      }

      return nextRoles
    })
  }

  const handleCreateSubmit = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await createUser({
        email: createForm.email.trim(),
        password: createForm.password,
        firstName: createForm.firstName.trim(),
        lastName: createForm.lastName.trim(),
        phone: createForm.phone.trim() || null,
        roles: createForm.roles,
      })

      setShowCreateForm(false)
      resetCreateForm()
      setSuccessMessage(t('User created successfully.'))
      await loadUsers(page)
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to create user.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handleEditSubmit = async (event) => {
    event.preventDefault()

    if (!editUserId) {
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await updateUser(editUserId, {
        email: editForm.email.trim(),
        firstName: editForm.firstName.trim(),
        lastName: editForm.lastName.trim(),
        phone: editForm.phone.trim() || null,
      })

      setShowEditForm(false)
      setEditUserId(null)
      setEditForm(emptyEditForm)
      setSuccessMessage(t('User profile updated successfully.'))
      await loadUsers(page)
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to update user.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handleRolesSubmit = async (event) => {
    event.preventDefault()

    if (!rolesUserId || rolesForm.length === 0) {
      setFormError(t('Select at least one role.'))
      return
    }

    const isProvider = rolesForm.includes('PROVIDER')

    if (isProvider && staffForm.buildingId) {
      if (!staffForm.staffType) {
        setFormError(t('Select a staff type.'))
        return
      }
    }

    if (
      isProvider &&
      !staffForm.buildingId &&
      (
        staffForm.employeeCode.trim() ||
        staffForm.staffType !== 'OTHER' ||
        staffForm.active !== true
      )
    ) {
      setFormError(
        t('Select a building to save the optional staff assignment.'),
      )
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await updateUserRoles(rolesUserId, rolesForm)

      if (isProvider && staffForm.buildingId) {
        await apiRequest(`/api/users/${rolesUserId}/staff`, {
          method: 'PUT',
          body: {
            buildingId: staffForm.buildingId,
            staffType: staffForm.staffType,
            employeeCode: staffForm.employeeCode.trim() || null,
            active: staffForm.active,
          },
        })
      }

      setShowRolesForm(false)
      setRolesUserId(null)
      setRolesForm([])
      setStaffForm(emptyStaffForm)
      setBuildings([])
      setBuildingsError('')

      setSuccessMessage(
        isProvider && staffForm.buildingId
          ? t('User roles and staff assignment updated successfully.')
          : t('User roles updated successfully.'),
      )

      await loadUsers(page)
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to update user roles.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const confirmStatusChange = async () => {
    if (!statusUser) {
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await updateUserStatus(statusUser.id, !statusUser.active)

      setStatusUser(null)

      setSuccessMessage(
        statusUser.active
          ? t('User deactivated successfully.')
          : t('User activated successfully.'),
      )

      await loadUsers(page)
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to update user status.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const providerSelected = rolesForm.includes('PROVIDER')

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('USER MANAGEMENT')}</p>
          <h2>{t('User Management')}</h2>
          <p className="muted">
            {t('Create application users, maintain their profiles and manage roles and account status.')}
          </p>
        </div>

        <button
          className="button button-primary"
          type="button"
          onClick={() => setShowCreateForm(true)}
        >
          + {t('Add user')}
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

      <section className="data-panel">
        <div className="data-panel-header">
          <div>
            <p className="eyebrow">{t('ACCOUNT DIRECTORY')}</p>
            <h3>{t('Application users')}</h3>
          </div>

          <span className="status-pill">
            {totalElements} {t('users')}
          </span>
        </div>

        {loading ? (
          <div className="module-empty-state compact-empty-state">
            {t('Loading...')}
          </div>
        ) : users.length === 0 ? (
          <div className="module-empty-state compact-empty-state">
            <h3>{t('No users found')}</h3>
            <p className="muted">
              {t('There are no application users available.')}
            </p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('User')}</th>
                  <th>{t('Roles')}</th>
                  <th>{t('Status')}</th>
                  <th className="table-actions-column">{t('Actions')}</th>
                </tr>
              </thead>

              <tbody>
                {users.map((account) => (
                  <tr key={account.id}>
                    <td>
                      <strong>
                        {account.firstName} {account.lastName}
                      </strong>
                      <span className="table-meta">
                        {account.email}
                      </span>
                    </td>

                    <td>
                      <div className="role-chip-list">
                        {account.roles?.map((accountRole) => (
                          <span
                            className="status-badge status-info"
                            key={accountRole}
                          >
                            {t(
                              roleLabels[accountRole] ||
                              accountRole,
                            )}
                          </span>
                        ))}
                      </div>
                    </td>

                    <td>
                      <span
                        className={`status-badge ${
                          account.active
                            ? 'status-success'
                            : 'status-muted'
                        }`}
                      >
                        {account.active
                          ? t('Active')
                          : t('Inactive')}
                      </span>
                    </td>

                    <td>
                      <TableActions moreLabel={t('More')}>
                        <TableAction
                          icon="eye"
                          label={t('View')}
                          variant="view"
                          onClick={() => viewUser(account.id)}
                        />

                        <TableAction
                          icon="edit"
                          label={t('Edit')}
                          variant="edit"
                          onClick={() => startEdit(account)}
                        />

                        <TableAction
                          icon="users"
                          label={t('Roles')}
                          variant="edit"
                          onClick={() => startRoles(account)}
                        />

                        <TableAction
                          icon="refreshCw"
                          label={
                            account.active
                              ? t('Deactivate')
                              : t('Activate')
                          }
                          variant={
                            account.active ? 'delete' : 'view'
                          }
                          onClick={() =>
                            startStatusChange(account)
                          }
                          disabled={
                            account.id === currentUserId
                          }
                          title={
                            account.id === currentUserId
                              ? t(
                                  'You cannot change your own account status.',
                                )
                              : undefined
                          }
                        />
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
            {t('Page')} {page + 1} {t('of')}{' '}
            {Math.max(totalPages, 1)}
          </span>

          <div className="pagination-actions">
            <button
              className="button button-secondary"
              type="button"
              disabled={page <= 0 || loading}
              onClick={() => loadUsers(page - 1)}
            >
              {t('Previous')}
            </button>

            <button
              className="button button-secondary"
              type="button"
              disabled={
                page + 1 >= totalPages || loading
              }
              onClick={() => loadUsers(page + 1)}
            >
              {t('Next')}
            </button>
          </div>
        </div>
      </section>

      <Modal
        open={
          Boolean(selectedUser) ||
          detailLoading ||
          Boolean(detailError)
        }
        onClose={closeDetails}
        eyebrow={t('USER DETAILS')}
        title={
          selectedUser
            ? `${selectedUser.firstName} ${selectedUser.lastName}`
            : t('Loading...')
        }
        size="large"
        closeOnBackdrop={!detailLoading}
      >
        {detailLoading ? (
          <div className="modal-loading-state">
            {t('Loading user details...')}
          </div>
        ) : selectedUser ? (
          <div className="detail-grid">
            <div>
              <span>{t('Email')}</span>
              <strong>{selectedUser.email}</strong>
            </div>

            <div>
              <span>{t('Phone')}</span>
              <strong>{selectedUser.phone || '—'}</strong>
            </div>

            <div>
              <span>{t('Roles')}</span>
              <strong>
                {selectedUser.roles
                  ?.map((accountRole) =>
                    t(
                      roleLabels[accountRole] ||
                      accountRole,
                    ),
                  )
                  .join(', ')}
              </strong>
            </div>

            <div>
              <span>{t('Status')}</span>
              <strong>
                {selectedUser.active
                  ? t('Active')
                  : t('Inactive')}
              </strong>
            </div>
          </div>
        ) : (
          <div
            className="feedback feedback-error"
            role="alert"
          >
            {detailError ||
              t('Unable to load user details.')}
          </div>
        )}
      </Modal>

      <Modal
        open={showCreateForm}
        onClose={closeCreateForm}
        eyebrow={t('NEW USER')}
        title={t('Create user')}
        size="large"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div
            className="feedback feedback-error modal-feedback"
            role="alert"
          >
            {formError}
          </div>
        )}

        <form
          className="entity-form"
          onSubmit={handleCreateSubmit}
        >
          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('First name')}</span>
              <input
                name="firstName"
                value={createForm.firstName}
                onChange={handleCreateChange}
                required
                maxLength={100}
              />
            </label>

            <label className="form-field">
              <span>{t('Last name')}</span>
              <input
                name="lastName"
                value={createForm.lastName}
                onChange={handleCreateChange}
                required
                maxLength={100}
              />
            </label>

            <label className="form-field">
              <span>{t('Email')}</span>
              <input
                type="email"
                name="email"
                value={createForm.email}
                onChange={handleCreateChange}
                required
                maxLength={255}
              />
            </label>

            <label className="form-field">
              <span>{t('Password')}</span>
              <input
                type="password"
                name="password"
                value={createForm.password}
                onChange={handleCreateChange}
                required
                minLength={8}
                maxLength={72}
              />
            </label>
          </div>

          <label className="form-field">
            <span>{t('Phone')}</span>
            <input
              name="phone"
              value={createForm.phone}
              onChange={handleCreateChange}
              maxLength={50}
            />
          </label>

          <div>
            <p className="form-section-label">
              {t('Roles')}
            </p>

            <div className="role-option-grid">
              {roleOptions.map((option) => (
                <label
                  className="checkbox-field"
                  key={option}
                >
                  <input
                    type="checkbox"
                    checked={createForm.roles.includes(option)}
                    onChange={() =>
                      setCreateForm((current) => ({
                        ...current,
                        roles: current.roles.includes(option)
                          ? current.roles.filter(
                              (role) => role !== option,
                            )
                          : [
                              ...current.roles,
                              option,
                            ],
                      }))
                    }
                  />

                  <span>
                    {t(roleLabels[option])}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className="entity-form-actions">
            <button
              className="button button-primary"
              type="submit"
              disabled={
                saving ||
                createForm.roles.length === 0
              }
            >
              {saving
                ? t('Saving...')
                : t('Create user')}
            </button>

            <button
              className="button button-secondary"
              type="button"
              onClick={closeCreateForm}
              disabled={saving}
            >
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={
          showEditForm &&
          Boolean(editUserId)
        }
        onClose={closeEditForm}
        eyebrow={t('EDIT USER')}
        title={t('Edit user profile')}
        size="medium"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div
            className="feedback feedback-error modal-feedback"
            role="alert"
          >
            {formError}
          </div>
        )}

        <form
          className="entity-form"
          onSubmit={handleEditSubmit}
        >
          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('First name')}</span>
              <input
                name="firstName"
                value={editForm.firstName}
                onChange={handleEditChange}
                required
                maxLength={100}
              />
            </label>

            <label className="form-field">
              <span>{t('Last name')}</span>
              <input
                name="lastName"
                value={editForm.lastName}
                onChange={handleEditChange}
                required
                maxLength={100}
              />
            </label>
          </div>

          <label className="form-field">
            <span>{t('Email')}</span>
            <input
              type="email"
              name="email"
              value={editForm.email}
              onChange={handleEditChange}
              required
              maxLength={255}
            />
          </label>

          <label className="form-field">
            <span>{t('Phone')}</span>
            <input
              name="phone"
              value={editForm.phone}
              onChange={handleEditChange}
              maxLength={50}
            />
          </label>

          <div className="entity-form-actions">
            <button
              className="button button-primary"
              type="submit"
              disabled={saving}
            >
              {saving
                ? t('Saving...')
                : t('Save user')}
            </button>

            <button
              className="button button-secondary"
              type="button"
              onClick={closeEditForm}
              disabled={saving}
            >
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={
          showRolesForm &&
          Boolean(rolesUserId)
        }
        onClose={closeRolesForm}
        eyebrow={t('MANAGE ROLES')}
        title={t('Manage user roles')}
        size="medium"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div
            className="feedback feedback-error modal-feedback"
            role="alert"
          >
            {formError}
          </div>
        )}

        <form
          className="entity-form"
          onSubmit={handleRolesSubmit}
        >
          <div className="role-option-grid">
            {roleOptions.map((option) => (
              <label
                className="checkbox-field"
                key={option}
              >
                <input
                  type="checkbox"
                  checked={rolesForm.includes(option)}
                  onChange={() => toggleRole(option)}
                />

                <span>
                  {t(roleLabels[option])}
                </span>
              </label>
            ))}
          </div>

          {providerSelected && (
            <div className="data-panel compact-panel">
              <div className="data-panel-header">
                <div>
                  <p className="eyebrow">
                    {t('OPTIONAL STAFF ASSIGNMENT')}
                  </p>

                  <h3>
                    {t('Provider staff assignment')}
                  </h3>
                </div>
              </div>

              <p className="panel-copy">
                {t(
                  'Optional. Complete these fields if this provider should be associated with a building and operational staff assignment.',
                )}
              </p>

              {buildingsError && (
                <div
                  className="feedback feedback-error modal-feedback"
                  role="alert"
                >
                  {buildingsError}
                </div>
              )}

              <div className="form-grid-2">
                <label className="form-field">
                  <span>{t('Building')}</span>

                  <select
                    name="buildingId"
                    value={staffForm.buildingId}
                    onChange={handleStaffChange}
                    disabled={saving || buildingsLoading}
                  >
                    <option value="">
                      {buildingsLoading
                        ? t('Loading...')
                        : t('No staff assignment')}
                    </option>

                    {buildings.map((building) => (
                      <option
                        key={building.id}
                        value={building.id}
                      >
                        {building.code}
                        {building.name
                          ? ` — ${building.name}`
                          : ''}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="form-field">
                  <span>{t('Staff Type')}</span>

                  <select
                    name="staffType"
                    value={staffForm.staffType}
                    onChange={handleStaffChange}
                    disabled={
                      saving ||
                      !staffForm.buildingId
                    }
                  >
                    {staffTypeOptions.map((staffType) => (
                      <option
                        key={staffType}
                        value={staffType}
                      >
                        {t(staffType)}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="form-field">
                  <span>{t('Employee Code')}</span>

                  <input
                    name="employeeCode"
                    value={staffForm.employeeCode}
                    onChange={handleStaffChange}
                    maxLength={80}
                    disabled={
                      saving ||
                      !staffForm.buildingId
                    }
                    placeholder="PROV-A-003"
                  />
                </label>

                <label className="checkbox-field">
                  <input
                    type="checkbox"
                    name="active"
                    checked={staffForm.active}
                    onChange={handleStaffChange}
                    disabled={
                      saving ||
                      !staffForm.buildingId
                    }
                  />

                  <span>{t('Active staff assignment')}</span>
                </label>
              </div>
            </div>
          )}

          <div className="entity-form-actions">
            <button
              className="button button-primary"
              type="submit"
              disabled={
                saving ||
                rolesForm.length === 0
              }
            >
              {saving
                ? t('Saving...')
                : t('Save roles')}
            </button>

            <button
              className="button button-secondary"
              type="button"
              onClick={closeRolesForm}
              disabled={saving}
            >
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={Boolean(statusUser)}
        onClose={() =>
          !saving && setStatusUser(null)
        }
        eyebrow={t('ACCOUNT STATUS')}
        title={
          statusUser?.active
            ? t('Deactivate user')
            : t('Activate user')
        }
        size="small"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div
            className="feedback feedback-error modal-feedback"
            role="alert"
          >
            {formError}
          </div>
        )}

        <p className="panel-copy">
          {statusUser?.active
            ? t(
                'This will prevent the user from authenticating until the account is activated again.',
              )
            : t(
                'This will allow the user to authenticate again if their credentials are valid.',
              )}
        </p>

        <div className="entity-form-actions">
          <button
            className={`button ${
              statusUser?.active
                ? 'button-danger'
                : 'button-primary'
            }`}
            type="button"
            onClick={confirmStatusChange}
            disabled={saving}
          >
            {saving
              ? t('Saving...')
              : statusUser?.active
                ? t('Deactivate')
                : t('Activate')}
          </button>

          <button
            className="button button-secondary"
            type="button"
            onClick={() => setStatusUser(null)}
            disabled={saving}
          >
            {t('Cancel')}
          </button>
        </div>
      </Modal>
    </section>
  )
}

export default UserManagementPage