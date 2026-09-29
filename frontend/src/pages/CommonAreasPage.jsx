//----------------------- M24.1 ------------------
import { useCallback, useEffect, useMemo, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import { apiRequest } from '../api/apiClient.js'
import {
  createCommonArea,
  deactivateCommonArea,
  getCommonAreas,
  updateCommonArea,
} from '../api/commonAreasApi.js'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'

const PAGE_SIZE = 100

function createDefaultForm() {
  return {
    buildingId: '',
    name: '',
    areaType: '',
    capacity: '',
    bookingRequired: true,
    bookingDurationMinutes: '',
  }
}

function CommonAreasPage() {
  const { t } = useTranslation()
  const { user } = useAuth()

  const role = String(
    user?.roles?.[0] ?? user?.role ?? ''
  ).trim().toUpperCase()

  const canManage = role === 'ADMINISTRATOR'

  const [commonAreas, setCommonAreas] = useState([])
  const [buildings, setBuildings] = useState([])

  const [loading, setLoading] = useState(true)
  const [buildingsLoading, setBuildingsLoading] = useState(true)

  const [error, setError] = useState('')
  const [buildingsError, setBuildingsError] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [showEdit, setShowEdit] = useState(false)

  const [selectedArea, setSelectedArea] = useState(null)

  const [form, setForm] = useState(createDefaultForm)

  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const loadCommonAreas = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const response = await getCommonAreas('', 0, PAGE_SIZE)

      setCommonAreas(
        Array.isArray(response?.content)
          ? response.content.filter((area) => area.active !== false)
          : []
      )
    } catch (requestError) {
      setError(
        requestError.message || t('Unable to load common areas.')
      )
      setCommonAreas([])
    } finally {
      setLoading(false)
    }
  }, [t])

  const loadBuildings = useCallback(async () => {
    setBuildingsLoading(true)
    setBuildingsError('')

    try {
      const response = await apiRequest(
        '/api/buildings?page=0&size=100',
        {
          method: 'GET',
        }
      )

      setBuildings(
        Array.isArray(response?.content)
          ? response.content.filter((building) => building.active !== false)
          : []
      )
    } catch (requestError) {
      setBuildingsError(
        requestError.message || t('Unable to load buildings.')
      )
      setBuildings([])
    } finally {
      setBuildingsLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadCommonAreas()
    loadBuildings()
  }, [loadCommonAreas, loadBuildings])

  const sortedBuildings = useMemo(
    () =>
      [...buildings].sort((a, b) =>
        String(a.code ?? '').localeCompare(
          String(b.code ?? ''),
          undefined,
          { numeric: true, sensitivity: 'base' }
        )
      ),
    [buildings]
  )

  const openCreate = () => {
    setForm(createDefaultForm())
    setFormError('')
    setSuccessMessage('')
    setShowCreate(true)
  }

  const closeCreate = () => {
    if (saving) return

    setShowCreate(false)
    setFormError('')
  }

  const openEdit = (area) => {
    setSelectedArea(area)

    setForm({
      buildingId: area.buildingId ?? '',
      name: area.name ?? '',
      areaType: area.areaType ?? '',
      capacity: area.capacity ?? '',
      bookingRequired: area.bookingRequired !== false,
      bookingDurationMinutes: area.bookingDurationMinutes ?? '',
    })

    setFormError('')
    setSuccessMessage('')
    setShowEdit(true)
  }

  const closeEdit = () => {
    if (saving) return

    setShowEdit(false)
    setSelectedArea(null)
    setFormError('')
  }

  const updateForm = (field, value) => {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const buildPayload = () => ({
    ...(showCreate ? { buildingId: form.buildingId } : {}),
    name: form.name.trim(),
    areaType: form.areaType.trim(),
    capacity:
      form.capacity === ''
        ? null
        : Number(form.capacity),
    bookingRequired: Boolean(form.bookingRequired),
    bookingDurationMinutes:
      form.bookingDurationMinutes === ''
        ? null
        : Number(form.bookingDurationMinutes),
  })

  const validateForm = () => {
    if (showCreate && !form.buildingId) {
      return t('Please select a building.')
    }

    if (!form.name.trim()) {
      return t('Name is required.')
    }

    if (!form.areaType.trim()) {
      return t('Area type is required.')
    }

    if (
      form.capacity !== '' &&
      (!Number.isInteger(Number(form.capacity)) ||
        Number(form.capacity) < 1)
    ) {
      return t('Capacity must be at least 1.')
    }

    if (
      form.bookingDurationMinutes !== '' &&
      (!Number.isInteger(Number(form.bookingDurationMinutes)) ||
        Number(form.bookingDurationMinutes) < 1)
    ) {
      return t('Booking duration must be at least 1 minute.')
    }

    return ''
  }

  const handleCreate = async (event) => {
    event.preventDefault()

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await createCommonArea(buildPayload())

      setShowCreate(false)
      setForm(createDefaultForm())

      await loadCommonAreas()

      setSuccessMessage(
        t('Common area created successfully.')
      )
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to create common area.')
      )
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = async (event) => {
    event.preventDefault()

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedArea) return

    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    try {
      await updateCommonArea(
        selectedArea.id,
        buildPayload()
      )

      setShowEdit(false)
      setSelectedArea(null)

      await loadCommonAreas()

      setSuccessMessage(
        t('Common area updated successfully.')
      )
    } catch (requestError) {
      setFormError(
        requestError.message || t('Unable to update common area.')
      )
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivate = async (area) => {
    const confirmed = window.confirm(
      t(
        `Deactivate "${area.name}"? Existing booking history will be preserved, but the area will no longer be available for new bookings.`
      )
    )

    if (!confirmed) return

    setError('')
    setSuccessMessage('')

    try {
      await deactivateCommonArea(area.id)

      await loadCommonAreas()

      setSuccessMessage(
        t('Common area deactivated successfully.')
      )
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to deactivate common area.')
      )
    }
  }

  if (!canManage) {
    return (
      <div className="empty-state">
        {t('You do not have access to Common Areas.')}
      </div>
    )
  }

  return (
    <div className="module-page">
      <section className="module-page-header">
        <div>
          <p className="eyebrow">{t('BUILDING CONFIGURATION')}</p>

          <h1>{t('Common Areas')}</h1>

          <p className="muted">
            {t(
              'Manage the common areas available for reservations in each building.'
            )}
          </p>
        </div>

        <button
          className="button button-primary"
          type="button"
          onClick={openCreate}
          disabled={buildingsLoading || buildings.length === 0}
        >
          + {t('Add Common Area')}
        </button>
      </section>

      {successMessage && (
        <div
          className="feedback feedback-success"
          role="status"
        >
          {successMessage}
        </div>
      )}

      {error && (
        <div
          className="feedback feedback-error"
          role="alert"
        >
          {error}
        </div>
      )}

      {buildingsError && (
        <div
          className="feedback feedback-error"
          role="alert"
        >
          {buildingsError}
        </div>
      )}

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">
              {t('COMMON AREAS')}
            </p>

            <h2>{t('Active Common Areas')}</h2>
          </div>

          <span className="status-pill">
            {commonAreas.length} {t('active areas')}
          </span>
        </div>

        {loading ? (
          <div className="feedback feedback-info">
            {t('Loading common areas...')}
          </div>
        ) : commonAreas.length === 0 ? (
          <div className="empty-state">
            {t('No common areas found.')}
          </div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Building')}</th>
                  <th>{t('Name')}</th>
                  <th>{t('Type')}</th>
                  <th>{t('Capacity')}</th>
                  <th>{t('Booking Required')}</th>
                  <th>{t('Duration')}</th>
                  <th>{t('Actions')}</th>
                </tr>
              </thead>

              <tbody>
                {commonAreas.map((area) => (
                  <tr key={area.id}>
                    <td>
                      <strong>
                        {area.buildingCode || '—'}
                      </strong>
                    </td>

                    <td>{area.name}</td>

                    <td>
                      <span className="status-pill">
                        {area.areaType}
                      </span>
                    </td>

                    <td>
                      {area.capacity ?? '—'}
                    </td>

                    <td>
                      {area.bookingRequired
                        ? t('Yes')
                        : t('No')}
                    </td>

                    <td>
                      {area.bookingDurationMinutes
                        ? `${area.bookingDurationMinutes} min`
                        : '—'}
                    </td>

                    <td>
                      <TableActions moreLabel={t('More')}>
                        <TableAction
                          icon="edit"
                          label={t('Edit')}
                          variant="edit"
                          onClick={() => openEdit(area)}
                        />

                        <TableAction
                          icon="trash"
                          label={t('Deactivate')}
                          variant="delete"
                          onClick={() => handleDeactivate(area)}
                        />
                      </TableActions>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modal
        open={showCreate}
        title={t('Add Common Area')}
        onClose={closeCreate}
        size="medium"
        closeOnBackdrop={!saving}
      >
        <form
          className="form-grid"
          onSubmit={handleCreate}
        >
          <label className="form-field form-grid-full">
            <span>{t('Building')}</span>

            <select
              value={form.buildingId}
              onChange={(event) =>
                updateForm(
                  'buildingId',
                  event.target.value
                )
              }
              disabled={saving || buildingsLoading}
              required
            >
              <option value="">
                {buildingsLoading
                  ? t('Loading buildings...')
                  : t('Select building')}
              </option>

              {sortedBuildings.map((building) => (
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

          <CommonAreaFields
            form={form}
            updateForm={updateForm}
            disabled={saving}
            t={t}
          />

          {formError && (
            <div
              className="feedback feedback-error form-grid-full"
              role="alert"
            >
              {formError}
            </div>
          )}

          <div className="modal-actions form-grid-full">
            <button
              className="button button-secondary"
              type="button"
              disabled={saving}
              onClick={closeCreate}
            >
              {t('Cancel')}
            </button>

            <button
              className="button button-primary"
              type="submit"
              disabled={saving || buildingsLoading}
            >
              {saving
                ? t('Saving...')
                : t('Create Common Area')}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        open={showEdit}
        title={t('Edit Common Area')}
        onClose={closeEdit}
        size="medium"
        closeOnBackdrop={!saving}
      >
        <form
          className="form-grid"
          onSubmit={handleEdit}
        >
          {selectedArea && (
            <div className="booking-edit-context form-grid-full">
              <span>{t('Building')}</span>
              <strong>
                {selectedArea.buildingCode || '—'}
              </strong>
            </div>
          )}

          <CommonAreaFields
            form={form}
            updateForm={updateForm}
            disabled={saving}
            t={t}
          />

          {formError && (
            <div
              className="feedback feedback-error form-grid-full"
              role="alert"
            >
              {formError}
            </div>
          )}

          <div className="modal-actions form-grid-full">
            <button
              className="button button-secondary"
              type="button"
              disabled={saving}
              onClick={closeEdit}
            >
              {t('Cancel')}
            </button>

            <button
              className="button button-primary"
              type="submit"
              disabled={saving}
            >
              {saving
                ? t('Saving...')
                : t('Save changes')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

function CommonAreaFields({
  form,
  updateForm,
  disabled,
  t,
}) {
  return (
    <>
      <label className="form-field">
        <span>{t('Name')}</span>

        <input
          type="text"
          value={form.name}
          onChange={(event) =>
            updateForm('name', event.target.value)
          }
          maxLength={150}
          disabled={disabled}
          required
        />
      </label>

      <label className="form-field">
        <span>{t('Area Type')}</span>

        <input
          type="text"
          value={form.areaType}
          onChange={(event) =>
            updateForm(
              'areaType',
              event.target.value
            )
          }
          maxLength={50}
          placeholder={t(
            'e.g. LOUNGE, GYM, POOL'
          )}
          disabled={disabled}
          required
        />
      </label>

      <label className="form-field">
        <span>{t('Capacity')}</span>

        <input
          type="number"
          min="1"
          step="1"
          value={form.capacity}
          onChange={(event) =>
            updateForm(
              'capacity',
              event.target.value
            )
          }
          disabled={disabled}
        />
      </label>

      <label className="form-field">
        <span>{t('Booking Duration (minutes)')}</span>

        <input
          type="number"
          min="1"
          step="1"
          value={form.bookingDurationMinutes}
          onChange={(event) =>
            updateForm(
              'bookingDurationMinutes',
              event.target.value
            )
          }
          disabled={disabled}
        />
      </label>

      <label className="form-field form-grid-full">
        <span>{t('Booking Required')}</span>

        <select
          value={form.bookingRequired ? 'true' : 'false'}
          onChange={(event) =>
            updateForm(
              'bookingRequired',
              event.target.value === 'true'
            )
          }
          disabled={disabled}
        >
          <option value="true">
            {t('Yes')}
          </option>

          <option value="false">
            {t('No')}
          </option>
        </select>
      </label>
    </>
  )
}

export default CommonAreasPage

