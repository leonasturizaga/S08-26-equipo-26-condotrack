import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/buildings?${params.toString()}`
}

export function getBuildings(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), {
    method: 'GET',
  })
}

export function getBuilding(buildingId) {
  return apiRequest(`/api/buildings/${buildingId}`, {
    method: 'GET',
  })
}

export function createBuilding(building) {
  return apiRequest('/api/buildings', {
    method: 'POST',
    body: building,
  })
}

export function updateBuilding(buildingId, building) {
  return apiRequest(`/api/buildings/${buildingId}`, {
    method: 'PUT',
    body: building,
  })
}
