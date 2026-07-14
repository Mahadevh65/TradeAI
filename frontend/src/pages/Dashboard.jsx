import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  AreaChart, Area, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts'
import { FiCpu, FiClock } from 'react-icons/fi'
import DashboardLayout from '../components/layout/DashboardLayout'
import StatCard from '../components/dashboard/StatCard'
import Badge from '../components/dashboard/Badge'
import { CardSkeleton, TableSkeleton } from '../components/dashboard/Skeleton'
import { portfolioApi } from '../api/portfolio'
import { stockApi } from '../api/stock'
import { watchlistApi } from '../api/watchlist'
import { newsApi } from '../api/news'
import { analyticsApi } from '../api/analytics'

const PIE_COLORS = ['#4C6FFF', '#8B5CF6', '#39D0D8', '#22D3A6', '#F5B94D', '#FB5A5A']

function isMarketHoursNow() {
  const hour = new Date().getUTCHours()
  return hour >= 13 && hour < 21 // rough NASDAQ hours in UTC
}

export default function Dashboard() {
  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['portfolio-summary'], queryFn: () => portfolioApi.summary().then((r) => r.data.data),
  })
  const { data: history } = useQuery({
    queryKey: ['portfolio-history'], queryFn: () => portfolioApi.history(90).then((r) => r.data.data),
  })
  const { data: sectors } = useQuery({
    queryKey: ['sector-allocation'], queryFn: () => analyticsApi.sectorAllocation().then((r) => r.data.data),
  })
  const { data: gainers } = useQuery({
    queryKey: ['gainers'], queryFn: () => stockApi.gainers().then((r) => r.data.data),
  })
  const { data: losers } = useQuery({
    queryKey: ['losers'], queryFn: () => stockApi.losers().then((r) => r.data.data),
  })
  const { data: watchlists } = useQuery({
    queryKey: ['watchlists'], queryFn: () => watchlistApi.getAll().then((r) => r.data.data),
  })
  const { data: news } = useQuery({
    queryKey: ['news-preview'], queryFn: () => newsApi.getLatest().then((r) => r.data.data),
  })
  const { data: trades } = useQuery({
    queryKey: ['recent-trades'], queryFn: () => portfolioApi.trades(0, 5).then((r) => r.data.data.content),
  })

  const defaultWatchlist = watchlists?.[0]
  const marketOpen = isMarketHoursNow()

  return (
    <DashboardLayout title="Overview">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-slate-500">Here's how your desk looks today.</p>
        <Badge variant={marketOpen ? 'gain' : 'neutral'}>
          <span className={`mr-1.5 inline-block h-1.5 w-1.5 rounded-full ${marketOpen ? 'bg-gain animate-pulse' : 'bg-slate-500'}`} />
          {marketOpen ? 'Market open' : 'Market closed'}
        </Badge>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {summaryLoading ? (
          Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)
        ) : (
          <>
            <StatCard label="Portfolio value" value={fmt(summary?.totalPortfolioValue)} />
            <StatCard label="Today's P/L" value={fmt(summary?.todayProfitLoss)} delta={pctOf(summary?.todayProfitLoss, summary?.totalInvested)} />
            <StatCard label="Weekly P/L" value={fmt(summary?.weeklyProfitLoss)} delta={pctOf(summary?.weeklyProfitLoss, summary?.totalInvested)} />
            <StatCard label="Monthly P/L" value={fmt(summary?.monthlyProfitLoss)} delta={pctOf(summary?.monthlyProfitLoss, summary?.totalInvested)} />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
        {/* Performance line chart */}
        <div className="lg:col-span-2 glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Performance</p>
          <ResponsiveContainer width="100%" height={260}>
            <AreaChart data={history || []}>
              <defs>
                <linearGradient id="perfGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#4C6FFF" stopOpacity={0.35} />
                  <stop offset="100%" stopColor="#4C6FFF" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="#1B2233" strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={60} />
              <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
              <Area type="monotone" dataKey="totalValue" stroke="#4C6FFF" fill="url(#perfGradient)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
          {(!history || history.length === 0) && (
            <p className="text-center text-xs text-slate-600 -mt-32">
              No history yet — snapshots build up as you trade day over day.
            </p>
          )}
        </div>

        {/* Allocation pie */}
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Sector allocation</p>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={sectors || []} dataKey="percentOfPortfolio" nameKey="sector" innerRadius={50} outerRadius={80} paddingAngle={2}>
                {(sectors || []).map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
              </Pie>
              <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
            </PieChart>
          </ResponsiveContainer>
          <div className="mt-3 space-y-1.5">
            {(sectors || []).slice(0, 4).map((s, i) => (
              <div key={s.sector} className="flex items-center justify-between text-xs">
                <span className="flex items-center gap-1.5 text-slate-400">
                  <span className="h-2 w-2 rounded-full" style={{ background: PIE_COLORS[i % PIE_COLORS.length] }} />
                  {s.sector}
                </span>
                <span className="text-slate-300 tabular-nums">{Number(s.percentOfPortfolio).toFixed(1)}%</span>
              </div>
            ))}
            {(!sectors || sectors.length === 0) && <p className="text-xs text-slate-600">No holdings yet</p>}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
        {/* Top gainers/losers */}
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-3">Top gainers</p>
          <MoverList items={gainers} positive />
        </div>
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-3">Top losers</p>
          <MoverList items={losers} positive={false} />
        </div>

        {/* AI Insights teaser */}
        <Link to="/copilot" className="glass-panel p-5 flex flex-col justify-between hover:border-signal-violet/40 transition-colors border border-transparent">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <FiCpu className="text-signal-violet" />
              <p className="text-xs font-mono uppercase tracking-widest text-slate-500">AI insights</p>
            </div>
            <p className="text-sm text-slate-300">
              Ask the copilot anything — "Should I buy TCS?" or "Summarize today's market."
            </p>
          </div>
          <span className="mt-4 text-sm font-medium text-signal-blue">Open AI Copilot →</span>
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Watchlist preview */}
        <div className="glass-panel p-5">
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs font-mono uppercase tracking-widest text-slate-500">
              {defaultWatchlist?.name || 'Watchlist'}
            </p>
            <Link to="/watchlist" className="text-xs text-signal-blue hover:text-signal-cyan transition-colors">View all</Link>
          </div>
          {!defaultWatchlist ? (
            <p className="text-xs text-slate-600">No watchlist yet — create one from the Watchlist page.</p>
          ) : (
            <div className="space-y-2">
              {(defaultWatchlist.items || []).slice(0, 5).map((item) => (
                <div key={item.itemId} className="flex items-center justify-between text-sm">
                  <span className="font-mono text-slate-300">{item.symbol}</span>
                  <span className="tabular-nums text-slate-400">{fmt(item.lastPrice)}</span>
                  <span className={`tabular-nums text-xs ${Number(item.dayChangePct) >= 0 ? 'text-gain' : 'text-loss'}`}>
                    {Number(item.dayChangePct || 0).toFixed(2)}%
                  </span>
                </div>
              ))}
              {defaultWatchlist.items?.length === 0 && <p className="text-xs text-slate-600">Nothing added yet</p>}
            </div>
          )}
        </div>

        {/* Market news preview */}
        <div className="glass-panel p-5">
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs font-mono uppercase tracking-widest text-slate-500">Market news</p>
            <Link to="/news" className="text-xs text-signal-blue hover:text-signal-cyan transition-colors">View all</Link>
          </div>
          <div className="space-y-3">
            {(news || []).slice(0, 4).map((n) => (
              <a key={n.id} href={n.url} target="_blank" rel="noreferrer" className="block group">
                <p className="text-sm text-slate-300 group-hover:text-slate-100 transition-colors line-clamp-2">{n.title}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-xs text-slate-600">{n.source}</span>
                  <Badge variant={n.sentiment === 'BULLISH' ? 'gain' : n.sentiment === 'BEARISH' ? 'loss' : 'neutral'}>
                    {n.sentiment?.toLowerCase()}
                  </Badge>
                </div>
              </a>
            ))}
            {(!news || news.length === 0) && (
              <p className="text-xs text-slate-600">No news cached yet — configure NEWS_API_KEY to populate this feed.</p>
            )}
          </div>
        </div>
      </div>

      {/* Recent transactions */}
      <div className="glass-panel p-5 mt-4">
        <div className="flex items-center justify-between mb-3">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500">Recent transactions</p>
          <Link to="/portfolio" className="text-xs text-signal-blue hover:text-signal-cyan transition-colors">View all</Link>
        </div>
        {!trades ? <TableSkeleton rows={3} /> : trades.length === 0 ? (
          <p className="text-xs text-slate-600">No trades yet.</p>
        ) : (
          <table className="w-full text-sm">
            <tbody>
              {trades.map((t) => (
                <tr key={t.id} className="border-t border-ink-700/60 first:border-0">
                  <td className="py-2 font-mono text-slate-300">{t.symbol}</td>
                  <td className="py-2">
                    <Badge variant={t.tradeType === 'BUY' ? 'gain' : 'loss'}>{t.tradeType}</Badge>
                  </td>
                  <td className="py-2 text-slate-400 tabular-nums">{t.quantity} sh</td>
                  <td className="py-2 text-slate-400 tabular-nums">{fmt(t.price)}</td>
                  <td className="py-2 text-slate-600 text-xs flex items-center gap-1">
                    <FiClock className="text-[10px]" /> {new Date(t.executedAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </DashboardLayout>
  )
}

function MoverList({ items, positive }) {
  if (!items || items.length === 0) return <p className="text-xs text-slate-600">No data cached yet</p>
  return (
    <div className="space-y-2">
      {items.slice(0, 5).map((s) => (
        <Link key={s.id} to={`/stocks/${s.symbol}`} className="flex items-center justify-between text-sm hover:opacity-80 transition-opacity">
          <span className="font-mono text-slate-300">{s.symbol}</span>
          <span className={`tabular-nums text-xs font-medium ${positive ? 'text-gain' : 'text-loss'}`}>
            {Number(s.dayChangePct || 0).toFixed(2)}%
          </span>
        </Link>
      ))}
    </div>
  )
}

function fmt(value) {
  if (value === undefined || value === null) return '—'
  return Number(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function pctOf(part, whole) {
  if (!whole || Number(whole) === 0) return 0
  return (Number(part) / Number(whole)) * 100
}
