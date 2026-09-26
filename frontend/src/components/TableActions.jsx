import { cloneElement, isValidElement, useEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'

import Icon from './Icon.jsx'

function TableActions({ children, maxVisible = 4, moreLabel = 'More' }) {
  const [moreOpen, setMoreOpen] = useState(false)
  const [menuStyle, setMenuStyle] = useState(null)
  const moreButtonRef = useRef(null)
  const moreMenuRef = useRef(null)

  const actions = useMemo(
    () => children && Array.isArray(children)
      ? children.filter(isValidElement)
      : children && isValidElement(children)
        ? [children]
        : [],
    [children],
  )

  const visibleActions = actions.slice(0, maxVisible)
  const overflowActions = actions.slice(maxVisible)

  useEffect(() => {
    if (!moreOpen) return undefined

    const updateMenuPosition = () => {
      if (!moreButtonRef.current) return
      const rect = moreButtonRef.current.getBoundingClientRect()
      const menuWidth = Math.min(220, Math.max(180, rect.width * 6))
      const right = Math.max(8, window.innerWidth - rect.right)
      const left = Math.min(Math.max(8, rect.left), window.innerWidth - menuWidth - 8)

      setMenuStyle({
        top: rect.bottom + 6,
        left,
        width: menuWidth,
      })
    }

    const closeOnEscape = (event) => {
      if (event.key === 'Escape') setMoreOpen(false)
    }

    const closeOnOutsideClick = (event) => {
      if (moreButtonRef.current?.contains(event.target)) return
      if (moreMenuRef.current?.contains(event.target)) return
      setMoreOpen(false)
    }

    updateMenuPosition()
    window.addEventListener('resize', updateMenuPosition)
    window.addEventListener('scroll', updateMenuPosition, true)
    document.addEventListener('keydown', closeOnEscape)
    document.addEventListener('mousedown', closeOnOutsideClick)

    return () => {
      window.removeEventListener('resize', updateMenuPosition)
      window.removeEventListener('scroll', updateMenuPosition, true)
      document.removeEventListener('keydown', closeOnEscape)
      document.removeEventListener('mousedown', closeOnOutsideClick)
    }
  }, [moreOpen])

  if (actions.length === 0) return null

  const closeAfterAction = (action) => {
    if (action.props?.onClick) {
      action.props.onClick()
    }
    setMoreOpen(false)
  }

  return (
    <div className="table-actions">
      {visibleActions}

      {overflowActions.length > 0 && (
        <>
          <button
            ref={moreButtonRef}
            className="table-action table-action-more"
            type="button"
            onClick={() => setMoreOpen((current) => !current)}
            aria-label={moreLabel}
            title={moreLabel}
            aria-expanded={moreOpen}
            aria-haspopup="menu"
          >
            <Icon name="moreVertical" size={18} />
          </button>

          {moreOpen && menuStyle && createPortal(
            <div
              ref={moreMenuRef}
              className="table-action-menu"
              style={menuStyle}
              role="menu"
            >
              {overflowActions.map((action, index) => cloneElement(action, {
                key: action.key || `overflow-${index}`,
                menu: true,
                onClick: () => closeAfterAction(action),
              }))}
            </div>,
            document.body,
          )}
        </>
      )}
    </div>
  )
}

export default TableActions
