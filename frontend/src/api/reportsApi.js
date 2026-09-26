import { apiRequest } from './apiClient.js'

export function getReportsSummary(buildingId = '') {
  const params = new URLSearchParams()

  if (buildingId) {
    params.set('buildingId', buildingId)
  }

  const query = params.toString()
  return apiRequest(`/api/reports/summary${query ? `?${query}` : ''}`, {
    method: 'GET',
  })
}
