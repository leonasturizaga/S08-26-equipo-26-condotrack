import { apiRequest } from './apiClient.js'

export function sendCommunication(payload) {
  return apiRequest('/api/communications', {
    method: 'POST',
    body: payload,
  })
}

export function getCommunications(page = 0, size = 20) {
  return apiRequest(`/api/communications?page=${page}&size=${size}`)
}

export function getCommunication(communicationId) {
  return apiRequest(`/api/communications/${communicationId}`)
}