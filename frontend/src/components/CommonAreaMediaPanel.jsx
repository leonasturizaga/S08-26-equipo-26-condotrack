import { useCallback, useEffect, useRef, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import {
  deleteCommonAreaMedia,
  getCommonAreaMedia,
  replaceCommonAreaPrimary,
  uploadCommonAreaGallery,
  uploadCommonAreaPrimary,
} from '../api/commonAreasApi.js'

const MAX_FILE_SIZE = 10 * 1024 * 1024
const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const ACCEPTED_EXTENSIONS = '.jpg,.jpeg,.png,.webp'

function getMediaByPurpose(media, purpose) {
  return media.filter(
    (item) =>
      item?.active !== false &&
      item?.purpose === purpose,
  )
}

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

function validateImage(file, t) {
  if (!file) {
    return t('Please select an image.')
  }

  if (!ACCEPTED_TYPES.includes(file.type)) {
    return t('Only JPG, PNG, and WebP images are allowed.')
  }

  if (file.size > MAX_FILE_SIZE) {
    return t('The image must be 10 MB or smaller.')
  }

  return ''
}

function MediaImage({ media, alt }) {
  if (!media?.secureUrl) {
    return (
      <div className="common-area-media-placeholder">
        <span>—</span>
      </div>
    )
  }

  return (
    <img
      src={media.secureUrl}
      alt={alt}
      className="common-area-media-image"
      loading="lazy"
    />
  )
}

function CommonAreaMediaPanel({
  commonArea,
  canManage,
}) {
  const { t } = useTranslation()

  const [media, setMedia] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const primaryInputRef = useRef(null)
  const galleryInputRef = useRef(null)

  const loadMedia = useCallback(async () => {
    if (!commonArea?.id) {
      setMedia([])
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await getCommonAreaMedia(commonArea.id)

      setMedia(
        Array.isArray(response)
          ? response.filter((item) => item?.active !== false)
          : [],
      )
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to load Common Area media.'),
      )
      setMedia([])
    } finally {
      setLoading(false)
    }
  }, [commonArea?.id, t])

  useEffect(() => {
    loadMedia()
  }, [loadMedia])

  const primary = getMediaByPurpose(
    media,
    'PRIMARY_IMAGE',
  )[0] || null

  const gallery = getMediaByPurpose(
    media,
    'GALLERY_IMAGE',
  ).sort(
    (a, b) =>
      Number(a.sortOrder ?? 0) -
      Number(b.sortOrder ?? 0),
  )

  const runMutation = async (
    operation,
    successText,
  ) => {
    setSaving(true)
    setError('')
    setSuccessMessage('')

    try {
      await operation()
      await loadMedia()
      setSuccessMessage(successText)
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to update Common Area media.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handlePrimarySelected = async (event) => {
    const file = event.target.files?.[0]
    event.target.value = ''

    const validationError = validateImage(file, t)

    if (validationError) {
      setError(validationError)
      return
    }

    await runMutation(
      () =>
        primary
          ? replaceCommonAreaPrimary(
              commonArea.id,
              file,
            )
          : uploadCommonAreaPrimary(
              commonArea.id,
              file,
            ),
      primary
        ? t('Primary image replaced successfully.')
        : t('Primary image uploaded successfully.'),
    )
  }

  const handleGallerySelected = async (event) => {
    const file = event.target.files?.[0]
    event.target.value = ''

    const validationError = validateImage(file, t)

    if (validationError) {
      setError(validationError)
      return
    }

    if (gallery.length >= 3) {
      setError(
        t('The gallery can contain a maximum of 3 images.'),
      )
      return
    }

    await runMutation(
      () =>
        uploadCommonAreaGallery(
          commonArea.id,
          file,
        ),
      t('Gallery image uploaded successfully.'),
    )
  }

  const handleDelete = async (item) => {
    const confirmed = window.confirm(
      t(
        `Delete "${item.originalFilename || 'this image'}"?`,
      ),
    )

    if (!confirmed) {
      return
    }

    await runMutation(
      () =>
        deleteCommonAreaMedia(
          commonArea.id,
          item.id,
        ),
      t('Media deleted successfully.'),
    )
  }

  return (
    <section className="panel common-area-media-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">
            {t('MEDIA')}
          </p>

          <h2>{t('Common Area Images')}</h2>

          <p className="muted">
            {t(
              'Manage the primary image and gallery images for this Common Area.',
            )}
          </p>
        </div>

        {canManage && (
          <span className="status-pill">
            {gallery.length}/3 {t('gallery images')}
          </span>
        )}
      </div>

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

      {loading ? (
        <div className="feedback feedback-info">
          {t('Loading Common Area media...')}
        </div>
      ) : (
        <div className="common-area-media-content">
          <div className="common-area-primary-section">
            <div className="common-area-media-heading">
              <div>
                <p className="eyebrow">
                  {t('PRIMARY IMAGE')}
                </p>

                <h3>
                  {primary
                    ? t('Primary image')
                    : t('No primary image')}
                </h3>
              </div>

              {canManage && (
                <>
                  <input
                    ref={primaryInputRef}
                    className="common-area-hidden-file-input"
                    type="file"
                    accept={ACCEPTED_EXTENSIONS}
                    onChange={handlePrimarySelected}
                    disabled={saving}
                  />

                  <button
                    className="button button-secondary button-small"
                    type="button"
                    onClick={() =>
                      primaryInputRef.current?.click()
                    }
                    disabled={saving}
                  >
                    {primary
                      ? t('Replace image')
                      : t('Upload image')}
                  </button>
                </>
              )}
            </div>

            <div className="common-area-primary-card">
              {primary ? (
                <>
                  <MediaImage
                    media={primary}
                    alt={`${commonArea.name} ${t('primary image')}`}
                  />

                  <div className="common-area-media-meta">
                    <strong>
                      {primary.originalFilename || t('Image')}
                    </strong>

                    {primary.fileSize && (
                      <span className="muted">
                        {formatFileSize(primary.fileSize)}
                      </span>
                    )}
                  </div>
                </>
              ) : (
                <div className="common-area-media-empty">
                  <strong>
                    {t('No primary image uploaded.')}
                  </strong>

                  <span className="muted">
                    {canManage
                      ? t(
                          'Upload an image to make this the main image for the Common Area.',
                        )
                      : t(
                          'An administrator has not uploaded a primary image yet.',
                        )}
                  </span>
                </div>
              )}
            </div>
          </div>

          <div className="common-area-gallery-section">
            <div className="common-area-media-heading">
              <div>
                <p className="eyebrow">
                  {t('GALLERY')}
                </p>

                <h3>
                  {t('Gallery images')}
                </h3>
              </div>

              {canManage && (
                <>
                  <input
                    ref={galleryInputRef}
                    className="common-area-hidden-file-input"
                    type="file"
                    accept={ACCEPTED_EXTENSIONS}
                    onChange={handleGallerySelected}
                    disabled={
                      saving || gallery.length >= 3
                    }
                  />

                  <button
                    className="button button-primary button-small"
                    type="button"
                    onClick={() =>
                      galleryInputRef.current?.click()
                    }
                    disabled={
                      saving || gallery.length >= 3
                    }
                  >
                    + {t('Upload gallery image')}
                  </button>
                </>
              )}
            </div>

            {gallery.length === 0 ? (
              <div className="common-area-media-empty">
                <strong>
                  {t('No gallery images uploaded.')}
                </strong>

                <span className="muted">
                  {canManage
                    ? t(
                        'You can upload up to 3 gallery images.',
                      )
                    : t(
                        'No gallery images are available for this Common Area.',
                      )}
                </span>
              </div>
            ) : (
              <div className="common-area-gallery-grid">
                {gallery.map((item, index) => (
                  <article
                    className="common-area-gallery-card"
                    key={item.id}
                  >
                    <div className="common-area-gallery-image-wrap">
                      <MediaImage
                        media={item}
                        alt={`${commonArea.name} ${t('gallery image')} ${index + 1}`}
                      />

                      <span className="common-area-gallery-order">
                        {index + 1}
                      </span>
                    </div>

                    <div className="common-area-gallery-details">
                      <strong
                        title={item.originalFilename || ''}
                      >
                        {item.originalFilename ||
                          t(`Gallery image ${index + 1}`)}
                      </strong>

                      {item.fileSize && (
                        <span className="muted">
                          {formatFileSize(item.fileSize)}
                        </span>
                      )}

                      {canManage && (
                        <button
                          className="button button-danger button-small"
                          type="button"
                          onClick={() =>
                            handleDelete(item)
                          }
                          disabled={saving}
                        >
                          {t('Delete')}
                        </button>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  )
}

export default CommonAreaMediaPanel
