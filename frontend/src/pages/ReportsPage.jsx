import { useCallback, useEffect, useMemo, useState } from 'react'

import { getBuildings } from '../api/buildingsApi.js'
import { getReportsSummary } from '../api/reportsApi.js'
import Icon from '../components/Icon.jsx'
import { useTranslation } from '../i18n/i18n.js'

const STATUS_ORDER = ['PENDING', 'APPROVED', 'RECEIVED', 'NOTIFIED', 'CREATED', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'REQUESTED', 'PENDING_APPROVAL', 'SCHEDULED', 'RESOLVED', 'CLOSED', 'CANCELLED', 'REJECTED', 'COMPLETED', 'NO_SHOW']

const CATEGORY_LABELS = {
  BOOKINGS: 'Bookings',
  DELIVERIES: 'Deliveries',
  INCIDENTS: 'Incidents',
  MAINTENANCE: 'Maintenance',
  MOVES: 'Moves',
}

function formatMonth(value, locale) {
  if (!value) return '—'
  const [year, month] = value.split('-').map(Number)
  if (!year || !month) return value

  return new Intl.DateTimeFormat(locale || 'en', {
    month: 'short',
    year: 'numeric',
  }).format(new Date(year, month - 1, 1))
}

function formatStatus(status) {
  return String(status || '')
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function ReportsPage() {
  const { t, language } = useTranslation()
  const [buildings, setBuildings] = useState([])
  const [selectedBuildingId, setSelectedBuildingId] = useState('')
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [buildingLoading, setBuildingLoading] = useState(true)
  const [error, setError] = useState('')

  const loadBuildings = useCallback(async () => {
    setBuildingLoading(true)

    try {
      const response = await getBuildings(0, 100)
      setBuildings(Array.isArray(response?.content) ? response.content : [])
    } catch {
      setBuildings([])
    } finally {
      setBuildingLoading(false)
    }
  }, [])

  const loadReport = useCallback(async (buildingId = '') => {
    setLoading(true)
    setError('')

    try {
      const response = await getReportsSummary(buildingId)
      setReport(response)
    } catch (requestError) {
      setReport(null)
      setError(requestError.message || t('Unable to load reports.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadBuildings()
  }, [loadBuildings])

  useEffect(() => {
    loadReport(selectedBuildingId)
  }, [loadReport, selectedBuildingId])

  const statusGroups = useMemo(() => {
    const groups = new Map()

    ;(report?.statusMetrics || []).forEach((item) => {
      if (!groups.has(item.category)) {
        groups.set(item.category, [])
      }
      groups.get(item.category).push(item)
    })

    groups.forEach((items) => {
      items.sort((a, b) => {
        const aIndex = STATUS_ORDER.indexOf(a.status)
        const bIndex = STATUS_ORDER.indexOf(b.status)
        return (aIndex === -1 ? 999 : aIndex) - (bIndex === -1 ? 999 : bIndex)
      })
    })

    return Array.from(groups.entries())
  }, [report])

  const chartMax = useMemo(() => {
    const values = (report?.monthlyTrend || []).flatMap((point) => [
      point.accessEvents,
      point.deliveries,
      point.bookings,
      point.incidents,
      point.maintenance,
      point.moves,
    ])

    return Math.max(1, ...values)
  }, [report])

  const kpis = report?.kpis

  return (
    <section className="module-page reports-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('REPORTS / KPIS')}</p>
          <h2>{t('Reports / KPIs')}</h2>
          <p className="muted">
            {t('Operational indicators and cross-module activity for condominium management.')}
          </p>
        </div>

        <div className="reports-filter">
          <label htmlFor="report-building">{t('Building')}</label>
          <select
            id="report-building"
            className="select-input"
            value={selectedBuildingId}
            onChange={(event) => setSelectedBuildingId(event.target.value)}
            disabled={buildingLoading || loading}
          >
            <option value="">{t('All buildings')}</option>
            {buildings.map((building) => (
              <option key={building.id} value={building.id}>
                {building.code} — {building.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && (
        <div className="feedback feedback-error" role="alert">
          {error}
        </div>
      )}

      {loading && !report ? (
        <div className="panel loading-panel">
          <p className="muted">{t('Loading reports...')}</p>
        </div>
      ) : report ? (
        <>
          <div className="reports-kpi-grid">
            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="building" size={19} /></div>
              <span>{t('Buildings')}</span>
              <strong>{kpis.buildings}</strong>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="home" size={19} /></div>
              <span>{t('Active units')}</span>
              <strong>{kpis.activeUnits}</strong>
              <small>{kpis.occupiedUnits} {t('occupied')}</small>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="users" size={19} /></div>
              <span>{t('Occupancy')}</span>
              <strong>{Number(kpis.occupancyRate).toFixed(1)}%</strong>
              <small>{kpis.activeResidents} {t('active residents')}</small>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="door" size={19} /></div>
              <span>{t('Access events · 30d')}</span>
              <strong>{kpis.accessEvents30d}</strong>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="package" size={19} /></div>
              <span>{t('Pending deliveries')}</span>
              <strong>{kpis.pendingDeliveries}</strong>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="calendar" size={19} /></div>
              <span>{t('Upcoming bookings · 30d')}</span>
              <strong>{kpis.upcomingBookings30d}</strong>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="alert" size={19} /></div>
              <span>{t('Open incidents')}</span>
              <strong>{kpis.openIncidents}</strong>
            </article>

            <article className="panel report-kpi-card">
              <div className="report-kpi-icon"><Icon name="wrench" size={19} /></div>
              <span>{t('Open maintenance')}</span>
              <strong>{kpis.openMaintenance}</strong>
            </article>
          </div>

          <div className="reports-two-column">
            <article className="panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">{t('ACTIVITY')}</p>
                  <h3>{t('Six-month operational trend')}</h3>
                </div>
                <span className="status-pill">
                  {report.monthlyTrend.length} {t('months')}
                </span>
              </div>

              <div className="report-chart" aria-label={t('Six-month operational trend')}>
                {report.monthlyTrend.map((point) => (
                  <div className="report-chart-column" key={point.month}>
                    <div className="report-chart-bars">
                      {[
                        ['accessEvents', 'access'],
                        ['deliveries', 'deliveries'],
                        ['bookings', 'bookings'],
                        ['incidents', 'incidents'],
                        ['maintenance', 'maintenance'],
                        ['moves', 'moves'],
                      ].map(([field, type]) => (
                        <div
                          key={type}
                          className={`report-chart-bar report-chart-bar-${type}`}
                          style={{ height: `${Math.max(3, (Number(point[field] || 0) / chartMax) * 100)}%` }}
                          title={`${t(field)}: ${point[field]}`}
                        />
                      ))}
                    </div>
                    <span>{formatMonth(point.month, language)}</span>
                  </div>
                ))}
              </div>

              <div className="report-chart-legend">
                {[
                  ['accessEvents', 'access'],
                  ['deliveries', 'deliveries'],
                  ['bookings', 'bookings'],
                  ['incidents', 'incidents'],
                  ['maintenance', 'maintenance'],
                  ['moves', 'moves'],
                ].map(([field, type]) => (
                  <span key={type}>
                    <i className={`report-legend-dot report-chart-bar-${type}`} />
                    {t(field)}
                  </span>
                ))}
              </div>
            </article>

            <article className="panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">{t('STATUS')}</p>
                  <h3>{t('Operational status mix')}</h3>
                </div>
              </div>

              <div className="report-status-list">
                {statusGroups.length === 0 ? (
                  <p className="muted">{t('No status data available.')}</p>
                ) : (
                  statusGroups.map(([category, items]) => {
                    const total = items.reduce((sum, item) => sum + Number(item.count || 0), 0)

                    return (
                      <div className="report-status-group" key={category}>
                        <div className="report-status-group-header">
                          <strong>{t(CATEGORY_LABELS[category] || category)}</strong>
                          <span>{total}</span>
                        </div>

                        {items.map((item) => (
                          <div className="report-status-row" key={`${category}-${item.status}`}>
                            <span>{t(formatStatus(item.status))}</span>
                            <div className="report-status-track">
                              <div
                                className="report-status-fill"
                                style={{ width: `${total ? (Number(item.count) / total) * 100 : 0}%` }}
                              />
                            </div>
                            <strong>{item.count}</strong>
                          </div>
                        ))}
                      </div>
                    )
                  })
                )}
              </div>
            </article>
          </div>

          <article className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">{t('BUILDING OVERVIEW')}</p>
                <h3>{t('Building operational summary')}</h3>
              </div>
              <span className="status-pill">
                {report.buildings.length} {t('buildings')}
              </span>
            </div>

            <div className="table-wrap">
              <table className="data-table reports-table">
                <thead>
                  <tr>
                    <th>{t('Building')}</th>
                    <th>{t('Active units')}</th>
                    <th>{t('Occupancy')}</th>
                    <th>{t('Residents')}</th>
                    <th>{t('Open incidents')}</th>
                    <th>{t('Open maintenance')}</th>
                    <th>{t('Pending deliveries')}</th>
                  </tr>
                </thead>
                <tbody>
                  {report.buildings.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="table-empty">{t('No buildings found.')}</td>
                    </tr>
                  ) : (
                    report.buildings.map((building) => (
                      <tr key={building.buildingId}>
                        <td>
                          <strong>{building.code}</strong>
                          <span className="table-secondary">{building.name}</span>
                        </td>
                        <td>{building.activeUnits}</td>
                        <td>
                          <strong>{Number(building.occupancyRate).toFixed(1)}%</strong>
                          <span className="table-secondary">{building.occupiedUnits} {t('occupied')}</span>
                        </td>
                        <td>{building.activeResidents}</td>
                        <td>{building.openIncidents}</td>
                        <td>{building.openMaintenance}</td>
                        <td>{building.pendingDeliveries}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </article>

          <p className="reports-generated muted">
            {t('Generated')} {new Intl.DateTimeFormat(language || 'en', {
              dateStyle: 'medium',
              timeStyle: 'short',
            }).format(new Date(report.generatedAt))}
          </p>
        </>
      ) : null}
    </section>
  )
}

export default ReportsPage
