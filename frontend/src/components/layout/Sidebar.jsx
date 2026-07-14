import React from 'react'
import { NavLink } from 'react-router-dom'
import {
  FiGrid, FiPieChart, FiStar, FiTrendingUp, FiCpu,
  FiFileText, FiBarChart2, FiShield, FiLogOut,
} from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Overview', icon: FiGrid },
  { to: '/portfolio', label: 'Portfolio', icon: FiPieChart },
  { to: '/watchlist', label: 'Watchlist', icon: FiStar },
  { to: '/markets', label: 'Markets', icon: FiTrendingUp },
  { to: '/copilot', label: 'AI Copilot', icon: FiCpu },
  { to: '/news', label: 'News', icon: FiFileText },
  { to: '/analytics', label: 'Analytics', icon: FiBarChart2 },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const isAdmin = user?.roles?.includes('ADMIN')

  return (
    <aside className="hidden md:flex flex-col w-60 shrink-0 border-r border-ink-700/80 bg-ink-900/40 backdrop-blur-xl h-screen sticky top-0">
      <div className="px-5 py-5 flex items-center gap-2.5">
        <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-signal-blue to-signal-violet shadow-glow flex items-center justify-center">
          <span className="font-display font-bold text-sm text-white">T</span>
        </div>
        <span className="font-display font-semibold text-base tracking-tight text-slate-100">
          TradeMind <span className="signal-gradient-text">AI</span>
        </span>
      </div>

      <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-signal-blue/10 text-signal-blue border border-signal-blue/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-ink-800/60 border border-transparent'
              }`
            }
          >
            <Icon className="text-base shrink-0" />
            {label}
          </NavLink>
        ))}

        {isAdmin && (
          <NavLink
            to="/admin"
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-amber/10 text-amber border border-amber/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-ink-800/60 border border-transparent'
              }`
            }
          >
            <FiShield className="text-base shrink-0" />
            Admin
          </NavLink>
        )}
      </nav>

      <div className="px-3 py-4 border-t border-ink-700/80">
        <div className="flex items-center gap-2.5 px-2 mb-3">
          <div className="h-8 w-8 rounded-full bg-gradient-to-br from-signal-violet to-signal-cyan flex items-center justify-center text-xs font-semibold text-white">
            {(user?.fullName || 'T')[0].toUpperCase()}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-medium text-slate-200 truncate">{user?.fullName}</p>
            <p className="text-xs text-slate-500 truncate">{user?.roles?.[0]?.toLowerCase()}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="w-full flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-slate-400 hover:text-loss hover:bg-loss/10 transition-colors"
        >
          <FiLogOut /> Sign out
        </button>
      </div>
    </aside>
  )
}
