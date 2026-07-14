import React from 'react'
import { motion } from 'framer-motion'

/**
 * Signature element: an animated "signal line" — a stylised candlestick +
 * trend line drifting behind the glass auth card. It's the one bold visual
 * move on an otherwise disciplined, quiet screen.
 */
function SignalBackdrop() {
  const candles = [
    { x: 20, h: 40, up: true }, { x: 50, h: 65, up: true }, { x: 80, h: 30, up: false },
    { x: 110, h: 80, up: true }, { x: 140, h: 45, up: false }, { x: 170, h: 90, up: true },
    { x: 200, h: 55, up: true }, { x: 230, h: 70, up: false }, { x: 260, h: 100, up: true },
    { x: 290, h: 60, up: true }, { x: 320, h: 85, up: false }, { x: 350, h: 120, up: true },
  ]

  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      <svg
        viewBox="0 0 400 300"
        className="absolute -right-24 top-1/2 -translate-y-1/2 w-[640px] opacity-[0.18]"
        fill="none"
      >
        {candles.map((c, i) => (
          <motion.rect
            key={i}
            x={c.x}
            width={14}
            rx={2}
            fill={c.up ? '#22D3A6' : '#FB5A5A'}
            initial={{ height: 0, y: 260 }}
            animate={{ height: c.h, y: 260 - c.h }}
            transition={{ duration: 1.1, delay: i * 0.06, ease: 'easeOut' }}
          />
        ))}
        <motion.path
          d="M20,220 C80,180 140,140 200,150 C260,160 320,60 380,40"
          stroke="url(#signalGrad)"
          strokeWidth="2.5"
          initial={{ pathLength: 0 }}
          animate={{ pathLength: 1 }}
          transition={{ duration: 2, delay: 0.8, ease: 'easeInOut' }}
        />
        <defs>
          <linearGradient id="signalGrad" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="#4C6FFF" />
            <stop offset="55%" stopColor="#8B5CF6" />
            <stop offset="100%" stopColor="#39D0D8" />
          </linearGradient>
        </defs>
      </svg>
    </div>
  )
}

export default function AuthLayout({ eyebrow, title, subtitle, children }) {
  return (
    <div className="relative min-h-screen w-full flex items-center justify-center overflow-hidden px-4">
      <SignalBackdrop />

      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        className="relative z-10 w-full max-w-md"
      >
        <div className="mb-8 flex items-center gap-2.5">
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-signal-blue to-signal-violet shadow-glow flex items-center justify-center">
            <span className="font-display font-bold text-sm text-white">T</span>
          </div>
          <span className="font-display font-semibold text-lg tracking-tight text-slate-100">
            TradeMind <span className="signal-gradient-text">AI</span>
          </span>
        </div>

        <div className="glass-panel p-8">
          {eyebrow && (
            <p className="text-xs font-mono uppercase tracking-widest text-signal-cyan mb-2">
              {eyebrow}
            </p>
          )}
          <h1 className="font-display text-2xl font-semibold text-slate-50 mb-1.5">{title}</h1>
          {subtitle && <p className="text-sm text-slate-400 mb-6">{subtitle}</p>}
          {children}
        </div>
      </motion.div>
    </div>
  )
}
