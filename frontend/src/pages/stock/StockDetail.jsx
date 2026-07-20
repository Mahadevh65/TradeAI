import React from 'react'
// import { useParams } from 'react-router-dom'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ComposedChart, Bar, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Badge from '../../components/dashboard/Badge'
import { CardSkeleton } from '../../components/dashboard/Skeleton'
import { stockApi } from '../../api/stock'
import { useAuth } from '../../context/AuthContext'

export default function StockDetail() {
  const { symbol } = useParams()

  const navigate = useNavigate()

  const { user } = useAuth()

  const requireLogin = () => {

    if (!user) {

      const ok = window.confirm(
        "Please login to continue.\n\nTrading and Watchlist features require an account."
      )

      if (ok) {
        navigate("/login")
      }

      return false
    }

    return true
  }

  const handleBuy = () => {

    if (!requireLogin()) return

    alert("Buy feature coming soon!")

  }

  const handleSell = () => {

    if (!requireLogin()) return

    alert("Sell feature coming soon!")

  }

  const handleWatchlist = () => {

    if (!requireLogin()) return

    alert("Added to Watchlist!")

  }

  const { data: stock, isLoading } = useQuery({
    queryKey: ['stock-details', symbol], queryFn: () => stockApi.getDetails(symbol).then((r) => r.data.data),
  })
  const { data: history } = useQuery({
    queryKey: ['stock-history', symbol], queryFn: () => stockApi.getHistory(symbol, 90).then((r) => r.data.data),
  })

  const chartData = (history || []).map((p) => ({
    ...p,
    range: [Number(p.low), Number(p.high)],
    bullish: Number(p.close) >= Number(p.open),
  }))

  return (
    <DashboardLayout title={symbol}>
      {isLoading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4"><CardSkeleton /><CardSkeleton /><CardSkeleton /><CardSkeleton /></div>
      ) : (
        <>
          <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
            <div>
              <h2 className="font-display text-xl font-semibold text-slate-100">{stock?.companyName}</h2>
              <p className="text-sm text-slate-500">{stock?.exchange} · {stock?.sector}</p>
            </div>
            <div className="text-right">
              <p className="font-display text-2xl font-semibold text-slate-50 tabular-nums">{fmt(stock?.lastPrice)}</p>
              <Badge variant={Number(stock?.dayChangePct) >= 0 ? 'gain' : 'loss'}>
                {Number(stock?.dayChangePct || 0).toFixed(2)}% today
              </Badge>
            </div>
          </div>

          <div className="glass-panel p-5 mb-6">
            <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Price history (90d)</p>
            <ResponsiveContainer width="100%" height={320}>
              <ComposedChart data={chartData}>
                <CartesianGrid stroke="#1B2233" strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="date" tick={{ fill: '#64748B', fontSize: 10 }} axisLine={false} tickLine={false} minTickGap={30} />
                <YAxis domain={['auto', 'auto']} tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={60} />
                <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
                <Bar dataKey="range" fill="#2A3348" barSize={3} />
                <Line type="monotone" dataKey="close" stroke="#4C6FFF" strokeWidth={2} dot={false} />
              </ComposedChart>
            </ResponsiveContainer>
            <p className="text-[11px] text-slate-600 mt-2">
              Chart shown at daily resolution; on free-tier data this may be a modeled series anchored to the current price rather than certified historical data.
            </p>
          </div>

          <div className="flex flex-wrap gap-3 mb-6">

            <button
              onClick={handleBuy}
              className="px-6 py-3 rounded-xl bg-green-600 hover:bg-green-700 text-white font-semibold transition"
            >
              Buy
            </button>

            <button
              onClick={handleSell}
              className="px-6 py-3 rounded-xl bg-red-600 hover:bg-red-700 text-white font-semibold transition"
            >
              Sell
            </button>

            <button
              onClick={handleWatchlist}
              className="px-6 py-3 rounded-xl border border-signal-blue text-signal-blue hover:bg-signal-blue/10 transition"
            >
              Add to Watchlist
            </button>

          </div>


          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {/* <Stat label="Market cap" value={fmt(stock?.marketCap)} />
            <Stat label="P/E ratio" value={stock?.peRatio ?? '—'} />
            <Stat label="EPS" value={stock?.eps ?? '—'} />
            <Stat label="Dividend yield" value={stock?.dividendYield ? `${stock.dividendYield}%` : '—'} />
            <Stat label="52w high" value={fmt(stock?.week52High)} />
            <Stat label="52w low" value={fmt(stock?.week52Low)} />
            <Stat label="Volume" value={stock?.volume?.toLocaleString() ?? '—'} />
            <Stat label="Currency" value={stock?.currency || 'USD'} /> */}

            <Stat label="Exchange" value={stock?.exchange ?? '—'} />
            <Stat label="Country" value={stock?.country ?? '—'} />
            <Stat label="Currency" value={stock?.currency ?? '—'} />
            <Stat label="Type" value={stock?.type ?? '—'} />
            <Stat label="Volume" value={stock?.volume?.toLocaleString() ?? '—'} />
            <Stat label="Sector" value={stock?.sector ?? '—'} />
            <Stat label="Company" value={stock?.companyName ?? '—'} />
            <Stat label="Symbol" value={stock?.symbol ?? '—'} />
          </div>
        </>
      )}
    </DashboardLayout>
  )
}

function Stat({ label, value }) {
  return (
    <div className="glass-panel p-4">
      <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-1.5">{label}</p>
      <p className="text-sm font-medium text-slate-200 tabular-nums">{value}</p>
    </div>
  )
}

function fmt(value) {
  if (value === undefined || value === null) return '—'
  return Number(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
