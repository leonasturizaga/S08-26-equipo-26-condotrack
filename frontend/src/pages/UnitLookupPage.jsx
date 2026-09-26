import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getUnitDashboard, lookupUnits } from '../api/unitsApi.js'
import { useTranslation } from '../i18n/i18n.js'
import Icon from '../components/Icon.jsx'

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function statusClass(status) {
  if (!status) return 'status-muted'
  const normalized = status.toLowerCase()
  if (['approved', 'completed', 'resolved', 'collected', 'closed'].includes(normalized)) {
    return 'status-success'
  }
  if (['pending', 'received', 'notified', 'scheduled', 'in_progress', 'open'].includes(normalized)) {
    return 'status-pending'
  }
  if (['rejected', 'cancelled', 'cancelled_by_resident'].includes(normalized)) {
    return 'status-error'
  }
  return 'status-info'
}

function Section({ title, count, children }) {
  return (
    <section className="unit-workspace-section">
      <div className="unit-workspace-section-header">
        <h3>{title}</h3>
        <span className="status-pill">{count}</span>
      </div>
      {children}
    </section>
  )
}

function EmptySection() {
  return <p className="muted unit-workspace-empty">No records found.</p>
}

function UnitLookupPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [selectedUnit, setSelectedUnit] = useState(null)
  const [dashboard, setDashboard] = useState(null)
  const [loadingResults, setLoadingResults] = useState(false)
  const [loadingDashboard, setLoadingDashboard] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const normalized = query.trim()

    if (normalized.length < 2) {
      setResults([])
      return undefined
    }

    let cancelled = false
    const timer = window.setTimeout(async () => {
      setLoadingResults(true)
      setError('')

      try {
        const response = await lookupUnits(normalized)
        if (!cancelled) {
          setResults(Array.isArray(response) ? response : [])
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(requestError.message || t('Unable to search units.'))
          setResults([])
        }
      } finally {
        if (!cancelled) setLoadingResults(false)
      }
    }, 250)

    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [query, t])

  const openUnit = async (unit) => {
    setSelectedUnit(unit)
    setDashboard(null)
    setLoadingDashboard(true)
    setError('')

    try {
      const response = await getUnitDashboard(unit.id)
      setDashboard(response)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load the unit workspace.'))
    } finally {
      setLoadingDashboard(false)
    }
  }

  const resetLookup = () => {
    setSelectedUnit(null)
    setDashboard(null)
    setQuery('')
    setResults([])
    setError('')
  }

  const current = dashboard || (selectedUnit ? {
    unit: {
      id: selectedUnit.id,
      buildingId: selectedUnit.buildingId,
      buildingName: selectedUnit.buildingName,
      unitNumber: selectedUnit.unitNumber,
      floorNumber: selectedUnit.floorNumber,
      unitType: selectedUnit.unitType,
    },
    residents: [],
    accessHistory: [],
    pendingDeliveries: [],
    bookings: [],
    activeMoveRequests: [],
    incidents: [],
    maintenanceRequests: [],
  } : null)

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('UNIT LOOKUP')}</p>
          <h2>{t('Unified Unit Lookup')}</h2>
          <p className="muted">
            {t('Search a unit and view its connected operational records in one workspace.')}
          </p>
        </div>

        {selectedUnit && (
          <button className="button button-secondary" type="button" onClick={resetLookup}>
            {t('New search')}
          </button>
        )}
      </div>

      <section className="data-panel">
        <label className="unit-lookup-search">
          <span>{t('Search by unit, building name or building code')}</span>
          <div className="unit-lookup-input-wrap">
            <Icon name="search" size={18} />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={t('e.g. 4B, Tower A, TWR-A')}
              aria-label={t('Unit lookup search')}
            />
          </div>
        </label>

        {query.trim().length > 0 && query.trim().length < 2 && (
          <p className="muted lookup-hint">{t('Enter at least 2 characters.')}</p>
        )}

        {loadingResults && (
          <div className="lookup-results-message">{t('Searching...')}</div>
        )}

        {!loadingResults && query.trim().length >= 2 && !selectedUnit && (
          results.length > 0 ? (
            <div className="lookup-results">
              {results.map((unit) => (
                <button
                  className="lookup-result"
                  type="button"
                  key={unit.id}
                  onClick={() => openUnit(unit)}
                >
                  <span className="lookup-result-icon"><Icon name="home" size={18} /></span>
                  <span className="lookup-result-main">
                    <strong>{unit.unitNumber}</strong>
                    <small>{unit.buildingName} ({unit.buildingCode})</small>
                  </span>
                  <span className="lookup-result-meta">
                    {unit.floorNumber == null ? '—' : `${t('Floor')} ${unit.floorNumber}`}
                  </span>
                  <Icon name="chevronRight" size={16} />
                </button>
              ))}
            </div>
          ) : (
            <div className="lookup-results-message">{t('No matching units found.')}</div>
          )
        )}
      </section>

      {error && (
        <div className="feedback feedback-error" role="alert">{error}</div>
      )}

      {loadingDashboard && (
        <section className="data-panel">
          <div className="modal-loading-state">{t('Loading unit workspace...')}</div>
        </section>
      )}

      {current && !loadingDashboard && (
        <section className="unit-workspace">
          <div className="unit-workspace-header">
            <div>
              <p className="eyebrow">{t('UNIT')}</p>
              <h3>{current.unit.unitNumber}</h3>
              <p className="muted">
                {current.unit.buildingName || selectedUnit?.buildingName || t('Building')}
              </p>
            </div>

            <div className="unit-workspace-meta">
              <span>{t('Floor')}: <strong>{current.unit.floorNumber ?? '—'}</strong></span>
              <span>{t('Type')}: <strong>{current.unit.unitType || '—'}</strong></span>
            </div>
          </div>

          <div className="unit-workspace-kpis">
            <div className="metric-card">
              <span className="metric-card-label">{t('Residents')}</span>
              <strong className="metric-card-value">{current.residents.length}</strong>
            </div>
            <div className="metric-card">
              <span className="metric-card-label">{t('Pending deliveries')}</span>
              <strong className="metric-card-value">{current.pendingDeliveries.length}</strong>
            </div>
            <div className="metric-card">
              <span className="metric-card-label">{t('Open incidents')}</span>
              <strong className="metric-card-value">{current.incidents.length}</strong>
            </div>
            <div className="metric-card">
              <span className="metric-card-label">{t('Maintenance requests')}</span>
              <strong className="metric-card-value">{current.maintenanceRequests.length}</strong>
            </div>
          </div>

          <div className="unit-workspace-grid">
            <Section title={t('Residents')} count={current.residents.length}>
              {current.residents.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.residents.map((resident) => (
                    <div className="workspace-list-row" key={resident.id}>
                      <div>
                        <strong>{resident.firstName} {resident.lastName}</strong>
                        <span>{resident.residentType}{resident.primaryContact ? ` · ${t('Primary contact')}` : ''}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Pending deliveries')} count={current.pendingDeliveries.length}>
              {current.pendingDeliveries.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.pendingDeliveries.map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.deliveryType}</strong>
                        <span>{item.carrierName || t('Unknown carrier')} · {formatDate(item.receivedAt)}</span>
                      </div>
                      <span className={`status-badge ${statusClass(item.status)}`}>{item.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Recent access')} count={current.accessHistory.length}>
              {current.accessHistory.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.accessHistory.slice(0, 8).map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.direction} · {item.accessMethod}</strong>
                        <span>{formatDate(item.occurredAt)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Bookings')} count={current.bookings.length}>
              {current.bookings.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.bookings.slice(0, 8).map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.commonAreaName || item.commonAreaId}</strong>
                        <span>{formatDate(item.startAt)} — {formatDate(item.endAt)}</span>
                      </div>
                      <span className={`status-badge ${statusClass(item.status)}`}>{item.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Move requests')} count={current.activeMoveRequests.length}>
              {current.activeMoveRequests.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.activeMoveRequests.map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.requestType}</strong>
                        <span>{item.scheduledStart ? formatDate(item.scheduledStart) : formatDate(item.requestedAt)}</span>
                      </div>
                      <span className={`status-badge ${statusClass(item.status)}`}>{item.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Incidents')} count={current.incidents.length}>
              {current.incidents.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.incidents.slice(0, 8).map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.title}</strong>
                        <span>{item.severity} · {formatDate(item.occurredAt)}</span>
                      </div>
                      <span className={`status-badge ${statusClass(item.status)}`}>{item.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Section>

            <Section title={t('Maintenance')} count={current.maintenanceRequests.length}>
              {current.maintenanceRequests.length === 0 ? <EmptySection /> : (
                <div className="workspace-list">
                  {current.maintenanceRequests.slice(0, 8).map((item) => (
                    <div className="workspace-list-row" key={item.id}>
                      <div>
                        <strong>{item.category || t('Maintenance request')}</strong>
                        <span>{item.priority} · {formatDate(item.scheduledAt || item.createdAt)}</span>
                      </div>
                      <span className={`status-badge ${statusClass(item.status)}`}>{item.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Section>
          </div>
        </section>
      )}
    </section>
  )
}

export default UnitLookupPage
