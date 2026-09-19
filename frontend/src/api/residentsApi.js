import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/residents?${params.toString()}`
}

export function getResidents(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), {
    method: 'GET',
  })
}

export function getResident(residentId) {
  return apiRequest(`/api/residents/${residentId}`, {
    method: 'GET',
  })
}

export function createResidentAssignment(resident) {
  return apiRequest('/api/residents', {
    method: 'POST',
    body: resident,
  })
}

export function updateResident(residentId, resident) {
  return apiRequest(`/api/residents/${residentId}`, {
    method: 'PUT',
    body: resident,
  })
}

export function reassignResident(residentId, unitId) {
  return apiRequest(`/api/residents/${residentId}/unit`, {
    method: 'PUT',
    body: { unitId },
  })
}
