//-------------------- M24.3 ---------------------------------
import { useCallback, useEffect, useMemo, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import { apiRequest } from '../api/apiClient.js'
import {
  createCommonArea,
  createCommonAreaAvailabilityBlock,
  deactivateCommonArea,
  deactivateCommonAreaAvailabilityBlock,
  getAmenities,
  getCommonAreaAvailabilityBlocks,
  getCommonAreas,
  updateCommonArea,
  updateCommonAreaAvailabilityBlock,
} from '../api/commonAreasApi.js'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import CommonAreaMediaPanel from '../components/CommonAreaMediaPanel.jsx'

const PAGE_SIZE = 100

function createDefaultForm() {
  return {
    buildingId: '',
    name: '',
    areaType: '',
    description: '',
    capacity: '',
    bookingRequired: true,
    bookingDurationMinutes: '',
    amenityIds: [],
  }
}

function createDefaultBlockForm() {
  return {
    blockType: 'UNAVAILABLE',
    startAt: '',
    endAt: '',
    notes: '',
  }
}

function toDateTimeLocal(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const offset = date.getTimezoneOffset()
  const localDate = new Date(date.getTime() - offset * 60 * 1000)

  return localDate.toISOString().slice(0, 16)
}

function toOffsetDateTime(value) {
  if (!value) {
    return null
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return null
  }

  return date.toISOString()
}

function formatDateTime(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function getBlockTypeLabel(blockType, t) {
  if (blockType === 'REPAIR') {
    return t('Repair')
  }

  return t('Unavailable')
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
  const [amenities, setAmenities] = useState([])

  const [loading, setLoading] = useState(true)
  const [buildingsLoading, setBuildingsLoading] = useState(true)
  const [amenitiesLoading, setAmenitiesLoading] = useState(true)

  const [error, setError] = useState('')
  const [buildingsError, setBuildingsError] = useState('')
  const [amenitiesError, setAmenitiesError] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [showEdit, setShowEdit] = useState(false)
  const [showAvailability, setShowAvailability] = useState(false)
  const [showMedia, setShowMedia] = useState(false)
  const [selectedArea, setSelectedArea] = useState(null)

  const [form, setForm] = useState(createDefaultForm)

  const [availabilityBlocks, setAvailabilityBlocks] = useState([])
  const [availabilityLoading, setAvailabilityLoading] = useState(false)
  const [availabilityError, setAvailabilityError] = useState('')

  const [blockForm, setBlockForm] = useState(
    createDefaultBlockForm,
  )
  const [editingBlock, setEditingBlock] = useState(null)
  const [blockFormError, setBlockFormError] = useState('')
  const [blockSaving, setBlockSaving] = useState(false)

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
          ? response.content.filter(
              (area) => area.active !== false,
            )
          : [],
      )
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to load common areas.'),
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
        },
      )

      setBuildings(
        Array.isArray(response?.content)
          ? response.content.filter(
              (building) => building.active !== false,
            )
          : [],
      )
    } catch (requestError) {
      setBuildingsError(
        requestError.message ||
          t('Unable to load buildings.'),
      )
      setBuildings([])
    } finally {
      setBuildingsLoading(false)
    }
  }, [t])

  const loadAmenities = useCallback(async () => {
    setAmenitiesLoading(true)
    setAmenitiesError('')

    try {
      const response = await getAmenities()

      setAmenities(
        Array.isArray(response) ? response : [],
      )
    } catch (requestError) {
      setAmenitiesError(
        requestError.message ||
          t('Unable to load amenities.'),
      )
      setAmenities([])
    } finally {
      setAmenitiesLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadCommonAreas()
    loadBuildings()
    loadAmenities()
  }, [
    loadCommonAreas,
    loadBuildings,
    loadAmenities,
  ])

  const sortedBuildings = useMemo(
    () =>
      [...buildings].sort((a, b) =>
        String(a.code ?? '').localeCompare(
          String(b.code ?? ''),
          undefined,
          {
            numeric: true,
            sensitivity: 'base',
          },
        ),
      ),
    [buildings],
  )

  const sortedAmenities = useMemo(
    () =>
      [...amenities].sort((a, b) =>
        String(a.name ?? '').localeCompare(
          String(b.name ?? ''),
          undefined,
          {
            sensitivity: 'base',
          },
        ),
      ),
    [amenities],
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
      description: area.description ?? '',
      capacity: area.capacity ?? '',
      bookingRequired: area.bookingRequired !== false,
      bookingDurationMinutes:
        area.bookingDurationMinutes ?? '',
      amenityIds: Array.isArray(area.amenities)
        ? area.amenities.map((amenity) => amenity.id)
        : [],
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

  const toggleAmenity = (amenityId) => {
    setForm((current) => {
      const currentIds = Array.isArray(
        current.amenityIds,
      )
        ? current.amenityIds
        : []

      const alreadySelected =
        currentIds.includes(amenityId)

      return {
        ...current,
        amenityIds: alreadySelected
          ? currentIds.filter(
              (id) => id !== amenityId,
            )
          : [...currentIds, amenityId],
      }
    })
  }

  const buildPayload = () => ({
    ...(showCreate
      ? { buildingId: form.buildingId }
      : {}),
    name: form.name.trim(),
    areaType: form.areaType.trim(),
    description:
      form.description.trim() === ''
        ? null
        : form.description.trim(),
    capacity:
      form.capacity === ''
        ? null
        : Number(form.capacity),
    bookingRequired: Boolean(
      form.bookingRequired,
    ),
    bookingDurationMinutes:
      form.bookingDurationMinutes === ''
        ? null
        : Number(form.bookingDurationMinutes),
    amenityIds: Array.isArray(form.amenityIds)
      ? form.amenityIds
      : [],
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

    if (form.description.length > 5000) {
      return t(
        'Description must not exceed 5000 characters.',
      )
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
      (!Number.isInteger(
        Number(form.bookingDurationMinutes),
      ) ||
        Number(form.bookingDurationMinutes) < 1)
    ) {
      return t(
        'Booking duration must be at least 1 minute.',
      )
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
        t('Common area created successfully.'),
      )
    } catch (requestError) {
      setFormError(
        requestError.message ||
          t('Unable to create common area.'),
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
        buildPayload(),
      )

      setShowEdit(false)
      setSelectedArea(null)

      await loadCommonAreas()

      setSuccessMessage(
        t('Common area updated successfully.'),
      )
    } catch (requestError) {
      setFormError(
        requestError.message ||
          t('Unable to update common area.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivate = async (area) => {
    const confirmed = window.confirm(
      t(
        `Deactivate "${area.name}"? Existing booking history will be preserved, but the area will no longer be available for new bookings.`,
      ),
    )

    if (!confirmed) return

    setError('')
    setSuccessMessage('')

    try {
      await deactivateCommonArea(area.id)

      await loadCommonAreas()

      setSuccessMessage(
        t('Common area deactivated successfully.'),
      )
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to deactivate common area.'),
      )
    }
  }

  /* =========================================================
     Availability Blocks
     ========================================================= */

  const loadAvailabilityBlocks = useCallback(
    async (commonAreaId) => {
      if (!commonAreaId) return

      setAvailabilityLoading(true)
      setAvailabilityError('')

      try {
        const response =
          await getCommonAreaAvailabilityBlocks(
            commonAreaId,
          )

        const blocks = Array.isArray(response)
          ? response
          : Array.isArray(response?.content)
            ? response.content
            : []

        setAvailabilityBlocks(
          blocks.filter(
            (block) => block.active !== false,
          ),
        )
      } catch (requestError) {
        setAvailabilityError(
          requestError.message ||
            t(
              'Unable to load availability blocks.',
            ),
        )
        setAvailabilityBlocks([])
      } finally {
        setAvailabilityLoading(false)
      }
    },
    [t],
  )

  const openAvailability = async (area) => {
    setSelectedArea(area)
    setAvailabilityBlocks([])
    setAvailabilityError('')
    setBlockFormError('')
    setEditingBlock(null)
    setBlockForm(createDefaultBlockForm())
    setSuccessMessage('')
    setShowAvailability(true)

    await loadAvailabilityBlocks(area.id)
  }

  const closeAvailability = () => {
    if (blockSaving) return

    setShowAvailability(false)
    setSelectedArea(null)
    setAvailabilityBlocks([])
    setAvailabilityError('')
    setBlockFormError('')
    setEditingBlock(null)
    setBlockForm(createDefaultBlockForm())
  }

  const openMedia = (area) => {
    setSelectedArea(area)
    setError('')
    setSuccessMessage('')
    setShowMedia(true)
  }

  const closeMedia = () => {
    setShowMedia(false)
    setSelectedArea(null)
  }
  const openCreateBlock = () => {
    setEditingBlock(null)
    setBlockForm(createDefaultBlockForm())
    setBlockFormError('')
  }

  const openEditBlock = (block) => {
    setEditingBlock(block)

    setBlockForm({
      blockType:
        block.blockType === 'REPAIR'
          ? 'REPAIR'
          : 'UNAVAILABLE',
      startAt: toDateTimeLocal(block.startAt),
      endAt: toDateTimeLocal(block.endAt),
      notes: block.notes ?? '',
    })

    setBlockFormError('')
  }

  const updateBlockForm = (field, value) => {
    setBlockForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const validateBlockForm = () => {
    if (
      blockForm.blockType !== 'UNAVAILABLE' &&
      blockForm.blockType !== 'REPAIR'
    ) {
      return t('Please select a block type.')
    }

    if (!blockForm.startAt) {
      return t('Start date and time is required.')
    }

    if (!blockForm.endAt) {
      return t('End date and time is required.')
    }

    const start = new Date(blockForm.startAt)
    const end = new Date(blockForm.endAt)

    if (
      Number.isNaN(start.getTime()) ||
      Number.isNaN(end.getTime())
    ) {
      return t(
        'Please enter valid start and end date/time values.',
      )
    }

    if (end <= start) {
      return t(
        'End date and time must be after the start date and time.',
      )
    }

    if (blockForm.notes.length > 1000) {
      return t(
        'Notes must not exceed 1000 characters.',
      )
    }

    return ''
  }

  const buildBlockPayload = () => ({
    blockType: blockForm.blockType,
    startAt: toOffsetDateTime(
      blockForm.startAt,
    ),
    endAt: toOffsetDateTime(blockForm.endAt),
    notes:
      blockForm.notes.trim() === ''
        ? null
        : blockForm.notes.trim(),
  })

  const handleSaveBlock = async (event) => {
    event.preventDefault()

    const validationError = validateBlockForm()

    if (validationError) {
      setBlockFormError(validationError)
      return
    }

    if (!selectedArea) return

    setBlockSaving(true)
    setBlockFormError('')
    setAvailabilityError('')

    try {
      if (editingBlock) {
        await updateCommonAreaAvailabilityBlock(
          editingBlock.id,
          buildBlockPayload(),
        )
      } else {
        await createCommonAreaAvailabilityBlock(
          selectedArea.id,
          buildBlockPayload(),
        )
      }

      await loadAvailabilityBlocks(
        selectedArea.id,
      )

      setEditingBlock(null)
      setBlockForm(createDefaultBlockForm())

      setSuccessMessage(
        editingBlock
          ? t(
              'Availability block updated successfully.',
            )
          : t(
              'Availability block created successfully.',
            ),
      )
    } catch (requestError) {
      setBlockFormError(
        requestError.message ||
          (editingBlock
            ? t(
                'Unable to update availability block.',
              )
            : t(
                'Unable to create availability block.',
              )),
      )
    } finally {
      setBlockSaving(false)
    }
  }

  const handleDeactivateBlock = async (block) => {
    const blockType = getBlockTypeLabel(
      block.blockType,
      t,
    )

    const confirmed = window.confirm(
      t(
        `Deactivate this ${blockType.toLowerCase()} block?`,
      ),
    )

    if (!confirmed) return

    setAvailabilityError('')
    setSuccessMessage('')

    try {
      await deactivateCommonAreaAvailabilityBlock(
        block.id,
      )

      if (selectedArea) {
        await loadAvailabilityBlocks(
          selectedArea.id,
        )
      }

      if (editingBlock?.id === block.id) {
        setEditingBlock(null)
        setBlockForm(createDefaultBlockForm())
      }

      setSuccessMessage(
        t(
          'Availability block deactivated successfully.',
        ),
      )
    } catch (requestError) {
      setAvailabilityError(
        requestError.message ||
          t(
            'Unable to deactivate availability block.',
          ),
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
          <p className="eyebrow">
            {t('BUILDING CONFIGURATION')}
          </p>

          <h1>{t('Common Areas')}</h1>

          <p className="muted">
            {t(
              'Manage the common areas available for reservations in each building.',
            )}
          </p>
        </div>

        <button
          className="button button-primary"
          type="button"
          onClick={openCreate}
          disabled={
            buildingsLoading ||
            buildings.length === 0
          }
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

      {amenitiesError && (
        <div
          className="feedback feedback-error"
          role="alert"
        >
          {amenitiesError}
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
                  <th>{t('Description')}</th>
                  <th>{t('Amenities')}</th>
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
                      {area.description ? (
                        <span
                          title={area.description}
                        >
                          {area.description.length > 80
                            ? `${area.description.slice(
                                0,
                                80,
                              )}…`
                            : area.description}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>

                    <td>
                      <AmenityList
                        amenities={area.amenities}
                        t={t}
                      />
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
                      <TableActions
                        moreLabel={t('More')}
                      >
                         <TableAction
                           icon="edit"
                           label={t('Edit')}
                           variant="edit"
                           onClick={() =>
                             openEdit(area)
                           }
                         />

                        <TableAction
                          icon="wrench"
                          label={t('Availability')}
                          variant="edit"
                          onClick={() =>
                            openAvailability(area)
                          }
                        />

                        <TableAction
                          icon="imagePlus"
                          label={t('Media')}
                          variant="edit"
                          onClick={() =>
                            openMedia(area)
                          }
                        />

                        <TableAction
                          icon="trash"
                          label={t('Deactivate')}
                          variant="delete"
                          onClick={() =>
                            handleDeactivate(area)
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
      </section>

      {/* =====================================================
          Create Common Area
          ===================================================== */}

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
                  event.target.value,
                )
              }
              disabled={
                saving || buildingsLoading
              }
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
            toggleAmenity={toggleAmenity}
            amenities={sortedAmenities}
            amenitiesLoading={amenitiesLoading}
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
              disabled={
                saving || buildingsLoading
              }
            >
              {saving
                ? t('Saving...')
                : t('Create Common Area')}
            </button>
          </div>
        </form>
      </Modal>

      {/* =====================================================
          Edit Common Area
          ===================================================== */}

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
            toggleAmenity={toggleAmenity}
            amenities={sortedAmenities}
            amenitiesLoading={amenitiesLoading}
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

      <Modal
        open={showMedia}
        title={
          selectedArea
            ? `${t('Common Area Media')} — ${selectedArea.name}`
            : t('Common Area Media')
        }
        onClose={closeMedia}
        size="large"
      >
        {selectedArea && (
          <CommonAreaMediaPanel
            commonArea={selectedArea}
            canManage={canManage}
          />
        )}
      </Modal>
      {/* =====================================================
          Availability / Repair Blocks
          ===================================================== */}

      <Modal
        open={showAvailability}
        title={t('Common Area Availability')}
        onClose={closeAvailability}
        size="large"
        closeOnBackdrop={!blockSaving}
      >
        {selectedArea && (
          <div className="availability-modal">
            <div className="availability-area-header">
              <div>
                <p className="eyebrow">
                  {t('COMMON AREA')}
                </p>

                <h3>{selectedArea.name}</h3>

                <p className="muted">
                  {selectedArea.buildingCode || '—'}
                  {' · '}
                  {selectedArea.areaType}
                </p>
              </div>

              <button
                className="button button-primary"
                type="button"
                onClick={openCreateBlock}
                disabled={blockSaving}
              >
                + {t('Add Block')}
              </button>
            </div>

            {availabilityError && (
              <div
                className="feedback feedback-error"
                role="alert"
              >
                {availabilityError}
              </div>
            )}

            {availabilityLoading ? (
              <div className="feedback feedback-info">
                {t(
                  'Loading availability blocks...',
                )}
              </div>
            ) : availabilityBlocks.length === 0 ? (
              <div className="empty-state availability-empty-state">
                <strong>
                  {t('No availability blocks')}
                </strong>

                <span>
                  {t(
                    'This common area currently has no unavailable or repair periods.',
                  )}
                </span>
              </div>
            ) : (
              <div className="availability-block-list">
                {availabilityBlocks.map((block) => (
                  <div
                    className="availability-block-card"
                    key={block.id}
                  >
                    <div className="availability-block-main">
                      <div className="availability-block-heading">
                        <span
                          className={`status-pill availability-type-pill ${
                            block.blockType ===
                            'REPAIR'
                              ? 'availability-repair'
                              : 'availability-unavailable'
                          }`}
                        >
                          {getBlockTypeLabel(
                            block.blockType,
                            t,
                          )}
                        </span>
                      </div>

                      <div className="availability-block-dates">
                        <div>
                          <span className="availability-label">
                            {t('Start')}
                          </span>

                          <strong>
                            {formatDateTime(
                              block.startAt,
                            )}
                          </strong>
                        </div>

                        <div>
                          <span className="availability-label">
                            {t('End')}
                          </span>

                          <strong>
                            {formatDateTime(
                              block.endAt,
                            )}
                          </strong>
                        </div>
                      </div>

                      {block.notes && (
                        <div className="availability-block-notes">
                          <span className="availability-label">
                            {t('Notes')}
                          </span>

                          <span>
                            {block.notes}
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="availability-block-actions">
                      <button
                        className="button button-secondary button-small"
                        type="button"
                        onClick={() =>
                          openEditBlock(block)
                        }
                        disabled={blockSaving}
                      >
                        {t('Edit')}
                      </button>

                      <button
                        className="button button-danger button-small"
                        type="button"
                        onClick={() =>
                          handleDeactivateBlock(
                            block,
                          )
                        }
                        disabled={blockSaving}
                      >
                        {t('Deactivate')}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="availability-block-form">
              <div className="availability-form-header">
                <div>
                  <p className="eyebrow">
                    {editingBlock
                      ? t('EDIT BLOCK')
                      : t('NEW BLOCK')}
                  </p>

                  <h4>
                    {editingBlock
                      ? t('Edit Availability Block')
                      : t('Add Availability Block')}
                  </h4>
                </div>

                {editingBlock && (
                  <button
                    className="button button-secondary button-small"
                    type="button"
                    onClick={openCreateBlock}
                    disabled={blockSaving}
                  >
                    {t('New block')}
                  </button>
                )}
              </div>

              <form
                className="form-grid"
                onSubmit={handleSaveBlock}
              >
                <label className="form-field">
                  <span>{t('Block Type')}</span>

                  <select
                    value={blockForm.blockType}
                    onChange={(event) =>
                      updateBlockForm(
                        'blockType',
                        event.target.value,
                      )
                    }
                    disabled={blockSaving}
                  >
                    <option value="UNAVAILABLE">
                      {t('Unavailable')}
                    </option>

                    <option value="REPAIR">
                      {t('Repair')}
                    </option>
                  </select>
                </label>

                <div />

                <label className="form-field">
                  <span>{t('Start')}</span>

                  <input
                    type="datetime-local"
                    value={blockForm.startAt}
                    onChange={(event) =>
                      updateBlockForm(
                        'startAt',
                        event.target.value,
                      )
                    }
                    disabled={blockSaving}
                    required
                  />
                </label>

                <label className="form-field">
                  <span>{t('End')}</span>

                  <input
                    type="datetime-local"
                    value={blockForm.endAt}
                    onChange={(event) =>
                      updateBlockForm(
                        'endAt',
                        event.target.value,
                      )
                    }
                    disabled={blockSaving}
                    required
                  />
                </label>

                <label className="form-field form-grid-full">
                  <span>{t('Notes')}</span>

                  <textarea
                    value={blockForm.notes}
                    onChange={(event) =>
                      updateBlockForm(
                        'notes',
                        event.target.value,
                      )
                    }
                    maxLength={1000}
                    rows={3}
                    placeholder={t(
                      'Optional explanation for this unavailable or repair period.',
                    )}
                    disabled={blockSaving}
                  />

                  <small className="muted">
                    {blockForm.notes.length}/1000
                  </small>
                </label>

                {blockFormError && (
                  <div
                    className="feedback feedback-error form-grid-full"
                    role="alert"
                  >
                    {blockFormError}
                  </div>
                )}

                <div className="modal-actions form-grid-full">
                  {editingBlock && (
                    <button
                      className="button button-secondary"
                      type="button"
                      onClick={openCreateBlock}
                      disabled={blockSaving}
                    >
                      {t('Cancel edit')}
                    </button>
                  )}

                  <button
                    className="button button-primary"
                    type="submit"
                    disabled={blockSaving}
                  >
                    {blockSaving
                      ? t('Saving...')
                      : editingBlock
                        ? t('Save changes')
                        : t('Create Block')}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

function CommonAreaFields({
  form,
  updateForm,
  toggleAmenity,
  amenities,
  amenitiesLoading,
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
            updateForm(
              'name',
              event.target.value,
            )
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
              event.target.value,
            )
          }
          maxLength={50}
          placeholder={t(
            'e.g. LOUNGE, GYM, POOL',
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
              event.target.value,
            )
          }
          disabled={disabled}
        />
      </label>

      <label className="form-field">
        <span>
          {t('Booking Duration (minutes)')}
        </span>

        <input
          type="number"
          min="1"
          step="1"
          value={form.bookingDurationMinutes}
          onChange={(event) =>
            updateForm(
              'bookingDurationMinutes',
              event.target.value,
            )
          }
          disabled={disabled}
        />
      </label>

      <label className="form-field form-grid-full">
        <span>{t('Booking Required')}</span>

        <select
          value={
            form.bookingRequired
              ? 'true'
              : 'false'
          }
          onChange={(event) =>
            updateForm(
              'bookingRequired',
              event.target.value === 'true',
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

      <label className="form-field form-grid-full">
        <span>{t('Description')}</span>

        <textarea
          value={form.description}
          onChange={(event) =>
            updateForm(
              'description',
              event.target.value,
            )
          }
          maxLength={5000}
          rows={4}
          placeholder={t(
            'Describe this common area, its features, and how it can be used.',
          )}
          disabled={disabled}
        />

        <small className="muted">
          {form.description.length}/5000
        </small>
      </label>

      <div className="form-field form-grid-full">
        <span>{t('Amenities')}</span>

        {amenitiesLoading ? (
          <div className="feedback feedback-info">
            {t('Loading amenities...')}
          </div>
        ) : amenities.length === 0 ? (
          <div className="empty-state">
            {t('No amenities available.')}
          </div>
        ) : (
          <div
            className="amenity-selector"
            role="group"
            aria-label={t('Amenities')}
          >
            {amenities.map((amenity) => {
              const selected =
                form.amenityIds.includes(
                  amenity.id,
                )

              return (
                <label
                  className={`amenity-option${
                    selected
                      ? ' amenity-option-selected'
                      : ''
                  }`}
                  key={amenity.id}
                >
                  <input
                    type="checkbox"
                    checked={selected}
                    onChange={() =>
                      toggleAmenity(
                        amenity.id,
                      )
                    }
                    disabled={disabled}
                  />

                  <span>{amenity.name}</span>
                </label>
              )
            })}
          </div>
        )}

        <small className="muted">
          {form.amenityIds.length}{' '}
          {t('amenities selected')}
        </small>
      </div>
    </>
  )
}

function AmenityList({ amenities }) {
  if (
    !Array.isArray(amenities) ||
    amenities.length === 0
  ) {
    return '—'
  }

  return (
    <div className="amenity-list">
      {amenities.map((amenity) => (
        <span
          className="status-pill amenity-pill"
          key={amenity.id}
        >
          {amenity.name}
        </span>
      ))}
    </div>
  )
}

export default CommonAreasPage