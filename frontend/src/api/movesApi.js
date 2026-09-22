import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/moves?${params.toString()}`
}

export function getMoves(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), { method: 'GET' })
}

export function getMove(moveId) {
  return apiRequest(`/api/moves/${moveId}`, { method: 'GET' })
}

export function getMoveUnitOptions() {
  return apiRequest('/api/moves/options/units', { method: 'GET' })
}

export function getMoveResidentOptions(unitId) {
  const params = new URLSearchParams({ unitId })
  return apiRequest(`/api/moves/options/residents?${params.toString()}`, { method: 'GET' })
}

export function createMove(payload) {
  return apiRequest('/api/moves', {
    method: 'POST',
    body: payload,
  })
}

export function updateMoveStatus(moveId, payload) {
  return apiRequest(`/api/moves/${moveId}/status`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateMoveOwnerAuthorization(moveId, payload) {
  return apiRequest(`/api/moves/${moveId}/owner-authorization`, {
    method: 'PUT',
    body: payload,
  })
}
