import { useCallback, useEffect, useMemo, useState } from 'react'

import { getBuildings } from '../api/buildingsApi.js'
import {
  getRbacAudit,
  getRbacMatrix,
  resetRbacBuildingOverride,
  updateRbacPermission,
} from '../api/rbacApi.js'
import Modal from '../components/Modal.jsx'
import { useTranslation } from '../i18n/i18n.js'

const ACTION_COLUMNS = ['VIEW', 'CREATE', 'UPDATE', 'DELETE', 'OTHER']

const ACTION_ORDER = {
  VIEW: 1,
  CREATE: 2,
  UPDATE: 3,
  DELETE: 4,
  ASSIGN: 5,
  APPROVE: 6,
  CANCEL: 7,
  OTHER: 8,
}

function humanize(value) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function displayAction(permission) {
  if (ACTION_COLUMNS.includes(permission.action)) return permission.action
  return 'OTHER'
}

function PermissionToggle({ assignment, permission, buildingId, onToggle, onReset, t, disabled }) {
  const label = assignment.active ? 'ON' : 'OFF'
  const sourceLabel = buildingId
    ? assignment.overridden
      ? 'Override'
      : 'Inherited'
    : 'Global'

  return (
    <div className="permission-cell-control">
      <button
        className={`permission-toggle ${assignment.active ? 'permission-toggle-on' : 'permission-toggle-off'}`}
        type="button"
        disabled={disabled}
        title={`${permission.code} — ${sourceLabel}`}
        onClick={() => onToggle(permission, assignment)}
      >
        {label}
        <small>{permission.scope}</small>
      </button>

      {buildingId && assignment.overridden && (
        <button
          className="button button-ghost button-small"
          type="button"
          onClick={() => onReset(permission)}
          title="Return this permission to the global default"
        >
          {t('Reset override')}
        </button>
      )}
    </div>
  )
}

function RbacSettingsPage() {
  const { t } = useTranslation()

  const [matrix, setMatrix] = useState(null)
  const [audit, setAudit] = useState(null)
  const [buildings, setBuildings] = useState([])
  const [selectedBuilding, setSelectedBuilding] = useState('')
  const [selectedRole, setSelectedRole] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [auditLoading, setAuditLoading] = useState(true)
  const [confirmReset, setConfirmReset] = useState(null)

  const loadMatrix = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getRbacMatrix(selectedBuilding || null)
      setMatrix(data)
      setSelectedRole((currentRole) => currentRole || data.roles?.[0]?.code || '')
    } catch (err) {
      setError(err.message || t('Unable to load RBAC configuration.'))
    } finally {
      setLoading(false)
    }
  }, [selectedBuilding, t])

  const loadAudit = useCallback(async () => {
    setAuditLoading(true)
    try {
      setAudit(await getRbacAudit(0, 25))
    } catch (err) {
      setError(err.message || t('Unable to load RBAC audit history.'))
    } finally {
      setAuditLoading(false)
    }
  }, [t])

  useEffect(() => {
    let active = true
    getBuildings(0, 100)
      .then((data) => {
        if (active) setBuildings(Array.isArray(data?.content) ? data.content : [])
      })
      .catch((err) => {
        if (active) setError(err.message || t('Unable to load buildings.'))
      })
    return () => {
      active = false
    }
  }, [t])

  useEffect(() => {
    loadMatrix()
  }, [loadMatrix])

  useEffect(() => {
    loadAudit()
  }, [loadAudit])

  const assignmentMap = useMemo(() => {
    const map = new Map()
    ;(matrix?.assignments || []).forEach((assignment) => {
      map.set(`${assignment.roleCode}:${assignment.permissionCode}`, assignment)
    })
    return map
  }, [matrix])

  const permissionsByModule = useMemo(() => {
    const groups = new Map()
    ;(matrix?.permissions || []).forEach((permission) => {
      if (!groups.has(permission.module)) groups.set(permission.module, [])
      groups.get(permission.module).push(permission)
    })

    return [...groups.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([module, permissions]) => [
        module,
        permissions.sort((a, b) => {
          const actionDiff = (ACTION_ORDER[a.action] || 99) - (ACTION_ORDER[b.action] || 99)
          if (actionDiff !== 0) return actionDiff
          return a.code.localeCompare(b.code)
        }),
      ])
  }, [matrix])

  const handleToggle = async (permission, assignment) => {
    setSaving(true)
    setError('')
    try {
      const updated = await updateRbacPermission({
        buildingId: selectedBuilding || null,
        roleCode: selectedRole,
        permissionCode: permission.code,
        active: !assignment.active,
      })
      setMatrix(updated)
      await loadAudit()
    } catch (err) {
      setError(err.message || t('Unable to update RBAC permission.'))
    } finally {
      setSaving(false)
    }
  }

  const resetOverride = async () => {
    if (!confirmReset) return

    setSaving(true)
    setError('')
    try {
      const updated = await resetRbacBuildingOverride(
        selectedBuilding,
        selectedRole,
        confirmReset.code,
      )
      setMatrix(updated)
      setConfirmReset(null)
      await loadAudit()
    } catch (err) {
      setError(err.message || t('Unable to reset the building override.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">{t('GENERAL SETTINGS')}</p>
          <h2>{t('Roles & Permissions')}</h2>
          <p className="muted">
            {t('Control which permissions are active globally or override them for a specific building.')}
          </p>
        </div>
      </section>

      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">{t('RBAC CONFIGURATION')}</p>
            <h3>{t('Permission matrix')}</h3>
          </div>
          <span className="status-pill">
            {matrix?.scope === 'BUILDING' ? t('Building override view') : t('Global defaults')}
          </span>
        </div>

        <div className="form-grid-2">
          <label className="form-field">
            <span>{t('Scope')}</span>
            <select value={selectedBuilding} onChange={(event) => setSelectedBuilding(event.target.value)}>
              <option value="">{t('Global defaults')}</option>
              {buildings.map((building) => (
                <option key={building.id} value={building.id}>
                  {building.code} — {building.name}
                </option>
              ))}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Role')}</span>
            <select value={selectedRole} onChange={(event) => setSelectedRole(event.target.value)}>
              {(matrix?.roles || []).map((role) => (
                <option key={role.code} value={role.code}>{role.name}</option>
              ))}
            </select>
          </label>
        </div>

        <p className="panel-copy">
          {t('ON means the permission is active. OFF means the permission is disabled. Building overrides replace the global default for the selected building.')}
          {selectedBuilding && selectedRole === 'ADMINISTRATOR' && (
            <> {t('Administrator permissions are managed globally.')}</>
          )}
        </p>

        {loading ? (
          <div className="feedback feedback-info">{t('Loading permissions...')}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table permission-matrix-table">
              <thead>
                <tr>
                  <th>{t('Module')}</th>
                  <th>{t('View')}</th>
                  <th>{t('Create')}</th>
                  <th>{t('Update')}</th>
                  <th>{t('Delete')}</th>
                  <th>{t('Other')}</th>
                </tr>
              </thead>
              <tbody>
                {permissionsByModule.map(([module, permissions]) => (
                  <tr key={module}>
                    <td><strong>{t(humanize(module))}</strong></td>
                    {ACTION_COLUMNS.map((column) => (
                      <td key={column}>
                        <div className="permission-cell-list">
                          {permissions
                            .filter((permission) => displayAction(permission) === column)
                            .map((permission) => {
                              const assignment = assignmentMap.get(`${selectedRole}:${permission.code}`)
                              if (!assignment) return null
                              return (
                                <PermissionToggle
                                  key={permission.code}
                                  assignment={assignment}
                                  permission={permission}
                                  buildingId={selectedBuilding}
                                  onToggle={handleToggle}
                                  onReset={() => setConfirmReset(permission)}
                                  t={t}
                                  disabled={Boolean(selectedBuilding && selectedRole === 'ADMINISTRATOR')}
                                />
                              )
                            })}
                          {!permissions.some((permission) => displayAction(permission) === column) && <span className="muted">—</span>}
                        </div>
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {saving && <div className="feedback feedback-info">{t('Saving permission configuration...')}</div>}
      </article>

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">{t('AUDIT TRAIL')}</p>
            <h3>{t('Recent permission changes')}</h3>
          </div>
        </div>

        {auditLoading ? (
          <div className="feedback feedback-info">{t('Loading audit history...')}</div>
        ) : audit?.items?.length ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Date')}</th>
                  <th>{t('Building')}</th>
                  <th>{t('Role')}</th>
                  <th>{t('Permission')}</th>
                  <th>{t('Change')}</th>
                  <th>{t('Actor')}</th>
                </tr>
              </thead>
              <tbody>
                {audit.items.map((item) => (
                  <tr key={item.id}>
                    <td>{new Date(item.occurredAt).toLocaleString()}</td>
                    <td>{item.buildingCode || t('Global')}</td>
                    <td>{item.roleCode}</td>
                    <td>{item.permissionCode}</td>
                    <td>
                      <span className="status-pill">
                        {item.previousActive ? 'ON' : 'OFF'} → {item.newActive ? 'ON' : 'OFF'}
                      </span>
                    </td>
                    <td>{item.actorEmail || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">{t('No RBAC permission changes have been recorded yet.')}</div>
        )}
      </article>

      <Modal
        open={Boolean(confirmReset)}
        title={t('Reset building override')}
        onClose={() => setConfirmReset(null)}
      >
        <p className="panel-copy">
          {t('This will remove the building-specific override and return the permission to the global default.')}
        </p>
        <div className="modal-actions">
          <button className="button button-secondary" type="button" onClick={() => setConfirmReset(null)}>
            {t('Cancel')}
          </button>
          <button className="button button-primary" type="button" onClick={resetOverride} disabled={saving}>
            {saving ? t('Saving...') : t('Reset override')}
          </button>
        </div>
      </Modal>
    </>
  )
}

export default RbacSettingsPage
