//------------------------- M24.8.5 --------------------------
import { useCallback, useEffect, useRef, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import {
  deleteIncidentMedia,
  getIncidentMedia,
  uploadIncidentMedia,
} from '../api/IncidentMediaApi.js'

const PURPOSES = [
  {
    value: 'PROBLEM_IMAGE',
    label: 'Problem image',
  },
  {
    value: 'SOLUTION_IMAGE',
    label: 'Solution image',
  },
  {
    value: 'DOCUMENT',
    label: 'Document / PDF',
  },
  {
    value: 'VIDEO',
    label: 'Video',
  },
]

const IMAGE_TYPES = [
  'image/jpeg',
  'image/png',
  'image/webp',
]

const VIDEO_TYPES = [
  'video/mp4',
  'video/webm',
  'video/quicktime',
]

function formatBytes(bytes) {
  if (!Number.isFinite(Number(bytes)) || Number(bytes) <= 0) {
    return ''
  }

  const value = Number(bytes)

  if (value < 1024 * 1024) {
    return `${Math.round(value / 1024)} KB`
  }

  return `${(value / (1024 * 1024)).toFixed(1)} MB`
}

function isImage(media) {
  return (
    media?.mediaType === 'IMAGE'
    || media?.contentType?.startsWith('image/')
  )
}

function isPdf(media) {
  return (
    media?.mediaType === 'DOCUMENT'
    || media?.contentType === 'application/pdf'
  )
}

function validateFile(file, purpose, t) {
  if (!file) {
    return t('Please select a file.')
  }

  if (purpose === 'DOCUMENT') {
    if (
      file.type !== 'application/pdf'
      || !file.name.toLowerCase().endsWith('.pdf')
    ) {
      return t('Only PDF documents are allowed.')
    }

    if (file.size > 20 * 1024 * 1024) {
      return t('PDF files must be 20 MB or smaller.')
    }

    return ''
  }

  if (purpose === 'VIDEO') {
    if (!VIDEO_TYPES.includes(file.type)) {
      return t('Only MP4, WebM or MOV videos are allowed.')
    }

    if (file.size > 50 * 1024 * 1024) {
      return t('Video files must be 50 MB or smaller.')
    }

    return ''
  }

  if (!IMAGE_TYPES.includes(file.type)) {
    return t('Only JPG, PNG or WebP images are allowed.')
  }

  if (file.size > 10 * 1024 * 1024) {
    return t('Images must be 10 MB or smaller.')
  }

  return ''
}

function mediaLabel(media, t) {
  if (media?.purpose === 'PROBLEM_IMAGE') {
    return t('Problem')
  }

  if (media?.purpose === 'SOLUTION_IMAGE') {
    return t('Solution')
  }

  if (media?.purpose === 'DOCUMENT') {
    return t('Document')
  }

  if (media?.purpose === 'VIDEO') {
    return t('Video')
  }

  return media?.purpose || t('Media')
}

function IncidentMediaPanel({
  incident,
  canManage = false,
  canUpload = false,
}) {
  const { t } = useTranslation()

  const [media, setMedia] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [deletingId, setDeletingId] = useState('')
  const [purpose, setPurpose] = useState('PROBLEM_IMAGE')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const inputRef = useRef(null)

  const loadMedia = useCallback(async () => {
    if (!incident?.id) {
      setMedia([])
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await getIncidentMedia(
        incident.id,
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
          || t('Unable to load incident media.'),
      )
    } finally {
      setLoading(false)
    }
  }, [incident?.id, t])

  useEffect(() => {
    loadMedia()
  }, [loadMedia])

  const grouped = {
    problem: media.filter(
      (item) => item.purpose === 'PROBLEM_IMAGE',
    ),
    solution: media.filter(
      (item) => item.purpose === 'SOLUTION_IMAGE',
    ),
    documents: media.filter(
      (item) => item.purpose === 'DOCUMENT',
    ),
    videos: media.filter(
      (item) => item.purpose === 'VIDEO',
    ),
  }

  const handleFileSelected = async (event) => {
    const file = event.target.files?.[0]

    event.target.value = ''

    if (!file) {
      return
    }

    const validationError = validateFile(
      file,
      purpose,
      t,
    )

    if (validationError) {
      setError(validationError)
      return
    }

    setUploading(true)
    setError('')
    setSuccess('')

    try {
      await uploadIncidentMedia(
        incident.id,
        purpose,
        file,
      )

      await loadMedia()

      setSuccess(
        t('Media uploaded successfully.'),
      )
    } catch (requestError) {
      setError(
        requestError.message
          || t('Unable to upload media.'),
      )
    } finally {
      setUploading(false)
    }
  }

  const handleDelete = async (mediaId) => {
    if (
      !window.confirm(
        t('Delete this media file?'),
      )
    ) {
      return
    }

    setDeletingId(mediaId)
    setError('')
    setSuccess('')

    try {
      await deleteIncidentMedia(mediaId)

      await loadMedia()

      setSuccess(
        t('Media deleted successfully.'),
      )
    } catch (requestError) {
      setError(
        requestError.message
          || t('Unable to delete media.'),
      )
    } finally {
      setDeletingId('')
    }
  }

  const openFilePicker = () => {
    if (uploading) {
      return
    }

    inputRef.current?.click()
  }

  const renderItem = (item) => {
    const image = isImage(item) && item.secureUrl
    const pdf = isPdf(item)

    return (
      <article
        key={item.id}
        className="compact-media-item"
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
                    || t('Incident media')
                }
                className="compact-media-thumbnail"
                loading="lazy"
              />
            </a>
          ) : (
            <div
              className={`compact-media-file-icon ${
                pdf
                  ? 'compact-media-file-pdf'
                  : ''
              }`}
              aria-hidden="true"
            >
              {pdf ? 'PDF' : 'FILE'}
            </div>
          )}
        </div>

        <div className="compact-media-info">
          <div className="compact-media-name">
            {item.originalFilename
              || t('Untitled file')}
          </div>

          <div className="compact-media-meta">
            <span className="compact-media-badge">
              {mediaLabel(item, t)}
            </span>

            {item.fileSize ? (
              <span>
                {formatBytes(item.fileSize)}
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
              {pdf ? t('Open PDF') : t('Open')}
            </a>
          )}

          {canManage && (
            <button
              className="button button-danger button-small"
              type="button"
              onClick={() => handleDelete(item.id)}
              disabled={deletingId === item.id}
            >
              {deletingId === item.id
                ? t('Deleting...')
                : t('Delete')}
            </button>
          )}
        </div>
      </article>
    )
  }

  const renderGroup = (title, items) => (
    <section className="compact-media-group">
      <div className="compact-media-group-title">
        {t(title)}
      </div>

      {items.length === 0 ? (
        <div className="compact-media-empty">
          {t('No files uploaded.')}
        </div>
      ) : (
        <div className="compact-media-list">
          {items.map(renderItem)}
        </div>
      )}
    </section>
  )

  return (
    <section className="compact-media-panel">
      <div className="compact-media-header">
        <div>
          <h3>{t('Media')}</h3>

          <p>
            {t(
              'Attach images, documents and videos to this incident.',
            )}
          </p>
        </div>

        {canUpload && (
          <div className="compact-media-upload">
            <select
              value={purpose}
              onChange={(event) =>
                setPurpose(event.target.value)
              }
              disabled={uploading}
              aria-label={t('Media type')}
            >
              {PURPOSES.map((option) => (
                <option
                  key={option.value}
                  value={option.value}
                >
                  {t(option.label)}
                </option>
              ))}
            </select>

            <button
              className="button button-primary button-small"
              type="button"
              onClick={openFilePicker}
              disabled={uploading}
            >
              {uploading
                ? t('Uploading...')
                : t('Upload')}
            </button>

            <input
              ref={inputRef}
              type="file"
              hidden
              accept={
                purpose === 'DOCUMENT'
                  ? 'application/pdf,.pdf'
                  : purpose === 'VIDEO'
                    ? 'video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov'
                    : 'image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp'
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

      {success && (
        <div
          className="feedback feedback-success compact-media-feedback"
          role="status"
        >
          {success}
        </div>
      )}

      {loading ? (
        <div className="feedback feedback-info">
          {t('Loading media...')}
        </div>
      ) : media.length === 0 ? (
        <div className="compact-media-empty compact-media-empty-top">
          {t('No media has been uploaded for this incident.')}
        </div>
      ) : (
        <div className="compact-media-groups">
          {renderGroup(
            'Problem images',
            grouped.problem,
          )}

          {renderGroup(
            'Solution images',
            grouped.solution,
          )}

          {renderGroup(
            'Documents',
            grouped.documents,
          )}

          {renderGroup(
            'Videos',
            grouped.videos,
          )}
        </div>
      )}
    </section>
  )
}

export default IncidentMediaPanel
