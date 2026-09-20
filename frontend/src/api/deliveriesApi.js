import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/deliveries?${params.toString()}`
}

export function getDeliveries(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), {
    method: 'GET',
  })
}

export function getDelivery(deliveryId) {
  return apiRequest(`/api/deliveries/${deliveryId}`, {
    method: 'GET',
  })
}

export function createDelivery(delivery) {
  return apiRequest('/api/deliveries', {
    method: 'POST',
    body: delivery,
  })
}

export function updateDeliveryStatus(deliveryId, status) {
  return apiRequest(`/api/deliveries/${deliveryId}/status`, {
    method: 'PUT',
    body: { status },
  })
}
