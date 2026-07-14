import React, { createContext, useContext, useState, useCallback } from 'react'
import { authApi } from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('tm_user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const login = useCallback(async (email, password) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await authApi.login({ email, password })
      const auth = data.data
      localStorage.setItem('tm_access_token', auth.accessToken)
      localStorage.setItem('tm_refresh_token', auth.refreshToken)
      const userObj = {
        id: auth.userId,
        fullName: auth.fullName,
        email: auth.email,
        roles: auth.roles,
      }
      localStorage.setItem('tm_user', JSON.stringify(userObj))
      setUser(userObj)
      return userObj
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed')
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const register = useCallback(async (payload) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await authApi.register(payload)
      return data
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('tm_access_token')
    localStorage.removeItem('tm_refresh_token')
    localStorage.removeItem('tm_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, error, login, register, logout, setError }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
