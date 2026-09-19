import { apiRequest } from './apiClient.js'

export function loginRequest(email, password) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: {
      email,
      password,
    },
  })
}

export function getCurrentUser() {
  return apiRequest('/api/auth/me', {
    method: 'GET',
  })
}
