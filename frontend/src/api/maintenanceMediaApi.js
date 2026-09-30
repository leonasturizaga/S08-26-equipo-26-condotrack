import { apiRequest } from './apiClient.js'

export function getMaintenanceMedia(maintenanceId) {
  return apiRequest(
    `/api/media?entityType=MAINTENANCE&entityId=${encodeURIComponent(maintenanceId)}`,
    { method: 'GET' },
  )
}

export function uploadMaintenanceMedia(maintenanceId, purpose, file) {
  const formData = new FormData()
  formData.append('entityType', 'MAINTENANCE')
  formData.append('entityId', maintenanceId)
  formData.append('purpose', purpose)
  formData.append('file', file)

  return apiRequest('/api/media', {
    method: 'POST',
    body: formData,
  })
}

export function deleteMaintenanceMedia(mediaId) {
  return apiRequest(`/api/media/${mediaId}`, {
    method: 'DELETE',
  })
}
