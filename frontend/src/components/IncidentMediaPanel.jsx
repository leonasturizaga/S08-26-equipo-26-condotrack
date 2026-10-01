import { useCallback, useEffect, useMemo, useState } from 'react'

import {
  deleteIncidentMedia,
  getIncidentMedia,
  uploadIncidentMedia,
} from '../api/IncidentMediaApi.js'

const PURPOSES = [
  { value: 'PROBLEM_IMAGE', label: 'Problem image' },
  { value: 'SOLUTION_IMAGE', label: 'Solution image' },
  { value: 'DOCUMENT', label: 'Document / PDF' },
  { value: 'VIDEO', label: 'Video' },
]

const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const VIDEO_TYPES = ['video/mp4', 'video/webm', 'video/quicktime']

function formatBytes(bytes) {
  if (!bytes) return ''
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function isImage(media) {
  return media?.mediaType === 'IMAGE' || media?.contentType?.startsWith('image/')
}

function isPdf(media) {
  return media?.mediaType === 'DOCUMENT' || media?.contentType === 'application/pdf'
}

function validateFile(file, purpose) {
  if (!file) return 'Please select a file.'

  if (purpose === 'DOCUMENT') {
    if (file.type !== 'application/pdf' || !file.name.toLowerCase().endsWith('.pdf')) {
      return 'Only PDF documents are allowed.'
    }
    if (file.size > 20 * 1024 * 1024) {
      return 'PDF files must be 20 MB or smaller.'
    }
    return ''
  }

  if (purpose === 'VIDEO') {
    if (!VIDEO_TYPES.includes(file.type)) {
      return 'Only MP4, WebM or MOV videos are allowed.'
    }
    if (file.size > 50 * 1024 * 1024) {
      return 'Video files must be 50 MB or smaller.'
    }
    return ''
  }

  if (!IMAGE_TYPES.includes(file.type)) {
    return 'Only JPG, PNG or WebP images are allowed.'
  }

  if (file.size > 10 * 1024 * 1024) {
    return 'Images must be 10 MB or smaller.'
  }

  return ''
}

function mediaLabel(media) {
  if (media?.purpose === 'PROBLEM_IMAGE') return 'Problem'
  if (media?.purpose === 'SOLUTION_IMAGE') return 'Solution'
  if (media?.purpose === 'DOCUMENT') return 'Document'
  if (media?.purpose === 'VIDEO') return 'Video'
  return media?.purpose || 'Media'
}

export default function IncidentMediaPanel({
  incident,
  canManage = false,
  canUpload = false,
  t = (value) => value,
}) {
  const [media, setMedia] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [deletingId, setDeletingId] = useState('')
  const [purpose, setPurpose] = useState('PROBLEM_IMAGE')
  const [file, setFile] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const loadMedia = useCallback(async () => {
    if (!incident?.id) return

    setLoading(true)
    setError('')

    try {
      const response = await getIncidentMedia(incident.id)
      setMedia(Array.isArray(response) ? response : [])
    } catch (requestError) {
      setError(requestError.message || t('Unable to load incident media.'))
    } finally {
      setLoading(false)
    }
  }, [incident?.id, t])

  useEffect(() => {
    loadMedia()
  }, [loadMedia])

  const grouped = useMemo(() => ({
    problem: media.filter((item) => item.purpose === 'PROBLEM_IMAGE'),
    solution: media.filter((item) => item.purpose === 'SOLUTION_IMAGE'),
    documents: media.filter((item) => item.purpose === 'DOCUMENT'),
    videos: media.filter((item) => item.purpose === 'VIDEO'),
  }), [media])

  const handleUpload = async () => {
    const validationError = validateFile(file, purpose)

    if (validationError) {
      setError(validationError)
      return
    }

    setUploading(true)
    setError('')
    setSuccess('')

    try {
      await uploadIncidentMedia(incident.id, purpose, file)
      setFile(null)
      const input = document.getElementById('incident-media-file-input')
      if (input) input.value = ''
      await loadMedia()
      setSuccess(t('Media uploaded successfully.'))
    } catch (requestError) {
      setError(requestError.message || t('Unable to upload media.'))
    } finally {
      setUploading(false)
    }
  }

  const handleDelete = async (mediaId) => {
    if (!window.confirm(t('Delete this media file?'))) return

    setDeletingId(mediaId)
    setError('')
    setSuccess('')

    try {
      await deleteIncidentMedia(mediaId)
      await loadMedia()
      setSuccess(t('Media deleted successfully.'))
    } catch (requestError) {
      setError(requestError.message || t('Unable to delete media.'))
    } finally {
      setDeletingId('')
    }
  }

  const renderItem = (item) => (
    <article key={item.id} className="media-item">
      <div className="media-item-preview">
        {isImage(item) ? (
          <img
            src={item.secureUrl}
            alt={item.originalFilename || t('Incident media')}
            className="media-thumbnail"
          />
        ) : isPdf(item) ? (
          <div className="media-file-icon">PDF</div>
        ) : (
          <div className="media-file-icon">FILE</div>
        )}
      </div>

      <div className="media-item-info">
        <strong>{item.originalFilename || t('Untitled file')}</strong>
        <span>{t(mediaLabel(item))}</span>
        {item.fileSize ? <small>{formatBytes(item.fileSize)}</small> : null}
      </div>

      <div className="media-item-actions">
        <a
          className="button button-secondary button-small"
          href={item.secureUrl}
          target="_blank"
          rel="noreferrer"
        >
          {isPdf(item) ? t('Open PDF') : t('Open')}
        </a>

        {canManage && (
          <button
            className="button button-danger button-small"
            type="button"
            onClick={() => handleDelete(item.id)}
            disabled={deletingId === item.id}
          >
            {deletingId === item.id ? t('Deleting...') : t('Delete')}
          </button>
        )}
      </div>
    </article>
  )

  const renderGroup = (title, items) => (
    <section className="panel-section" style={{ marginTop: '18px' }}>
      <p className="form-section-label">{t(title)}</p>
      {items.length === 0 ? (
        <p className="muted">{t('No files uploaded.')}</p>
      ) : (
        <div className="media-list">
          {items.map(renderItem)}
        </div>
      )}
    </section>
  )

  return (
    <div className="panel-section" style={{ marginTop: '24px' }}>
      <p className="form-section-label">{t('Media')}</p>

      {error && (
        <div className="modal-feedback feedback feedback-error" role="alert">
          {error}
        </div>
      )}

      {success && (
        <div className="modal-feedback feedback feedback-success" role="status">
          {success}
        </div>
      )}

      {canUpload && (
        <div className="media-upload-panel">
          <div className="form-grid-2">
            <label className="form-field">
              <span>{t('Media type')}</span>
              <select
                value={purpose}
                onChange={(event) => setPurpose(event.target.value)}
                disabled={uploading}
              >
                {PURPOSES.map((option) => (
                  <option key={option.value} value={option.value}>
                    {t(option.label)}
                  </option>
                ))}
              </select>
            </label>

            <label className="form-field">
              <span>{t('File')}</span>
              <input
                id="incident-media-file-input"
                type="file"
                accept={
                  purpose === 'DOCUMENT'
                    ? 'application/pdf,.pdf'
                    : purpose === 'VIDEO'
                      ? 'video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov'
                      : 'image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp'
                }
                onChange={(event) => {
                  setFile(event.target.files?.[0] || null)
                  setError('')
                  setSuccess('')
                }}
                disabled={uploading}
              />
            </label>
          </div>

          <div className="entity-form-actions">
            <button
              className="button button-primary"
              type="button"
              onClick={handleUpload}
              disabled={uploading || !file}
            >
              {uploading ? t('Uploading...') : t('Upload media')}
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <div className="modal-loading-state">{t('Loading media...')}</div>
      ) : media.length === 0 ? (
        <p className="muted" style={{ marginTop: '16px' }}>
          {t('No media has been uploaded for this incident.')}
        </p>
      ) : (
        <>
          {renderGroup('Problem images', grouped.problem)}
          {renderGroup('Solution images', grouped.solution)}
          {renderGroup('Documents', grouped.documents)}
          {renderGroup('Videos', grouped.videos)}
        </>
      )}
    </div>
  )
}
