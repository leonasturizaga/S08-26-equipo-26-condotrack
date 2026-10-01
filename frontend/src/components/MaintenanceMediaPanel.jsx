//----------------------- M24.8.5 --------------------------
import { useCallback, useEffect, useRef, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import {
  deleteMaintenanceMedia,
  getMaintenanceMedia,
  uploadMaintenanceMedia,
} from '../api/maintenanceMediaApi.js'

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const MAX_DOCUMENT_SIZE = 20 * 1024 * 1024

const IMAGE_TYPES = [
  'image/jpeg',
  'image/png',
  'image/webp',
]

const DOCUMENT_TYPES = [
  'application/pdf',
]

function formatFileSize(bytes) {
  if (!Number.isFinite(Number(bytes)) || Number(bytes) <= 0) {
    return ''
  }

  const value = Number(bytes)

  if (value < 1024 * 1024) {
    return `${Math.round(value / 1024)} KB`
  }

  return `${(value / (1024 * 1024)).toFixed(1)} MB`
}

function purposeLabel(purpose, t) {
  if (purpose === 'PROBLEM_IMAGE') {
    return t('Problem images')
  }

  if (purpose === 'SOLUTION_IMAGE') {
    return t('Solution images')
  }

  if (purpose === 'DOCUMENT') {
    return t('Documents')
  }

  return purpose
}

function purposeBadge(purpose, t) {
  if (purpose === 'PROBLEM_IMAGE') {
    return t('Problem')
  }

  if (purpose === 'SOLUTION_IMAGE') {
    return t('Solution')
  }

  if (purpose === 'DOCUMENT') {
    return t('Document')
  }

  return purpose
}

function validateFile(file, purpose, t) {
  if (!file) {
    return t('Please select a file.')
  }

  if (purpose === 'DOCUMENT') {
    if (!DOCUMENT_TYPES.includes(file.type)) {
      return t('Only PDF documents are allowed.')
    }

    if (file.size > MAX_DOCUMENT_SIZE) {
      return t('The document must be 20 MB or smaller.')
    }

    return ''
  }

  if (!IMAGE_TYPES.includes(file.type)) {
    return t('Only JPG, PNG, and WebP images are allowed.')
  }

  if (file.size > MAX_IMAGE_SIZE) {
    return t('The image must be 10 MB or smaller.')
  }

  return ''
}

function isImage(item) {
  return (
    item?.mediaType === 'IMAGE'
    || item?.contentType?.startsWith('image/')
  )
}

function isDocument(item) {
  return (
    item?.mediaType === 'DOCUMENT'
    || item?.contentType === 'application/pdf'
  )
}

function MaintenanceMediaPanel({
  maintenance,
  canManage = false,
  canUpload = false,
}) {
  const { t } = useTranslation()

  const [media, setMedia] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [uploadPurpose, setUploadPurpose] = useState('PROBLEM_IMAGE')

  const inputRef = useRef(null)

  const loadMedia = useCallback(async () => {
    if (!maintenance?.id) {
      setMedia([])
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await getMaintenanceMedia(
        maintenance.id,
      )

      setMedia(
        Array.isArray(response)
          ? response.filter(
              (item) => item?.active !== false,
            )
          : [],
      )
    } catch (requestError) {
      setError(
        requestError.message
          || t('Unable to load maintenance media.'),
      )

      setMedia([])
    } finally {
      setLoading(false)
    }
  }, [maintenance?.id, t])

  useEffect(() => {
    loadMedia()
  }, [loadMedia])

  const grouped = {
    PROBLEM_IMAGE: media.filter(
      (item) => item.purpose === 'PROBLEM_IMAGE',
    ),
    SOLUTION_IMAGE: media.filter(
      (item) => item.purpose === 'SOLUTION_IMAGE',
    ),
    DOCUMENT: media.filter(
      (item) => item.purpose === 'DOCUMENT',
    ),
  }

  const handleUpload = async (file) => {
    const validationError = validateFile(
      file,
      uploadPurpose,
      t,
    )

    if (validationError) {
      setError(validationError)
      return
    }

    setSaving(true)
    setError('')
    setSuccessMessage('')

    try {
      await uploadMaintenanceMedia(
        maintenance.id,
        uploadPurpose,
        file,
      )

      await loadMedia()

      setSuccessMessage(
        t('Maintenance media uploaded successfully.'),
      )
    } catch (requestError) {
      setError(
        requestError.message
          || t('Unable to upload maintenance media.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handleFileSelected = async (event) => {
    const file = event.target.files?.[0]

    event.target.value = ''

    if (!file) {
      return
    }

    await handleUpload(file)
  }

  const handleDelete = async (item) => {
    if (
      !window.confirm(
        t(
          `Delete "${item.originalFilename || 'this file'}"?`,
        ),
      )
    ) {
      return
    }

    setSaving(true)
    setError('')
    setSuccessMessage('')

    try {
      await deleteMaintenanceMedia(item.id)

      await loadMedia()

      setSuccessMessage(
        t('Maintenance media deleted successfully.'),
      )
    } catch (requestError) {
      setError(
        requestError.message
          || t('Unable to delete maintenance media.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const openFilePicker = () => {
    if (saving) {
      return
    }

    inputRef.current?.click()
  }

  const renderItem = (item) => {
    const image = isImage(item) && item.secureUrl
    const document = isDocument(item)

    return (
      <article
        className="compact-media-item"
        key={item.id}
      >
        <div className="compact-media-preview">
          {image ? (
            <a
              href={item.secureUrl}
              target="_blank"
              rel="noreferrer"
              className="compact-media-preview-link"
              title={t('Open image')}
            >
              <img
                src={item.secureUrl}
                alt={
                  item.originalFilename
                    || t('Maintenance media')
                }
                className="compact-media-thumbnail"
                loading="lazy"
              />
            </a>
          ) : (
            <div
              className={`compact-media-file-icon ${
                document
                  ? 'compact-media-file-pdf'
                  : ''
              }`}
              aria-hidden="true"
            >
              {document ? 'PDF' : 'FILE'}
            </div>
          )}
        </div>

        <div className="compact-media-info">
          <div className="compact-media-name">
            {item.originalFilename
              || t('Media file')}
          </div>

          <div className="compact-media-meta">
            <span className="compact-media-badge">
              {purposeBadge(item.purpose, t)}
            </span>

            {item.fileSize ? (
              <span>
                {formatFileSize(item.fileSize)}
              </span>
            ) : null}
          </div>
        </div>

        <div className="compact-media-actions">
          {item.secureUrl && (
            <a
              className="button button-secondary button-small"
              href={item.secureUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t('Open')}
            </a>
          )}

          {canManage && (
            <button
              className="button button-danger button-small"
              type="button"
              onClick={() => handleDelete(item)}
              disabled={saving}
            >
              {t('Delete')}
            </button>
          )}
        </div>
      </article>
    )
  }

  return (
    <section className="compact-media-panel">
      <div className="compact-media-header">
        <div>
          <h3>{t('Media')}</h3>

          <p>
            {t(
              'Attach problem images, solution images and documents to this maintenance request.',
            )}
          </p>
        </div>

        {canUpload && (
          <div className="compact-media-upload">
            <select
              value={uploadPurpose}
              onChange={(event) =>
                setUploadPurpose(event.target.value)
              }
              disabled={saving}
              aria-label={t('Media type')}
            >
              <option value="PROBLEM_IMAGE">
                {t('Problem image')}
              </option>

              <option value="SOLUTION_IMAGE">
                {t('Solution image')}
              </option>

              <option value="DOCUMENT">
                {t('Document')}
              </option>
            </select>

            <button
              className="button button-primary button-small"
              type="button"
              onClick={openFilePicker}
              disabled={saving}
            >
              {saving
                ? t('Uploading...')
                : t('Upload')}
            </button>

            <input
              ref={inputRef}
              type="file"
              hidden
              accept={
                uploadPurpose === 'DOCUMENT'
                  ? '.pdf,application/pdf'
                  : '.jpg,.jpeg,.png,.webp'
              }
              onChange={handleFileSelected}
            />
          </div>
        )}
      </div>

      {error && (
        <div
          className="feedback feedback-error compact-media-feedback"
          role="alert"
        >
          {error}
        </div>
      )}

      {successMessage && (
        <div
          className="feedback feedback-success compact-media-feedback"
          role="status"
        >
          {successMessage}
        </div>
      )}

      {loading ? (
        <div className="feedback feedback-info">
          {t('Loading maintenance media...')}
        </div>
      ) : (
        <div className="compact-media-groups">
          {[
            'PROBLEM_IMAGE',
            'SOLUTION_IMAGE',
            'DOCUMENT',
          ].map((purpose) => (
            <section
              className="compact-media-group"
              key={purpose}
            >
              <div className="compact-media-group-title">
                {purposeLabel(purpose, t)}
              </div>

              {grouped[purpose].length === 0 ? (
                <div className="compact-media-empty">
                  {t('No media uploaded.')}
                </div>
              ) : (
                <div className="compact-media-list">
                  {grouped[purpose].map(renderItem)}
                </div>
              )}
            </section>
          ))}
        </div>
      )}
    </section>
  )
}

export default MaintenanceMediaPanel