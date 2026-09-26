import Icon from './Icon.jsx'

function TableAction({
  icon,
  label,
  onClick,
  variant = 'neutral',
  disabled = false,
  title,
  loading = false,
  loadingLabel,
  menu = false,
}) {
  const displayLabel = loading && loadingLabel ? loadingLabel : label
  const resolvedTitle = title || displayLabel

  return (
    <button
      className={menu ? `table-action-menu-item table-action-${variant}` : `table-action table-action-${variant}`}
      type="button"
      onClick={onClick}
      disabled={disabled}
      role={menu ? 'menuitem' : undefined}
      aria-label={displayLabel}
      title={menu ? undefined : resolvedTitle}
    >
      <Icon name={icon} size={17} />
      {menu && <span>{displayLabel}</span>}
    </button>
  )
}

export default TableAction
