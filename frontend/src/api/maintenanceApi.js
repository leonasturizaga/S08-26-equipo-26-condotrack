import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return `/api/maintenance?${params.toString()}`
}

export function getMaintenance(page = 0, size = 20) { return apiRequest(buildPagePath(page, size), { method: 'GET' }) }
export function getMaintenanceUnitOptions() { return apiRequest('/api/maintenance/options/units', { method: 'GET' }) }
export function getMaintenanceRequest(id) { return apiRequest(`/api/maintenance/${id}`, { method: 'GET' }) }
export function getAssignableMaintenanceStaff(buildingId) { return apiRequest(`/api/maintenance/assignable-staff?buildingId=${encodeURIComponent(buildingId)}`, { method: 'GET' }) }
export function createMaintenance(payload) { return apiRequest('/api/maintenance', { method: 'POST', body: payload }) }
export function updateMaintenance(id, payload) { return apiRequest(`/api/maintenance/${id}`, { method: 'PUT', body: payload }) }
export function updateMaintenanceAssignment(id, assignedToStaffId) { return apiRequest(`/api/maintenance/${id}/assignment`, { method: 'PUT', body: { assignedToStaffId } }) }
export function updateMaintenanceStatus(id, status, resolution = null) { return apiRequest(`/api/maintenance/${id}/status`, { method: 'PUT', body: { status, resolution } }) }
