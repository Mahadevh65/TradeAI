import React from 'react'

const VARIANTS = {
  gain: 'bg-gain/10 text-gain border-gain/30',
  loss: 'bg-loss/10 text-loss border-loss/30',
  neutral: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
  amber: 'bg-amber/10 text-amber border-amber/30',
  violet: 'bg-signal-violet/10 text-signal-violet border-signal-violet/30',
}

export default function Badge({ children, variant = 'neutral' }) {
  return (
    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${VARIANTS[variant]}`}>
      {children}
    </span>
  )
}
