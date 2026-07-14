import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { FiArrowRight, FiCheckCircle } from 'react-icons/fi'
import AuthLayout from '../../components/auth/AuthLayout'
import FormField from '../../components/auth/FormField'
import { useAuth } from '../../context/AuthContext'

const PASSWORD_HINT = 'At least 8 characters, with uppercase, lowercase, a number and a symbol.'

export default function Register() {
  const { register, loading, error } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    requestedRole: 'TRADER',
  })
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await register(form)
      setSubmitted(true)
    } catch {
      // error already surfaced via context
    }
  }

  if (submitted) {
    return (
      <AuthLayout eyebrow="Almost there" title="Check your inbox">
        <div className="flex flex-col items-center text-center py-4">
          <FiCheckCircle className="text-4xl text-gain mb-3" />
          <p className="text-sm text-slate-400 mb-6">
            We sent a verification link to <span className="text-slate-200">{form.email}</span>.
            Verify your email to activate your desk.
          </p>
          <Link
            to="/login"
            className="text-sm text-signal-blue hover:text-signal-cyan transition-colors font-medium"
          >
            Back to sign in
          </Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      eyebrow="Get started"
      title="Open your desk"
      subtitle="Portfolio tracking, live market data and an AI copilot — free to start."
    >
      <form onSubmit={handleSubmit} noValidate>
        <FormField
          label="Full name"
          type="text"
          placeholder="Jordan Lee"
          required
          value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
        />
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
        <p className="-mt-3 mb-4 text-xs text-slate-600">{PASSWORD_HINT}</p>

        <div className="mb-5">
          <label className="block text-xs font-medium text-slate-400 mb-1.5">Account type</label>
          <div className="grid grid-cols-2 gap-2">
            {['TRADER', 'ANALYST'].map((role) => (
              <button
                type="button"
                key={role}
                onClick={() => setForm({ ...form, requestedRole: role })}
                className={`rounded-xl border py-2 text-sm font-medium transition-colors ${
                  form.requestedRole === role
                    ? 'border-signal-blue/70 bg-signal-blue/10 text-signal-blue'
                    : 'border-ink-700 text-slate-400 hover:border-ink-600'
                }`}
              >
                {role === 'TRADER' ? 'Trader' : 'Analyst'}
              </button>
            ))}
          </div>
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
          {loading ? 'Creating account…' : 'Create account'}
          {!loading && <FiArrowRight className="text-base" />}
        </motion.button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Already have an account?{' '}
        <Link to="/login" className="text-signal-blue hover:text-signal-cyan transition-colors font-medium">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
