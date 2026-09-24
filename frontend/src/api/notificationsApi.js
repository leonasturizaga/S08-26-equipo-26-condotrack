import { apiRequest } from './apiClient.js'

export function getNotifications(page = 0, size = 20) {
  return apiRequest(`/api/notifications?page=${page}&size=${size}`, {
    method: 'GET',
  })
}

export function getUnreadNotificationCount() {
  return apiRequest('/api/notifications/unread-count', {
    method: 'GET',
  })
}

export function getNotification(notificationId) {
  return apiRequest(`/api/notifications/${notificationId}`, {
    method: 'GET',
  })
}

export function markNotificationRead(notificationId) {
  return apiRequest(`/api/notifications/${notificationId}/read`, {
    method: 'PUT',
  })
}

export function markAllNotificationsRead() {
  return apiRequest('/api/notifications/read-all', {
    method: 'PUT',
  })
}
