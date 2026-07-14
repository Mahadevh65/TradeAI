import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ProtectedRoute from './ProtectedRoute'

export default function AdminRoute({ children }) {
  const { user } = useAuth()
  return (
    <ProtectedRoute>
      {user && !user.roles?.includes('ADMIN') ? <Navigate to="/dashboard" replace /> : children}
    </ProtectedRoute>
  )
}
