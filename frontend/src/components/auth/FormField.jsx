import React from 'react'

export default function FormField({ label, error, ...props }) {
  return (
    <div className="mb-4">
      <label className="block text-xs font-medium text-slate-400 mb-1.5">{label}</label>
      <input
        className={`w-full rounded-xl bg-ink-900/80 border ${
          error ? 'border-loss/60' : 'border-ink-700'
        } px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600
           focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20
           transition-colors`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-loss">{error}</p>}
    </div>
  )
}
