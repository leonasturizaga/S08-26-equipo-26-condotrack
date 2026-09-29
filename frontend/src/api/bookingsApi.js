//---------------- M24.3 ---------------------
// import { apiRequest } from './apiClient.js'

// export function getBookings(page = 0, size = 20) {
//   const params = new URLSearchParams({ page: String(page), size: String(size) })
//   return apiRequest(`/api/bookings?${params.toString()}`, { method: 'GET' })
// }

// export function getBooking(bookingId) {
//   return apiRequest(`/api/bookings/${bookingId}`, { method: 'GET' })
// }

// export function createBooking(payload) {
//   return apiRequest('/api/bookings', {
//     method: 'POST',
//     body: payload,
//   })
// }

// export function updateBookingStatus(bookingId, payload) {
//   return apiRequest(`/api/bookings/${bookingId}/status`, {
//     method: 'PUT',
//     body: payload,
//   })
// }

// export function updateBooking(bookingId, payload) {
//   return apiRequest(`/api/bookings/${bookingId}`, {
//     method: 'PUT',
//     body: payload,
//   })
// }


//---------------------- M24.4 --------------------
import { apiRequest } from './apiClient.js'

export function getBookings(page = 0, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return apiRequest(`/api/bookings?${params.toString()}`, { method: 'GET' })
}

export async function getAllBookings(size = 100) {
  const firstPage = await getBookings(0, size)

  const totalPages = Number(firstPage?.totalPages ?? 1)
  const firstContent = Array.isArray(firstPage?.content) ? firstPage.content : []

  if (totalPages <= 1) {
    return firstContent
  }

  const remainingPages = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, index) =>
      getBookings(index + 1, size),
    ),
  )

  return [
    ...firstContent,
    ...remainingPages.flatMap((page) =>
      Array.isArray(page?.content) ? page.content : [],
    ),
  ]
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