import { useCallback, useEffect, useState } from 'react'
import { getAudit } from '../api/auditApi.js'
import { getBuildings } from '../api/buildingsApi.js'
import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 25

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

function titleize(value) {
  return String(value || '').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase())
}

export default function AuditPage() {
  const { t } = useTranslation()
  const [items, setItems] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [buildings, setBuildings] = useState([])
  const [filters, setFilters] = useState({ entityType: '', action: '', buildingId: '', actorEmail: '', from: '', to: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null)

  const loadBuildings = useCallback(async () => {
    try {
      const response = await getBuildings(0, 100)
      setBuildings(Array.isArray(response?.content) ? response.content : [])
    } catch {
      setBuildings([])
    }
  }, [])

  const loadAudit = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')
    try {
      const response = await getAudit({ ...filters, page: pageToLoad, size: PAGE_SIZE })
      setItems(Array.isArray(response?.items) ? response.items : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load audit records.'))
    } finally {
      setLoading(false)
    }
  }, [filters, t])

  useEffect(() => { loadBuildings() }, [loadBuildings])
  useEffect(() => { loadAudit(0) }, [loadAudit])

  const updateFilter = (event) => {
    const { name, value } = event.target
    setFilters((current) => ({ ...current, [name]: value }))
  }

  const clearFilters = () => setFilters({ entityType: '', action: '', buildingId: '', actorEmail: '', from: '', to: '' })

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('AUDIT / TRACEABILITY')}</p>
          <h2>{t('Audit & Traceability')}</h2>
          <p className="muted">{t('Review operational changes and who performed them.')}</p>
        </div>
        <button className="button button-secondary" type="button" onClick={() => loadAudit(page)} disabled={loading}>↻ {t('Refresh')}</button>
      </div>

      {error && <div className="feedback feedback-error" role="alert">{error}</div>}

      <article className="panel">
        <div className="form-grid-3">
          <label className="form-field"><span>{t('Module')}</span><input name="entityType" value={filters.entityType} onChange={updateFilter} placeholder="MOVE_REQUEST" /></label>
          <label className="form-field"><span>{t('Action')}</span><input name="action" value={filters.action} onChange={updateFilter} placeholder="STATUS_CHANGED" /></label>
          <label className="form-field"><span>{t('Building')}</span><select name="buildingId" value={filters.buildingId} onChange={updateFilter}><option value="">{t('All buildings')}</option>{buildings.map((building) => <option key={building.id} value={building.id}>{building.code} — {building.name}</option>)}</select></label>
          <label className="form-field"><span>{t('Actor email')}</span><input name="actorEmail" value={filters.actorEmail} onChange={updateFilter} /></label>
          <label className="form-field"><span>{t('From')}</span><input type="datetime-local" name="from" value={filters.from} onChange={updateFilter} /></label>
          <label className="form-field"><span>{t('To')}</span><input type="datetime-local" name="to" value={filters.to} onChange={updateFilter} /></label>
        </div>
        <div className="form-actions"><button className="button button-secondary" type="button" onClick={clearFilters}>{t('Clear')}</button><button className="button button-primary" type="button" onClick={() => loadAudit(0)} disabled={loading}>{t('Apply filters')}</button></div>
      </article>

      <article className="panel">
        <div className="panel-header"><div><p className="eyebrow">{t('AUDIT LOG')}</p><h3>{totalElements} {t('records')}</h3></div></div>
        {loading ? <div className="feedback feedback-info">{t('Loading...')}</div> : items.length === 0 ? <div className="empty-state">{t('No audit records found.')}</div> : (
          <div className="table-wrap"><table className="data-table"><thead><tr><th>{t('Date')}</th><th>{t('Actor')}</th><th>{t('Module')}</th><th>{t('Action')}</th><th>{t('Building')}</th><th>{t('Entity')}</th><th>{t('Actions')}</th></tr></thead><tbody>
          {items.map((item) => <tr key={item.id}><td>{formatDateTime(item.occurredAt)}</td><td>{item.actorEmail || '—'}</td><td>{titleize(item.entityType)}</td><td><span className="status-pill">{titleize(item.action)}</span></td><td>{item.buildingCode || '—'}</td><td>{String(item.entityId).slice(0, 8)}…</td><td><TableActions moreLabel={t('More')}><TableAction icon="eye" label={t('View')} variant="view" onClick={() => setSelected(item)} /></TableActions></td></tr>)}
          </tbody></table></div>
        )}
        {totalPages > 1 && <div className="pagination"><button className="button button-secondary button-small" type="button" onClick={() => loadAudit(page - 1)} disabled={page <= 0 || loading}>{t('Previous')}</button><span>{t('Page')} {page + 1} {t('of')} {totalPages}</span><button className="button button-secondary button-small" type="button" onClick={() => loadAudit(page + 1)} disabled={page + 1 >= totalPages || loading}>{t('Next')}</button></div>}
      </article>

      <Modal open={Boolean(selected)} onClose={() => setSelected(null)} eyebrow={t('AUDIT RECORD')} title={selected ? titleize(selected.action) : t('Audit record')} size="large">
        {selected && <div className="detail-grid"><div><span>{t('Date')}</span><strong>{formatDateTime(selected.occurredAt)}</strong></div><div><span>{t('Actor')}</span><strong>{selected.actorEmail || '—'}</strong></div><div><span>{t('Module')}</span><strong>{titleize(selected.entityType)}</strong></div><div><span>{t('Action')}</span><strong>{titleize(selected.action)}</strong></div><div><span>{t('Building')}</span><strong>{selected.buildingCode || '—'}</strong></div><div><span>{t('Entity ID')}</span><strong>{selected.entityId}</strong></div><div><span>{t('Previous status')}</span><strong>{selected.previousStatus || '—'}</strong></div><div><span>{t('New status')}</span><strong>{selected.newStatus || '—'}</strong></div><div className="detail-grid-wide"><span>{t('Resolution')}</span><strong>{selected.resolution || '—'}</strong></div><div className="detail-grid-wide"><span>{t('Details')}</span><pre className="audit-details">{JSON.stringify(selected.details || {}, null, 2)}</pre></div></div>}
      </Modal>
    </section>
  )
}
