import { apiRequest } from './apiClient.js'

export function getCommonAreas(buildingId = '', page = 0, size = 100) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (buildingId) params.set('buildingId', buildingId)
  return apiRequest(`/api/common-areas?${params.toString()}`, { method: 'GET' })
}

export function getCommonArea(commonAreaId) {
  return apiRequest(`/api/common-areas/${commonAreaId}`, { method: 'GET' })
}
