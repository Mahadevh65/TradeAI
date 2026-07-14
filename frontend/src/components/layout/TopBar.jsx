import React, { useState } from 'react'
import { FiBell } from 'react-icons/fi'
import { motion, AnimatePresence } from 'framer-motion'
import CommandPalette from './CommandPalette'

export default function TopBar({ title }) {
  const [notifOpen, setNotifOpen] = useState(false)

  const notifications = [
    { id: 1, text: 'AAPL crossed your alert price of $220', time: '2m ago' },
    { id: 2, text: 'Your TSLA position is up 4.2% today', time: '1h ago' },
  ]

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between gap-4 border-b border-ink-700/80 bg-ink-950/70 backdrop-blur-xl px-4 md:px-6 py-3.5">
      <h1 className="font-display text-lg font-semibold text-slate-100">{title}</h1>

      <div className="flex items-center gap-3">
        <CommandPalette />

        <div className="relative">
          <button
            onClick={() => setNotifOpen((o) => !o)}
            className="relative rounded-xl border border-ink-700 bg-ink-900/60 p-2 text-slate-400 hover:text-slate-200 hover:border-ink-600 transition-colors"
          >
            <FiBell className="text-base" />
            <span className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-signal-violet text-[9px] font-semibold text-white flex items-center justify-center">
              {notifications.length}
            </span>
          </button>

          <AnimatePresence>
            {notifOpen && (
              <motion.div
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.15 }}
                className="absolute right-0 mt-2 w-72 glass-panel p-2 z-40"
              >
                <p className="px-2 py-1.5 text-xs font-mono uppercase tracking-widest text-slate-500">
                  Notifications
                </p>
                {notifications.map((n) => (
                  <div key={n.id} className="px-2 py-2 rounded-lg hover:bg-ink-800/60 transition-colors">
                    <p className="text-sm text-slate-300">{n.text}</p>
                    <p className="text-xs text-slate-600 mt-0.5">{n.time}</p>
                  </div>
                ))}
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </header>
  )
}
