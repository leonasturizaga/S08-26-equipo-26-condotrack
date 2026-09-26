import { apiRequest } from './apiClient.js'

export function getBookings(page = 0, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return apiRequest(`/api/bookings?${params.toString()}`, { method: 'GET' })
}

export function getBooking(bookingId) {
  return apiRequest(`/api/bookings/${bookingId}`, { method: 'GET' })
}

export function createBooking(payload) {
  return apiRequest('/api/bookings', {
    method: 'POST',
    body: payload,
  })
}

export function updateBookingStatus(bookingId, payload) {
  return apiRequest(`/api/bookings/${bookingId}/status`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateBooking(bookingId, payload) {
  return apiRequest(`/api/bookings/${bookingId}`, {
    method: 'PUT',
    body: payload,
  })
}