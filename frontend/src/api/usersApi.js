import { apiRequest } from './apiClient.js'

function buildPagePath(page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return `/api/users?${params.toString()}`
}

export function getUsers(page = 0, size = 20) {
  return apiRequest(buildPagePath(page, size), {
    method: 'GET',
  })
}

export function getUser(userId) {
  return apiRequest(`/api/users/${userId}`, {
    method: 'GET',
  })
}

export function createUser(user) {
  return apiRequest('/api/users', {
    method: 'POST',
    body: user,
  })
}

export function updateUser(userId, user) {
  return apiRequest(`/api/users/${userId}`, {
    method: 'PUT',
    body: user,
  })
}

export function updateUserRoles(userId, roles) {
  return apiRequest(`/api/users/${userId}/roles`, {
    method: 'PUT',
    body: { roles },
  })
}

export function updateUserStatus(userId, active) {
  return apiRequest(`/api/users/${userId}/status`, {
    method: 'PUT',
    body: { active },
  })
}
