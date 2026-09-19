import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/units?${params.toString()}`
}

export function getUnits(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), {
    method: 'GET',
  })
}

export function getUnit(unitId) {
  return apiRequest(`/api/units/${unitId}`, {
    method: 'GET',
  })
}

export function createUnit(unit) {
  return apiRequest('/api/units', {
    method: 'POST',
    body: unit,
  })
}

export function updateUnit(unitId, unit) {
  return apiRequest(`/api/units/${unitId}`, {
    method: 'PUT',
    body: unit,
  })
}
