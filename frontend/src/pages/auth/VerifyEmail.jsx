import React, { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { FiCheckCircle, FiAlertTriangle, FiLoader } from 'react-icons/fi'
import AuthLayout from '../../components/auth/AuthLayout'
import { authApi } from '../../api/auth'

export default function VerifyEmail() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [status, setStatus] = useState('loading') // loading | success | error

  useEffect(() => {
    if (!token) {
      setStatus('error')
      return
    }
    authApi
      .verifyEmail(token)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'))
  }, [token])

  return (
    <AuthLayout eyebrow="Email verification" title="Verifying your account">
      <div className="flex flex-col items-center text-center py-4">
        {status === 'loading' && (
          <>
            <FiLoader className="text-4xl text-signal-blue mb-3 animate-spin" />
            <p className="text-sm text-slate-400">One moment…</p>
          </>
        )}
        {status === 'success' && (
          <>
            <FiCheckCircle className="text-4xl text-gain mb-3" />
            <p className="text-sm text-slate-400 mb-6">Your email is verified. You're ready to sign in.</p>
            <Link to="/login" className="text-sm text-signal-blue hover:text-signal-cyan transition-colors font-medium">
              Continue to sign in
            </Link>
          </>
        )}
        {status === 'error' && (
          <>
            <FiAlertTriangle className="text-4xl text-loss mb-3" />
            <p className="text-sm text-slate-400 mb-6">
              This verification link is invalid or has expired.
            </p>
            <Link to="/register" className="text-sm text-signal-blue hover:text-signal-cyan transition-colors font-medium">
              Back to registration
            </Link>
          </>
        )}
      </div>
    </AuthLayout>
  )
}
