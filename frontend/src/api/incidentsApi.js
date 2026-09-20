import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/incidents?${params.toString()}`
}

export function getIncidents(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), { method: 'GET' })
}

export function getIncidentUnitOptions() {
  return apiRequest('/api/incidents/options/units', { method: 'GET' })
}

export function getIncident(incidentId) {
  return apiRequest(`/api/incidents/${incidentId}`, { method: 'GET' })
}

export function createIncident(incident) {
  return apiRequest('/api/incidents', {
    method: 'POST',
    body: incident,
  })
}

export function updateIncident(incidentId, incident) {
  return apiRequest(`/api/incidents/${incidentId}`, {
    method: 'PUT',
    body: incident,
  })
}

export function updateIncidentAssignment(incidentId, assignedToStaffId) {
  return apiRequest(`/api/incidents/${incidentId}/assignment`, {
    method: 'PUT',
    body: { assignedToStaffId },
  })
}

export function updateIncidentStatus(incidentId, status, resolution = null) {
  return apiRequest(`/api/incidents/${incidentId}/status`, {
    method: 'PUT',
    body: { status, resolution },
  })
}

export function getAssignableStaff(buildingId) {
  const params = new URLSearchParams({ buildingId })

  return apiRequest(`/api/incidents/assignable-staff?${params.toString()}`, {
    method: 'GET',
  })
}
