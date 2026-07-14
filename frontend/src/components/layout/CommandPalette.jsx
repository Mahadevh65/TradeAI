import React, { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { FiSearch, FiCommand } from 'react-icons/fi'
import { stockApi } from '../../api/stock'

const QUICK_ACTIONS = [
  { label: 'Go to Dashboard', path: '/dashboard' },
  { label: 'Go to Portfolio', path: '/portfolio' },
  { label: 'Go to Watchlist', path: '/watchlist' },
  { label: 'Go to Markets', path: '/markets' },
  { label: 'Ask AI Copilot', path: '/copilot' },
  { label: 'View News', path: '/news' },
  { label: 'View Analytics', path: '/analytics' },
]

export default function CommandPalette() {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const navigate = useNavigate()

  useEffect(() => {
    const handler = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        setOpen((o) => !o)
      }
      if (e.key === 'Escape') setOpen(false)
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [])

  useEffect(() => {
    if (!query || query.length < 1) {
      setResults([])
      return
    }
    const timeout = setTimeout(() => {
      stockApi.search(query).then(({ data }) => setResults(data.data || [])).catch(() => setResults([]))
    }, 250)
    return () => clearTimeout(timeout)
  }, [query])

  const go = useCallback((path) => {
    setOpen(false)
    setQuery('')
    navigate(path)
  }, [navigate])

  const filteredActions = QUICK_ACTIONS.filter((a) =>
    a.label.toLowerCase().includes(query.toLowerCase())
  )

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className="hidden md:flex items-center gap-2 rounded-xl border border-ink-700 bg-ink-900/60 px-3 py-1.5 text-xs text-slate-500 hover:border-ink-600 transition-colors"
      >
        <FiSearch className="text-sm" />
        Search stocks, pages…
        <span className="ml-4 flex items-center gap-0.5 rounded border border-ink-700 px-1.5 py-0.5 text-[10px] text-slate-600">
          <FiCommand className="text-[10px]" />K
        </span>
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-start justify-center pt-[15vh] px-4"
            onClick={() => setOpen(false)}
          >
            <motion.div
              initial={{ opacity: 0, y: -12, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -12, scale: 0.98 }}
              transition={{ duration: 0.15 }}
              onClick={(e) => e.stopPropagation()}
              className="w-full max-w-lg glass-panel overflow-hidden"
            >
              <div className="flex items-center gap-2.5 px-4 py-3 border-b border-ink-700">
                <FiSearch className="text-slate-500" />
                <input
                  autoFocus
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Search symbols or jump to a page…"
                  className="flex-1 bg-transparent text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none"
                />
              </div>

              <div className="max-h-80 overflow-y-auto py-2">
                {results.length > 0 && (
                  <>
                    <p className="px-4 py-1 text-[10px] font-mono uppercase tracking-widest text-slate-600">Stocks</p>
                    {results.map((r) => (
                      <button
                        key={r.id}
                        onClick={() => go(`/stocks/${r.symbol}`)}
                        className="w-full flex items-center justify-between px-4 py-2 text-sm text-slate-300 hover:bg-ink-800/60 transition-colors text-left"
                      >
                        <span>
                          <span className="font-mono text-signal-cyan mr-2">{r.symbol}</span>
                          {r.companyName}
                        </span>
                      </button>
                    ))}
                  </>
                )}

                {filteredActions.length > 0 && (
                  <>
                    <p className="px-4 py-1 mt-1 text-[10px] font-mono uppercase tracking-widest text-slate-600">Navigate</p>
                    {filteredActions.map((a) => (
                      <button
                        key={a.path}
                        onClick={() => go(a.path)}
                        className="w-full flex items-center px-4 py-2 text-sm text-slate-300 hover:bg-ink-800/60 transition-colors text-left"
                      >
                        {a.label}
                      </button>
                    ))}
                  </>
                )}

                {results.length === 0 && filteredActions.length === 0 && (
                  <p className="px-4 py-6 text-center text-sm text-slate-600">No matches</p>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}
