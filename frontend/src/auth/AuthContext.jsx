//----------------- milestone 0 --------------------
// import { createContext, useContext, useMemo, useState } from 'react'
// import { authenticateMockUser } from './mockUsers.js'

// const STORAGE_KEY = 'condotrack-auth-user'

// const AuthContext = createContext(null)

// function getStoredUser() {
//   try {
//     const stored = localStorage.getItem(STORAGE_KEY)
//     return stored ? JSON.parse(stored) : null
//   } catch {
//     return null
//   }
// }

// export function AuthProvider({ children }) {
//   const [user, setUser] = useState(getStoredUser)

//   const login = (identifier, password) => {
//     const authenticatedUser = authenticateMockUser(identifier, password)

//     if (!authenticatedUser) {
//       return { success: false }
//     }

//     localStorage.setItem(
//       STORAGE_KEY,
//       JSON.stringify(authenticatedUser)
//     )

//     setUser(authenticatedUser)

//     return {
//       success: true,
//       user: authenticatedUser,
//     }
//   }

//   const logout = () => {
//     localStorage.removeItem(STORAGE_KEY)
//     setUser(null)
//   }

//   const value = useMemo(
//     () => ({
//       user,
//       isAuthenticated: Boolean(user),
//       login,
//       logout,
//     }),
//     [user]
//   )

//   return (
//     <AuthContext.Provider value={value}>
//       {children}
//     </AuthContext.Provider>
//   )
// }

// export function useAuth() {
//   const context = useContext(AuthContext)

//   if (!context) {
//     throw new Error('useAuth must be used inside AuthProvider')
//   }

//   return context
// }


//----------------- milestone 1 --------------------
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  clearAuthSession,
  getStoredAuthSession,
  storeAuthSession,
} from '../api/apiClient.js'
import { getCurrentUser, loginRequest } from '../api/authApi.js'

const AuthContext = createContext(null)

function normalizeUser(user) {
  if (!user) {
													
											 
		   
    return null
  }

  const roles = Array.isArray(user.roles) ? user.roles : []
  const primaryRole = roles[0] || user.role || null
  const name = [user.firstName, user.lastName]
    .filter(Boolean)
    .join(' ')
    .trim()

  return {
    ...user,
    roles,
    role: primaryRole,
    name: name || user.email,
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(getStoredAuthSession)
  const [user, setUser] = useState(() =>
    normalizeUser(getStoredAuthSession()?.user),
  )
  const [isInitializing, setIsInitializing] = useState(true)

  useEffect(() => {
    let cancelled = false

    const restoreSession = async () => {
      if (!session?.token) {
        if (!cancelled) {
          setIsInitializing(false)
        }
        return
      }

      try {
        const currentUser = normalizeUser(await getCurrentUser())

        if (cancelled) {
          return
        }

        const refreshedSession = {
          token: session.token,
          user: currentUser,
        }

        storeAuthSession(refreshedSession)
        setSession(refreshedSession)
        setUser(currentUser)
      } catch {
        if (!cancelled) {
          clearAuthSession()
          setSession(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) {
          setIsInitializing(false)
        }
      }
    }

    restoreSession()
				  
									   
	 

    return () => {
      cancelled = true
    }
    // Session restoration should happen once when AuthProvider mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(async (email, password) => {
    try {
      const response = await loginRequest(
        email.trim().toLowerCase(),
        password,
      )

      const normalizedUser = normalizeUser(response.user)
      const nextSession = {
        token: response.token,
        user: normalizedUser,
      }

      storeAuthSession(nextSession)
      setSession(nextSession)
      setUser(normalizedUser)

      return {
        success: true,
        user: normalizedUser,
      }
    } catch (error) {
      return {
        success: false,
        error,
      }
    }
  }, [])

  const logout = useCallback(() => {
    clearAuthSession()
    setSession(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      token: session?.token || null,
      isAuthenticated: Boolean(user && session?.token),
      isInitializing,
      login,
      logout,
    }),
    [user, session, isInitializing, login, logout],
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