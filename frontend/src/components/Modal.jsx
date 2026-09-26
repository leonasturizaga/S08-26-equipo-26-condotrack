import { useEffect, useId } from 'react'
import { createPortal } from 'react-dom'

import { useTranslation } from '../i18n/i18n.js'
import Icon from './Icon.jsx'

function Modal({
  open,
  onClose,
  eyebrow,
  title,
  children,
  footer,
  size = 'medium',
  closeOnBackdrop = true,
}) {
  const { t } = useTranslation()
  const modalId = useId()
  const titleId = `modal-title-${modalId}`

  useEffect(() => {
    if (!open) {
      return undefined
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open, onClose])

  if (!open) {
    return null
  }

  return createPortal(
    <div
      className="modal-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (closeOnBackdrop && event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div
        className={`modal-dialog modal-${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <header className="modal-header">
          <div>
            {eyebrow && <p className="eyebrow">{eyebrow}</p>}
            <h3 id={titleId}>{title}</h3>
          </div>

          <button
            className="icon-button modal-close"
            type="button"
            onClick={onClose}
            aria-label={t('Close')}
            title={t('Close')}
          >
            ×
          </button>
        </header>

        <div className="modal-body">
          {children}
        </div>

        {footer && (
          <footer className="modal-footer">
            {footer}
          </footer>
        )}
      </div>
    </div>,
    document.body,
  )
}

export default Modal
