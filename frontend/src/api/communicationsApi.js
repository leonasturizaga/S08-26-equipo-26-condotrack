import { apiRequest } from './apiClient.js'

export function sendCommunication(payload) {
  return apiRequest('/api/communications', {
    method: 'POST',
    body: payload,
  })
}
