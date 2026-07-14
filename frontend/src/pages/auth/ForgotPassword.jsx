import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { FiArrowRight, FiCheckCircle } from 'react-icons/fi'
import AuthLayout from '../../components/auth/AuthLayout'
import FormField from '../../components/auth/FormField'
import { authApi } from '../../api/auth'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [sent, setSent] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await authApi.forgotPassword(email)
      setSent(true)
    } finally {
      setLoading(false)
    }
  }

  if (sent) {
    return (
      <AuthLayout eyebrow="Check your inbox" title="Reset link sent">
        <div className="flex flex-col items-center text-center py-4">
          <FiCheckCircle className="text-4xl text-gain mb-3" />
          <p className="text-sm text-slate-400 mb-6">
            If an account exists for <span className="text-slate-200">{email}</span>, a reset link
            is on its way. It expires in 30 minutes.
          </p>
          <Link to="/login" className="text-sm text-signal-blue hover:text-signal-cyan transition-colors font-medium">
            Back to sign in
          </Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      eyebrow="Reset password"
      title="Forgot your password?"
      subtitle="Enter the email on your account and we'll send you a reset link."
    >
      <form onSubmit={handleSubmit} noValidate>
        <FormField
          label="Email"
          type="email"
          placeholder="you@firm.com"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <motion.button
          whileTap={{ scale: 0.98 }}
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet
                     py-2.5 text-sm font-medium text-white shadow-glow hover:brightness-110 transition-all
                     disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {loading ? 'Sending…' : 'Send reset link'}
          {!loading && <FiArrowRight className="text-base" />}
        </motion.button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Remembered it?{' '}
        <Link to="/login" className="text-signal-blue hover:text-signal-cyan transition-colors font-medium">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
