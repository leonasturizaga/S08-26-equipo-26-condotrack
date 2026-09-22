import { apiRequest } from './apiClient.js'

function matrixPath(buildingId) {
  if (!buildingId) return '/api/rbac/matrix'
  return `/api/rbac/matrix?buildingId=${encodeURIComponent(buildingId)}`
}

export function getRbacMatrix(buildingId = null) {
  return apiRequest(matrixPath(buildingId), { method: 'GET' })
}

export function updateRbacPermission({ buildingId = null, roleCode, permissionCode, active }) {
  return apiRequest('/api/rbac/permission-assignments', {
    method: 'PUT',
    body: {
      buildingId: buildingId || null,
      roleCode,
      permissionCode,
      active,
    },
  })
}

export function resetRbacBuildingOverride(buildingId, roleCode, permissionCode) {
  return apiRequest(
    `/api/rbac/buildings/${buildingId}/permission-overrides/${encodeURIComponent(roleCode)}/${encodeURIComponent(permissionCode)}`,
    { method: 'DELETE' },
  )
}

export function getRbacAudit(page = 0, size = 25) {
  return apiRequest(`/api/rbac/audit?page=${page}&size=${size}`, {
    method: 'GET',
  })
}
