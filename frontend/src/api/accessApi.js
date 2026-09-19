//------------------ milestone 4 ------------------   
// import { apiRequest } from './apiClient.js'

// export function createVisitorAuthorization(authorization) {
//   return apiRequest('/api/access/visitor-authorizations', {
//     method: 'POST',
//     body: authorization,
//   })
// }

// export function checkInVisitorAuthorization(authorizationId) {
//   return apiRequest(`/api/access/visitor-authorizations/${authorizationId}/check-in`, {
//     method: 'POST',
//   })
// }


//------------------ milestone 4.1 ------------------
import { apiRequest } from './apiClient.js'

export function createVisitorAuthorization(authorization) {
  return apiRequest('/api/access/visitor-authorizations', {
    method: 'POST',
    body: authorization,
  })
}

export function getVisitorAuthorizations(page = 0, size = 20, status = '') {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  if (status) {
    params.set('status', status)
  }

  return apiRequest(`/api/access/visitor-authorizations?${params.toString()}`)
}

export function getVisitorAuthorization(authorizationId) {
  return apiRequest(`/api/access/visitor-authorizations/${authorizationId}`)
}

export function checkInVisitorAuthorization(authorizationId) {
  return apiRequest(`/api/access/visitor-authorizations/${authorizationId}/check-in`, {
    method: 'POST',
  })
}

export function checkInVisitorByQrToken(qrToken) {
  return apiRequest('/api/access/visitor-authorizations/check-in', {
    method: 'POST',
    body: { qrToken },
  })
}

export function checkOutVisitorAuthorization(authorizationId) {
  return apiRequest(`/api/access/visitor-authorizations/${authorizationId}/check-out`, {
    method: 'POST',
  })
}

export function checkOutVisitorByQrToken(qrToken) {
  return apiRequest('/api/access/visitor-authorizations/check-out', {
    method: 'POST',
    body: { qrToken },
  })
}

export function getActiveVisitors(page = 0, size = 20) {
  return apiRequest(`/api/access/active-visitors?page=${page}&size=${size}`)
}
