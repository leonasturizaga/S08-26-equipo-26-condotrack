import { createContext, useContext, useMemo, useState } from 'react'
import { authenticateMockUser } from './mockUsers.js'

const STORAGE_KEY = 'condotrack-auth-user'

const AuthContext = createContext(null)

function getStoredUser() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser)

  const login = (identifier, password) => {
    const authenticatedUser = authenticateMockUser(identifier, password)

    if (!authenticatedUser) {
      return { success: false }
    }

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(authenticatedUser)
    )

    setUser(authenticatedUser)

    return {
      success: true,
      user: authenticatedUser,
    }
  }

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY)
    setUser(null)
  }

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      login,
      logout,
    }),
    [user]
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}