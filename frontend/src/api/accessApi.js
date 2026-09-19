import { apiRequest } from './apiClient.js'

export function createVisitorAuthorization(authorization) {
  return apiRequest('/api/access/visitor-authorizations', {
    method: 'POST',
    body: authorization,
  })
}

export function checkInVisitorAuthorization(authorizationId) {
  return apiRequest(`/api/access/visitor-authorizations/${authorizationId}/check-in`, {
    method: 'POST',
  })
}
