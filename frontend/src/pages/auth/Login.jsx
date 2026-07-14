import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { FiArrowRight } from 'react-icons/fi'
import AuthLayout from '../../components/auth/AuthLayout'
import FormField from '../../components/auth/FormField'
import { useAuth } from '../../context/AuthContext'

export default function Login() {
  const { login, loading, error } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await login(form.email, form.password)
      navigate('/dashboard')
    } catch {
      // error already surfaced via context
    }
  }

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Sign in to your desk"
      subtitle="Real-time portfolio, AI insights and market data in one terminal."
    >
      <form onSubmit={handleSubmit} noValidate>
        <FormField
          label="Email"
          type="email"
          placeholder="you@firm.com"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />
        <FormField
          label="Password"
          type="password"
          placeholder="••••••••"
          required
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />

        <div className="flex justify-end mb-5 -mt-2">
          <Link to="/forgot-password" className="text-xs text-signal-blue hover:text-signal-cyan transition-colors">
            Forgot password?
          </Link>
        </div>

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
          {loading ? 'Signing in…' : 'Sign in'}
          {!loading && <FiArrowRight className="text-base" />}
        </motion.button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Don't have an account?{' '}
        <Link to="/register" className="text-signal-blue hover:text-signal-cyan transition-colors font-medium">
          Create one
        </Link>
      </p>
    </AuthLayout>
  )
}
