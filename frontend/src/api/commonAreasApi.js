//---------------------- M24.3 ---------------------------
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

/* =========================================================
   Common Area Availability Blocks
   ========================================================= */

export function getCommonAreaAvailabilityBlocks(commonAreaId) {
  return apiRequest(
    `/api/common-areas/${commonAreaId}/availability-blocks`,
    {
      method: 'GET',
    },
  )
}

export function createCommonAreaAvailabilityBlock(
  commonAreaId,
  payload,
) {
  return apiRequest(
    `/api/common-areas/${commonAreaId}/availability-blocks`,
    {
      method: 'POST',
      body: payload,
    },
  )
}

export function updateCommonAreaAvailabilityBlock(
  blockId,
  payload,
) {
  return apiRequest(
    `/api/common-areas/availability-blocks/${blockId}`,
    {
      method: 'PUT',
      body: payload,
    },
  )
}

export function deactivateCommonAreaAvailabilityBlock(blockId) {
  return apiRequest(
    `/api/common-areas/availability-blocks/${blockId}`,
    {
      method: 'DELETE',
    },
  )
}

/* =========================================================
   Common Area Media
   M24.7
   ========================================================= */

export function getCommonAreaMedia(commonAreaId) {
  return apiRequest(
    `/api/media/common-areas/${commonAreaId}`,
    {
      method: 'GET',
    },
  )
}

function createMediaFormData(file) {
  const formData = new FormData()
  formData.append('file', file)
  return formData
}

export function uploadCommonAreaPrimary(commonAreaId, file) {
  return apiRequest(
    `/api/media/common-areas/${commonAreaId}/primary`,
    {
      method: 'POST',
      body: createMediaFormData(file),
    },
  )
}

export function replaceCommonAreaPrimary(commonAreaId, file) {
  return apiRequest(
    `/api/media/common-areas/${commonAreaId}/primary`,
    {
      method: 'PUT',
      body: createMediaFormData(file),
    },
  )
}

export function uploadCommonAreaGallery(commonAreaId, file) {
  return apiRequest(
    `/api/media/common-areas/${commonAreaId}/gallery`,
    {
      method: 'POST',
      body: createMediaFormData(file),
    },
  )
}

export function deleteCommonAreaMedia(commonAreaId, mediaId) {
  return apiRequest(
    `/api/media/common-areas/${commonAreaId}/${mediaId}`,
    {
      method: 'DELETE',
    },
  )
}