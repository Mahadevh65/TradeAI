import React, { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { FiArrowRight, FiCheckCircle, FiAlertTriangle } from 'react-icons/fi'
import AuthLayout from '../../components/auth/AuthLayout'
import FormField from '../../components/auth/FormField'
import { authApi } from '../../api/auth'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const [newPassword, setNewPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [done, setDone] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await authApi.resetPassword({ token, newPassword })
      setDone(true)
      setTimeout(() => navigate('/login'), 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reset password')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <AuthLayout eyebrow="Invalid link" title="Missing reset token">
        <div className="flex flex-col items-center text-center py-4">
          <FiAlertTriangle className="text-4xl text-amber mb-3" />
          <p className="text-sm text-slate-400 mb-6">
            This reset link looks broken. Request a new one from the forgot password page.
          </p>
          <Link to="/forgot-password" className="text-sm text-signal-blue hover:text-signal-cyan transition-colors font-medium">
            Request new link
          </Link>
        </div>
      </AuthLayout>
    )
  }

  if (done) {
    return (
      <AuthLayout eyebrow="Success" title="Password updated">
        <div className="flex flex-col items-center text-center py-4">
          <FiCheckCircle className="text-4xl text-gain mb-3" />
          <p className="text-sm text-slate-400">Redirecting you to sign in…</p>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout eyebrow="Reset password" title="Set a new password">
      <form onSubmit={handleSubmit} noValidate>
        <FormField
          label="New password"
          type="password"
          placeholder="••••••••"
          required
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <p className="-mt-3 mb-4 text-xs text-slate-600">
          At least 8 characters, with uppercase, lowercase, a number and a symbol.
        </p>

        {error && (
          <div className="mb-4 rounded-lg bg-loss/10 border border-loss/30 px-3 py-2 text-xs text-loss">
            {error}
          </div>
        )}

        <motion.button
          whileTap={{ scale: 0.98 }}
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet
                     py-2.5 text-sm font-medium text-white shadow-glow hover:brightness-110 transition-all
                     disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {loading ? 'Updating…' : 'Update password'}
          {!loading && <FiArrowRight className="text-base" />}
        </motion.button>
      </form>
    </AuthLayout>
  )
}
