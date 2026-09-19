import { useCallback, useEffect, useMemo, useState } from 'react'

import { getBuildings } from '../api/buildingsApi.js'
import {
  createUnit,
  getUnit,
  getUnits,
  updateUnit,
} from '../api/unitsApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'
import Modal from '../components/Modal.jsx'

const PAGE_SIZE = 20

const emptyForm = {
  buildingId: '',
  unitNumber: '',
  floorNumber: '',
  unitType: '',
  active: true,
}

function UnitsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const isAdministrator = user?.roles?.includes('ADMINISTRATOR')
  const canCreateOrEdit = isAdministrator

  const [units, setUnits] = useState([])
  const [buildings, setBuildings] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [selectedUnit, setSelectedUnit] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const buildingNameById = useMemo(
    () => Object.fromEntries(buildings.map((building) => [building.id, building.name])),
    [buildings],
  )

  const loadUnits = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')

    try {
      const response = await getUnits(pageToLoad, PAGE_SIZE)
      setUnits(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load units.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  const loadBuildingsForAdmin = useCallback(async () => {
    if (!isAdministrator) {
      return
    }

    try {
      const response = await getBuildings(0, 100)
      setBuildings(Array.isArray(response?.content) ? response.content : [])
    } catch (requestError) {
      setError(requestError.message || t('Unable to load buildings for the unit form.'))
    }
  }, [isAdministrator, t])

  useEffect(() => {
    loadUnits(0)
  }, [loadUnits])

  useEffect(() => {
    loadBuildingsForAdmin()
  }, [loadBuildingsForAdmin])

  const resetForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setFormError('')
  }

  const startCreate = () => {
    resetForm()
    setShowForm(true)
  }

  const startEdit = (unit) => {
    setForm({
      buildingId: unit.buildingId || '',
      unitNumber: unit.unitNumber || '',
      floorNumber: unit.floorNumber ?? '',
      unitType: unit.unitType || '',
      active: Boolean(unit.active),
    })
    setEditingId(unit.id)
    setFormError('')
    setSuccessMessage('')
    setShowForm(true)
  }

  const closeForm = () => {
    setShowForm(false)
    resetForm()
  }

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    setSuccessMessage('')

    const payload = {
      buildingId: form.buildingId,
      unitNumber: form.unitNumber.trim(),
      floorNumber: form.floorNumber === '' ? null : Number(form.floorNumber),
      unitType: form.unitType.trim() || null,
    }

    try {
      if (editingId) {
        await updateUnit(editingId, {
          ...payload,
          active: form.active,
        })
        setSuccessMessage(t('Unit updated successfully.'))
      } else {
        await createUnit(payload)
        setSuccessMessage(t('Unit created successfully.'))
      }

      setShowForm(false)
      resetForm()
      await loadUnits(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to save unit.'))
    } finally {
      setSaving(false)
    }
  }

  const viewUnit = async (unitId) => {
    setSelectedUnit(null)
    setDetailError('')
    setDetailLoading(true)
    setError('')

    try {
      const detail = await getUnit(unitId)

      if (!detail || typeof detail !== 'object') {
        throw new Error(t('The unit details could not be loaded.'))
      }

      setSelectedUnit(detail)
    } catch (requestError) {
      setDetailError(requestError.message || t('Unable to load unit details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  const closeDetails = () => {
    if (!detailLoading) {
      setSelectedUnit(null)
      setDetailError('')
    }
  }

  const showBuildingColumn = isAdministrator

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('UNITS')}</p>
          <h2>{t('Units')}</h2>
          <p className="muted">
            {t('View and maintain the units connected to each building.')}
          </p>
        </div>

        {canCreateOrEdit && (
          <button className="button button-primary" type="button" onClick={startCreate}>
            + {t('Add unit')}
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

      <Modal
        open={showForm && canCreateOrEdit}
        onClose={closeForm}
        eyebrow={editingId ? t('EDIT UNIT') : t('NEW UNIT')}
        title={editingId ? t('Edit unit') : t('Create unit')}
        size="large"
        closeOnBackdrop={!saving}
      >
        {formError && (
          <div className="feedback feedback-error modal-feedback" role="alert">
            {formError}
          </div>
        )}

        <form className="entity-form" onSubmit={handleSubmit}>
          <div className="form-grid-4">
            <label className="form-field field-span-2">
              <span>{t('Building')}</span>
              <select name="buildingId" value={form.buildingId} onChange={handleChange} required>
                <option value="">{t('Select building')}</option>
                {buildings.map((building) => (
                  <option
                    key={building.id}
                    value={building.id}
                    disabled={!building.active && building.id !== form.buildingId}
                  >
                    {building.name} ({building.code}){!building.active ? ` — ${t('Inactive')}` : ''}
                  </option>
                ))}
              </select>
            </label>

            <label className="form-field">
              <span>{t('Unit number')}</span>
              <input name="unitNumber" value={form.unitNumber} onChange={handleChange} required maxLength={50} />
            </label>

            <label className="form-field">
              <span>{t('Floor')}</span>
              <input name="floorNumber" value={form.floorNumber} onChange={handleChange} type="number" min="0" />
            </label>
          </div>

          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('Unit type')}</span>
              <input name="unitType" value={form.unitType} onChange={handleChange} maxLength={30} />
            </label>
          </div>

          {editingId && (
            <label className="checkbox-field">
              <input
                type="checkbox"
                name="active"
                checked={form.active}
                onChange={handleChange}
              />
              <span>{t('Active unit')}</span>
            </label>
          )}

          <div className="entity-form-actions">
            <button className="button button-primary" type="submit" disabled={saving}>
              {saving ? t('Saving...') : t('Save unit')}
            </button>

            <button className="button button-secondary" type="button" onClick={closeForm} disabled={saving}>
              {t('Cancel')}
            </button>
          </div>
        </form>
      </Modal>

      <section className="data-panel">
        <div className="data-panel-header">
          <div>
            <p className="eyebrow">{t('UNIT DIRECTORY')}</p>
            <h3>{t('Connected units')}</h3>
          </div>

          <span className="status-pill">
            {totalElements} {t('units')}
          </span>
        </div>

        {loading ? (
          <div className="module-empty-state compact-empty-state">
            {t('Loading...')}
          </div>
        ) : units.length === 0 ? (
          <div className="module-empty-state compact-empty-state">
            <h3>{t('No units found')}</h3>
            <p className="muted">{t('There are no units available for your role.')}</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  {showBuildingColumn && <th>{t('Building')}</th>}
                  <th>{t('Unit')}</th>
                  <th>{t('Floor')}</th>
                  <th>{t('Type')}</th>
                  <th>{t('Status')}</th>
                  <th className="table-actions-column">{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {units.map((unit) => (
                  <tr key={unit.id}>
                    {showBuildingColumn && (
                      <td>
                        {buildingNameById[unit.buildingId] || t('Unknown building')}
                      </td>
                    )}
                    <td>
                      <strong>{unit.unitNumber}</strong>
                    </td>
                    <td>{unit.floorNumber ?? '—'}</td>
                    <td>{unit.unitType || '—'}</td>
                    <td>
                      <span className={`status-badge ${unit.active ? 'status-success' : 'status-muted'}`}>
                        {unit.active ? t('Active') : t('Inactive')}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="button button-ghost" type="button" onClick={() => viewUnit(unit.id)}>
                          {t('View')}
                        </button>
                        {canCreateOrEdit && (
                          <>
                            <button className="button button-ghost" type="button" onClick={() => startEdit(unit)}>
                              {t('Edit')}
                            </button>
                            <button
                              className="button button-danger button-placeholder-disabled"
                              type="button"
                              disabled
                              title={t('Deletion is not available yet. Use deactivation instead.')}
                              aria-label={t('Delete')}
                            >
                              {t('Delete')}
                            </button>
                          </>
                        )}
                      </div>
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
              onClick={() => loadUnits(page - 1)}
            >
              {t('Previous')}
            </button>
            <button
              className="button button-secondary"
              type="button"
              disabled={page + 1 >= totalPages || loading}
              onClick={() => loadUnits(page + 1)}
            >
              {t('Next')}
            </button>
          </div>
        </div>
      </section>

      <Modal
        open={Boolean(selectedUnit) || detailLoading || Boolean(detailError)}
        onClose={closeDetails}
        eyebrow={t('UNIT DETAILS')}
        title={selectedUnit?.unitNumber || t('Loading...')}
        size="medium"
        closeOnBackdrop={!detailLoading}
      >
        {detailLoading ? (
          <div className="modal-loading-state">
            {t('Loading unit details...')}
          </div>
        ) : selectedUnit ? (
          <div className="detail-grid">
            {showBuildingColumn && (
              <div>
                <span>{t('Building')}</span>
                <strong>{buildingNameById[selectedUnit.buildingId] || t('Unknown building')}</strong>
              </div>
            )}
            <div><span>{t('Unit')}</span><strong>{selectedUnit.unitNumber}</strong></div>
            <div><span>{t('Floor')}</span><strong>{selectedUnit.floorNumber ?? '—'}</strong></div>
            <div><span>{t('Type')}</span><strong>{selectedUnit.unitType || '—'}</strong></div>
            <div><span>{t('Status')}</span><strong>{selectedUnit.active ? t('Active') : t('Inactive')}</strong></div>
          </div>
        ) : (
          <div className="feedback feedback-error" role="alert">
            {detailError || t('Unable to load unit details.')}
          </div>
        )}
      </Modal>
    </section>
  )
}

export default UnitsPage
