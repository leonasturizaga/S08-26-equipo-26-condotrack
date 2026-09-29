//------------------------ M23.1----------------------
// import { apiRequest } from './apiClient.js'

// export function getCommonAreas(buildingId = '', page = 0, size = 100) {
//   const params = new URLSearchParams({ page: String(page), size: String(size) })
//   if (buildingId) params.set('buildingId', buildingId)
//   return apiRequest(`/api/common-areas?${params.toString()}`, { method: 'GET' })
// }

// export function getCommonArea(commonAreaId) {
//   return apiRequest(`/api/common-areas/${commonAreaId}`, { method: 'GET' })
// }


//--------------------------- M24.1 -----------------------------
import { apiRequest } from './apiClient.js'

export function getCommonAreas(buildingId = '', page = 0, size = 100) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  if (buildingId) {
    params.set('buildingId', buildingId)
  }

  return apiRequest(`/api/common-areas?${params.toString()}`, {
    method: 'GET',
  })
}

export function getCommonArea(commonAreaId) {
  return apiRequest(`/api/common-areas/${commonAreaId}`, {
    method: 'GET',
  })
}

export function getAmenities() {
  return apiRequest('/api/common-areas/amenities', {
    method: 'GET',
  })
}

export function createCommonArea(payload) {
  return apiRequest('/api/common-areas', {
    method: 'POST',
    body: payload,
  })
}

export function updateCommonArea(commonAreaId, payload) {
  return apiRequest(`/api/common-areas/${commonAreaId}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deactivateCommonArea(commonAreaId) {
  return apiRequest(`/api/common-areas/${commonAreaId}`, {
    method: 'DELETE',
  })
}