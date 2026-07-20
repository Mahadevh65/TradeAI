import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import ForgotPassword from './pages/auth/ForgotPassword'
import ResetPassword from './pages/auth/ResetPassword'
import VerifyEmail from './pages/auth/VerifyEmail'
import Dashboard from './pages/Dashboard'
import Portfolio from './pages/portfolio/Portfolio'
import Watchlist from './pages/watchlist/Watchlist'
import Markets from './pages/stock/Markets'
import StockDetail from './pages/stock/StockDetail'
import AiCopilot from './pages/ai/AiCopilot'
import News from './pages/news/News'
import Analytics from './pages/analytics/Analytics'
import Admin from './pages/admin/Admin'
import ProtectedRoute from './routes/ProtectedRoute'
import AdminRoute from './routes/AdminRoute'

export default function App() {
  return (
    <Routes>
      {/* <Route path="/" element={<Navigate to="/login" replace />} /> */}
      <Route path="/" element={<Navigate to="/markets" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/verify-email" element={<VerifyEmail />} />

      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/portfolio" element={<ProtectedRoute><Portfolio /></ProtectedRoute>} />
      <Route path="/watchlist" element={<ProtectedRoute><Watchlist /></ProtectedRoute>} />
      {/* <Route path="/markets" element={<ProtectedRoute><Markets /></ProtectedRoute>} />
      <Route path="/stocks/:symbol" element={<ProtectedRoute><StockDetail /></ProtectedRoute>} />
      <Route path="/copilot" element={<ProtectedRoute><AiCopilot /></ProtectedRoute>} />
      <Route path="/news" element={<ProtectedRoute><News /></ProtectedRoute>} /> */}
      {/* <Route path="/analytics" element={<ProtectedRoute><Analytics /></ProtectedRoute>} /> */}
      <Route path="/markets" element={<Markets />} />
      <Route path="/stocks/:symbol" element={<StockDetail />} />
      <Route path="/copilot" element={<AiCopilot />} />
      <Route path="/news" element={<News />} />
      <Route path="/analytics" element={<Analytics />} />
      <Route path="/admin" element={<AdminRoute><Admin /></AdminRoute>} />

      {/* <Route path="*" element={<Navigate to="/login" replace />} /> */}
      <Route path="*" element={<Navigate to="/markets" replace />} />
    </Routes>
  )
}
