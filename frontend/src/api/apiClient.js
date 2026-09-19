const DEFAULT_API_BASE_URL = 'http://localhost:8080'

export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL
).replace(/\/$/, '')

const AUTH_STORAGE_KEY = 'condotrack-auth-session'

export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.data = data
  }
}

export function getStoredAuthSession() {
  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

export function storeAuthSession(session) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
  // Remove the pre-API mock-auth key used by the initial frontend prototype.
  localStorage.removeItem('condotrack-auth-user')
}

function getAuthToken() {
  return getStoredAuthSession()?.token || null
}

async function parseResponseBody(response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    return response.json()
  }

  return response.text()
}

export async function apiRequest(path, options = {}) {
  const {
    headers: customHeaders,
    body,
    ...requestOptions
  } = options

  const headers = new Headers(customHeaders || {})
  const token = getAuthToken()

  headers.set('Accept', 'application/json')

  if (body !== undefined && body !== null) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...requestOptions,
      headers,
      body:
        body !== undefined && body !== null
          ? JSON.stringify(body)
          : undefined,
    })
  } catch (error) {
    throw new ApiError(
      'Unable to connect to the CondoTrack server.',
      0,
      { cause: error },
    )
  }

  const data = await parseResponseBody(response)

  if (!response.ok) {
    const message =
      typeof data === 'object' && data?.error
        ? data.error
        : `Request failed with status ${response.status}.`

    throw new ApiError(message, response.status, data)
  }

  return data
}
