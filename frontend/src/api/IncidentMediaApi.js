import { apiRequest } from './apiClient.js'

export function getIncidentMedia(incidentId) {
  return apiRequest(
    `/api/media?entityType=INCIDENT&entityId=${encodeURIComponent(incidentId)}`,
    { method: 'GET' },
  )
}

export function uploadIncidentMedia(incidentId, purpose, file) {
  const formData = new FormData()
  formData.append('entityType', 'INCIDENT')
  formData.append('entityId', incidentId)
  formData.append('purpose', purpose)
  formData.append('file', file)

  return apiRequest('/api/media', {
    method: 'POST',
    body: formData,
  })
}

export function deleteIncidentMedia(mediaId) {
  return apiRequest(`/api/media/${mediaId}`, {
    method: 'DELETE',
  })
}
