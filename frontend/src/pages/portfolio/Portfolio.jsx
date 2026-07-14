import React, { useState } from 'react'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { FiPlus, FiMinus } from 'react-icons/fi'
import DashboardLayout from '../../components/layout/DashboardLayout'
import StatCard from '../../components/dashboard/StatCard'
import Badge from '../../components/dashboard/Badge'
import Modal from '../../components/dashboard/Modal'
import { CardSkeleton, TableSkeleton } from '../../components/dashboard/Skeleton'
import { portfolioApi } from '../../api/portfolio'

export default function Portfolio() {
  const queryClient = useQueryClient()
  const [tradeModal, setTradeModal] = useState(null) // { type: 'BUY'|'SELL', symbol? }
  const [form, setForm] = useState({ symbol: '', quantity: '', price: '' })
  const [error, setError] = useState(null)

  const { data: summary, isLoading } = useQuery({
    queryKey: ['portfolio-summary'], queryFn: () => portfolioApi.summary().then((r) => r.data.data),
  })
  const { data: tradesPage } = useQuery({
    queryKey: ['trades-history'], queryFn: () => portfolioApi.trades(0, 15).then((r) => r.data.data),
  })

  const tradeMutation = useMutation({
    mutationFn: (payload) =>
      tradeModal.type === 'BUY' ? portfolioApi.buy(payload) : portfolioApi.sell(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolio-summary'] })
      queryClient.invalidateQueries({ queryKey: ['trades-history'] })
      setTradeModal(null)
      setForm({ symbol: '', quantity: '', price: '' })
      setError(null)
    },
    onError: (err) => setError(err.response?.data?.message || 'Trade failed'),
  })

  const openTrade = (type, symbol = '') => {
    setForm({ symbol, quantity: '', price: '' })
    setError(null)
    setTradeModal({ type })
  }

  const submitTrade = (e) => {
    e.preventDefault()
    tradeMutation.mutate({
      symbol: form.symbol.toUpperCase(),
      quantity: form.quantity,
      price: form.price || undefined,
    })
  }

  return (
    <DashboardLayout title="Portfolio">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-slate-500">Holdings, trades and performance.</p>
        <div className="flex gap-2">
          <button onClick={() => openTrade('BUY')} className="flex items-center gap-1.5 rounded-xl bg-gain/10 border border-gain/30 text-gain px-4 py-2 text-sm font-medium hover:bg-gain/20 transition-colors">
            <FiPlus /> Buy
          </button>
          <button onClick={() => openTrade('SELL')} className="flex items-center gap-1.5 rounded-xl bg-loss/10 border border-loss/30 text-loss px-4 py-2 text-sm font-medium hover:bg-loss/20 transition-colors">
            <FiMinus /> Sell
          </button>
        </div>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {isLoading ? Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />) : (
          <>
            <StatCard label="Total value" value={fmt(summary?.totalPortfolioValue)} />
            <StatCard label="Total invested" value={fmt(summary?.totalInvested)} />
            <StatCard label="Total P/L" value={fmt(summary?.totalProfitLoss)} delta={Number(summary?.totalRoiPercent || 0)} />
            <StatCard label="ROI" value={Number(summary?.totalRoiPercent || 0).toFixed(2)} prefix="" suffix="%" />
          </>
        )}
      </div>

      <div className="glass-panel p-5 mb-6 overflow-x-auto">
        <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Holdings</p>
        {isLoading ? <TableSkeleton /> : (!summary?.holdings || summary.holdings.length === 0) ? (
          <p className="text-sm text-slate-600">No holdings yet — use Buy above to open a position.</p>
        ) : (
          <table className="w-full text-sm min-w-[720px]">
            <thead>
              <tr className="text-left text-xs text-slate-500 border-b border-ink-700">
                <th className="pb-2 font-medium">Symbol</th>
                <th className="pb-2 font-medium">Qty</th>
                <th className="pb-2 font-medium">Avg price</th>
                <th className="pb-2 font-medium">Current price</th>
                <th className="pb-2 font-medium">Value</th>
                <th className="pb-2 font-medium">P/L</th>
                <th className="pb-2 font-medium">ROI</th>
                <th className="pb-2 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {summary.holdings.map((h) => (
                <tr key={h.holdingId} className="border-b border-ink-700/50 last:border-0">
                  <td className="py-2.5">
                    <p className="font-mono text-slate-200">{h.symbol}</p>
                    <p className="text-xs text-slate-600">{h.companyName}</p>
                  </td>
                  <td className="py-2.5 tabular-nums text-slate-300">{h.quantity}</td>
                  <td className="py-2.5 tabular-nums text-slate-300">{fmt(h.averagePrice)}</td>
                  <td className="py-2.5 tabular-nums text-slate-300">{fmt(h.currentPrice)}</td>
                  <td className="py-2.5 tabular-nums text-slate-200">{fmt(h.currentValue)}</td>
                  <td className={`py-2.5 tabular-nums ${Number(h.profitLoss) >= 0 ? 'text-gain' : 'text-loss'}`}>{fmt(h.profitLoss)}</td>
                  <td className={`py-2.5 tabular-nums ${Number(h.roiPercent) >= 0 ? 'text-gain' : 'text-loss'}`}>{Number(h.roiPercent).toFixed(2)}%</td>
                  <td className="py-2.5 text-right">
                    <button onClick={() => openTrade('SELL', h.symbol)} className="text-xs text-loss hover:underline">Sell</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="glass-panel p-5 overflow-x-auto">
        <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Trade history</p>
        {!tradesPage ? <TableSkeleton /> : tradesPage.content?.length === 0 ? (
          <p className="text-sm text-slate-600">No trades yet.</p>
        ) : (
          <table className="w-full text-sm min-w-[600px]">
            <tbody>
              {tradesPage.content?.map((t) => (
                <tr key={t.id} className="border-b border-ink-700/50 last:border-0">
                  <td className="py-2 font-mono text-slate-300">{t.symbol}</td>
                  <td className="py-2"><Badge variant={t.tradeType === 'BUY' ? 'gain' : 'loss'}>{t.tradeType}</Badge></td>
                  <td className="py-2 tabular-nums text-slate-400">{t.quantity} sh</td>
                  <td className="py-2 tabular-nums text-slate-400">{fmt(t.price)}</td>
                  <td className="py-2 tabular-nums text-slate-400">{fmt(t.totalAmount)}</td>
                  {t.realizedPl !== null && t.realizedPl !== undefined && (
                    <td className={`py-2 tabular-nums ${Number(t.realizedPl) >= 0 ? 'text-gain' : 'text-loss'}`}>
                      {fmt(t.realizedPl)} realized
                    </td>
                  )}
                  <td className="py-2 text-xs text-slate-600">{new Date(t.executedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <Modal open={!!tradeModal} onClose={() => setTradeModal(null)} title={`${tradeModal?.type === 'BUY' ? 'Buy' : 'Sell'} shares`}>
        <form onSubmit={submitTrade}>
          <div className="mb-4">
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Symbol</label>
            <input
              required value={form.symbol}
              onChange={(e) => setForm({ ...form, symbol: e.target.value.toUpperCase() })}
              placeholder="AAPL"
              className="w-full rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20"
            />
          </div>
          <div className="mb-4">
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Quantity</label>
            <input
              required type="number" step="0.0001" min="0.0001" value={form.quantity}
              onChange={(e) => setForm({ ...form, quantity: e.target.value })}
              placeholder="10"
              className="w-full rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20"
            />
          </div>
          <div className="mb-5">
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Price (optional — uses market price if blank)</label>
            <input
              type="number" step="0.01" min="0" value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })}
              placeholder="Market price"
              className="w-full rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20"
            />
          </div>
          {error && <div className="mb-4 rounded-lg bg-loss/10 border border-loss/30 px-3 py-2 text-xs text-loss">{error}</div>}
          <button
            type="submit" disabled={tradeMutation.isPending}
            className={`w-full rounded-xl py-2.5 text-sm font-medium text-white transition-all disabled:opacity-60 ${
              tradeModal?.type === 'BUY' ? 'bg-gradient-to-r from-gain to-signal-cyan' : 'bg-gradient-to-r from-loss to-amber'
            }`}
          >
            {tradeMutation.isPending ? 'Submitting…' : `Confirm ${tradeModal?.type === 'BUY' ? 'buy' : 'sell'}`}
          </button>
        </form>
      </Modal>
    </DashboardLayout>
  )
}

function fmt(value) {
  if (value === undefined || value === null) return '—'
  return Number(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
