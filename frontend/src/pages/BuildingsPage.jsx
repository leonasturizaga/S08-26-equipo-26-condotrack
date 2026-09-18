import { useCallback, useEffect, useState } from 'react'

import {
  createBuilding,
  getBuilding,
  getBuildings,
  updateBuilding,
} from '../api/buildingsApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 10

const emptyForm = {
  name: '',
  code: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  stateProvince: '',
  postalCode: '',
  country: 'Argentina',
  timezone: 'America/Argentina/Buenos_Aires',
  active: true,
}

function BuildingsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const isAdministrator = user?.roles?.includes('ADMINISTRATOR')

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
  const [selectedBuilding, setSelectedBuilding] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)

  const loadBuildings = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')

    try {
      const response = await getBuildings(pageToLoad, PAGE_SIZE)
      setBuildings(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load buildings.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadBuildings(0)
  }, [loadBuildings])

  const resetForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setFormError('')
  }

  const startCreate = () => {
    resetForm()
    setShowForm(true)
  }

  const startEdit = (building) => {
    setForm({
      name: building.name || '',
      code: building.code || '',
      addressLine1: building.addressLine1 || '',
      addressLine2: building.addressLine2 || '',
      city: building.city || '',
      stateProvince: building.stateProvince || '',
      postalCode: building.postalCode || '',
      country: building.country || '',
      timezone: building.timezone || '',
      active: Boolean(building.active),
    })
    setEditingId(building.id)
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
      name: form.name.trim(),
      code: form.code.trim(),
      addressLine1: form.addressLine1.trim(),
      addressLine2: form.addressLine2.trim() || null,
      city: form.city.trim(),
      stateProvince: form.stateProvince.trim() || null,
      postalCode: form.postalCode.trim() || null,
      country: form.country.trim(),
      timezone: form.timezone.trim() || null,
    }

    try {
      if (editingId) {
        await updateBuilding(editingId, {
          ...payload,
          active: form.active,
        })
        setSuccessMessage(t('Building updated successfully.'))
      } else {
        await createBuilding(payload)
        setSuccessMessage(t('Building created successfully.'))
      }

      setShowForm(false)
      resetForm()
      await loadBuildings(page)
    } catch (requestError) {
      setFormError(requestError.message || t('Unable to save building.'))
    } finally {
      setSaving(false)
    }
  }

  const viewBuilding = async (buildingId) => {
    setDetailLoading(true)
    setError('')

    try {
      const detail = await getBuilding(buildingId)
      setSelectedBuilding(detail)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load building details.'))
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('BUILDINGS')}</p>
          <h2>{t('Buildings')}</h2>
          <p className="muted">
            {t('Manage the condominium buildings connected to CondoTrack.')}
          </p>
        </div>

        {isAdministrator && (
          <button className="button button-primary" type="button" onClick={startCreate}>
            + {t('Add building')}
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

      {showForm && isAdministrator && (
        <section className="data-panel">
          <div className="data-panel-header">
            <div>
              <p className="eyebrow">
                {editingId ? t('EDIT BUILDING') : t('NEW BUILDING')}
              </p>
              <h3>{editingId ? t('Edit building') : t('Create building')}</h3>
            </div>

            <button className="button button-secondary" type="button" onClick={closeForm}>
              {t('Cancel')}
            </button>
          </div>

          {formError && (
            <div className="feedback feedback-error" role="alert">
              {formError}
            </div>
          )}

          <form className="entity-form" onSubmit={handleSubmit}>
            <div className="form-grid-3">
              <label className="form-field">
                <span>{t('Name')}</span>
                <input name="name" value={form.name} onChange={handleChange} required maxLength={200} />
              </label>

              <label className="form-field">
                <span>{t('Code')}</span>
                <input name="code" value={form.code} onChange={handleChange} required maxLength={50} />
              </label>

              <label className="form-field">
                <span>{t('Country')}</span>
                <input name="country" value={form.country} onChange={handleChange} required maxLength={120} />
              </label>
            </div>

            <div className="form-grid-2">
              <label className="form-field">
                <span>{t('Address')}</span>
                <input name="addressLine1" value={form.addressLine1} onChange={handleChange} required maxLength={255} />
              </label>

              <label className="form-field">
                <span>{t('Address line 2')}</span>
                <input name="addressLine2" value={form.addressLine2} onChange={handleChange} maxLength={255} />
              </label>
            </div>

            <div className="form-grid-4">
              <label className="form-field">
                <span>{t('City')}</span>
                <input name="city" value={form.city} onChange={handleChange} required maxLength={120} />
              </label>

              <label className="form-field">
                <span>{t('State / Province')}</span>
                <input name="stateProvince" value={form.stateProvince} onChange={handleChange} maxLength={120} />
              </label>

              <label className="form-field">
                <span>{t('Postal code')}</span>
                <input name="postalCode" value={form.postalCode} onChange={handleChange} maxLength={30} />
              </label>

              <label className="form-field">
                <span>{t('Timezone')}</span>
                <input name="timezone" value={form.timezone} onChange={handleChange} maxLength={80} />
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
                <span>{t('Active building')}</span>
              </label>
            )}

            <div className="entity-form-actions">
              <button className="button button-primary" type="submit" disabled={saving}>
                {saving ? t('Saving...') : t('Save building')}
              </button>

              <button className="button button-secondary" type="button" onClick={closeForm} disabled={saving}>
                {t('Cancel')}
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="data-panel">
        <div className="data-panel-header">
          <div>
            <p className="eyebrow">{t('BUILDING DIRECTORY')}</p>
            <h3>{t('Connected buildings')}</h3>
          </div>

          <span className="status-pill">
            {totalElements} {t('buildings')}
          </span>
        </div>

        {loading ? (
          <div className="module-empty-state compact-empty-state">
            {t('Loading...')}
          </div>
        ) : buildings.length === 0 ? (
          <div className="module-empty-state compact-empty-state">
            <h3>{t('No buildings found')}</h3>
            <p className="muted">{t('There are no active buildings available for your role.')}</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Building')}</th>
                  <th>{t('Code')}</th>
                  <th>{t('City')}</th>
                  <th>{t('Country')}</th>
                  <th>{t('Status')}</th>
                  <th className="table-actions-column">{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {buildings.map((building) => (
                  <tr key={building.id}>
                    <td>
                      <strong>{building.name}</strong>
                      <span className="table-meta">{building.addressLine1}</span>
                    </td>
                    <td>{building.code}</td>
                    <td>{building.city}</td>
                    <td>{building.country}</td>
                    <td>
                      <span className={`status-badge ${building.active ? 'status-success' : 'status-muted'}`}>
                        {building.active ? t('Active') : t('Inactive')}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="button button-ghost" type="button" onClick={() => viewBuilding(building.id)}>
                          {t('View')}
                        </button>
                        {isAdministrator && (
                          <button className="button button-ghost" type="button" onClick={() => startEdit(building)}>
                            {t('Edit')}
                          </button>
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
              onClick={() => loadBuildings(page - 1)}
            >
              {t('Previous')}
            </button>
            <button
              className="button button-secondary"
              type="button"
              disabled={page + 1 >= totalPages || loading}
              onClick={() => loadBuildings(page + 1)}
            >
              {t('Next')}
            </button>
          </div>
        </div>
      </section>

      {detailLoading && (
        <div className="feedback feedback-info" role="status">
          {t('Loading building details...')}
        </div>
      )}

      {selectedBuilding && !detailLoading && (
        <section className="data-panel detail-panel">
          <div className="data-panel-header">
            <div>
              <p className="eyebrow">{t('BUILDING DETAILS')}</p>
              <h3>{selectedBuilding.name}</h3>
            </div>

            <button className="button button-secondary" type="button" onClick={() => setSelectedBuilding(null)}>
              {t('Close')}
            </button>
          </div>

          <div className="detail-grid">
            <div><span>{t('Code')}</span><strong>{selectedBuilding.code}</strong></div>
            <div><span>{t('Address')}</span><strong>{selectedBuilding.addressLine1}</strong></div>
            <div><span>{t('City')}</span><strong>{selectedBuilding.city}</strong></div>
            <div><span>{t('State / Province')}</span><strong>{selectedBuilding.stateProvince || '—'}</strong></div>
            <div><span>{t('Postal code')}</span><strong>{selectedBuilding.postalCode || '—'}</strong></div>
            <div><span>{t('Country')}</span><strong>{selectedBuilding.country}</strong></div>
            <div><span>{t('Timezone')}</span><strong>{selectedBuilding.timezone || '—'}</strong></div>
            <div><span>{t('Status')}</span><strong>{selectedBuilding.active ? t('Active') : t('Inactive')}</strong></div>
          </div>
        </section>
      )}
    </section>
  )
}

export default BuildingsPage
