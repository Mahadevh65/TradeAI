import React, { useState } from 'react'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { FiPlus, FiTrash2, FiBell, FiX } from 'react-icons/fi'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Modal from '../../components/dashboard/Modal'
import Badge from '../../components/dashboard/Badge'
import { TableSkeleton } from '../../components/dashboard/Skeleton'
import { watchlistApi } from '../../api/watchlist'

export default function Watchlist() {
  const queryClient = useQueryClient()
  const [activeId, setActiveId] = useState(null)
  const [newListModal, setNewListModal] = useState(false)
  const [addStockModal, setAddStockModal] = useState(false)
  const [alertModal, setAlertModal] = useState(false)
  const [listName, setListName] = useState('')
  const [symbolInput, setSymbolInput] = useState('')
  const [alertForm, setAlertForm] = useState({ symbol: '', condition: 'ABOVE', targetPrice: '' })
  const [sort, setSort] = useState({ by: 'symbol', dir: 'asc' })

  const { data: watchlists, isLoading } = useQuery({
    queryKey: ['watchlists-full'], queryFn: () => watchlistApi.getAll().then((r) => r.data.data),
  })
  const { data: alerts } = useQuery({
    queryKey: ['price-alerts'], queryFn: () => watchlistApi.getAlerts().then((r) => r.data.data),
  })

  const active = watchlists?.find((w) => w.id === activeId) || watchlists?.[0]

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['watchlists-full'] })
    queryClient.invalidateQueries({ queryKey: ['price-alerts'] })
  }

  const createList = useMutation({
    mutationFn: (name) => watchlistApi.create(name),
    onSuccess: () => { invalidate(); setNewListModal(false); setListName('') },
  })
  const deleteList = useMutation({
    mutationFn: (id) => watchlistApi.remove(id),
    onSuccess: invalidate,
  })
  const addItem = useMutation({
    mutationFn: (symbol) => watchlistApi.addItem(active.id, symbol),
    onSuccess: () => { invalidate(); setAddStockModal(false); setSymbolInput('') },
  })
  const removeItem = useMutation({
    mutationFn: (stockId) => watchlistApi.removeItem(active.id, stockId),
    onSuccess: invalidate,
  })
  const createAlert = useMutation({
    mutationFn: (payload) => watchlistApi.createAlert(payload),
    onSuccess: () => { invalidate(); setAlertModal(false); setAlertForm({ symbol: '', condition: 'ABOVE', targetPrice: '' }) },
  })
  const deleteAlert = useMutation({
    mutationFn: (id) => watchlistApi.deleteAlert(id),
    onSuccess: invalidate,
  })

  const sortedItems = [...(active?.items || [])].sort((a, b) => {
    let cmp = 0
    if (sort.by === 'price') cmp = (a.lastPrice || 0) - (b.lastPrice || 0)
    else if (sort.by === 'change') cmp = (a.dayChangePct || 0) - (b.dayChangePct || 0)
    else cmp = a.symbol.localeCompare(b.symbol)
    return sort.dir === 'asc' ? cmp : -cmp
  })

  const toggleSort = (by) => {
    setSort((s) => (s.by === by ? { by, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { by, dir: 'asc' }))
  }

  return (
    <DashboardLayout title="Watchlist">
      <div className="flex items-center justify-between mb-5 flex-wrap gap-3">
        <div className="flex items-center gap-2 flex-wrap">
          {(watchlists || []).map((w) => (
            <button
              key={w.id}
              onClick={() => setActiveId(w.id)}
              className={`rounded-xl px-3.5 py-1.5 text-sm font-medium transition-colors ${
                (active?.id === w.id) ? 'bg-signal-blue/10 text-signal-blue border border-signal-blue/30' : 'text-slate-400 hover:text-slate-200 border border-transparent'
              }`}
            >
              {w.name}
            </button>
          ))}
          <button onClick={() => setNewListModal(true)} className="rounded-xl px-3 py-1.5 text-sm text-slate-500 hover:text-slate-300 border border-dashed border-ink-700 flex items-center gap-1">
            <FiPlus /> New list
          </button>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setAlertModal(true)} className="flex items-center gap-1.5 rounded-xl border border-ink-700 px-3.5 py-1.5 text-sm text-slate-300 hover:border-amber/50 hover:text-amber transition-colors">
            <FiBell /> New alert
          </button>
          {active && (
            <button onClick={() => setAddStockModal(true)} className="flex items-center gap-1.5 rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet px-3.5 py-1.5 text-sm font-medium text-white shadow-glow">
              <FiPlus /> Add stock
            </button>
          )}
        </div>
      </div>

      <div className="glass-panel p-5 mb-6 overflow-x-auto">
        {isLoading ? <TableSkeleton /> : !active ? (
          <p className="text-sm text-slate-600">Create a watchlist to start tracking stocks.</p>
        ) : sortedItems.length === 0 ? (
          <p className="text-sm text-slate-600">No stocks in this list yet — add one above.</p>
        ) : (
          <table className="w-full text-sm min-w-[560px]">
            <thead>
              <tr className="text-left text-xs text-slate-500 border-b border-ink-700">
                <th className="pb-2 font-medium cursor-pointer" onClick={() => toggleSort('symbol')}>Symbol</th>
                <th className="pb-2 font-medium">Company</th>
                <th className="pb-2 font-medium cursor-pointer" onClick={() => toggleSort('price')}>Price</th>
                <th className="pb-2 font-medium cursor-pointer" onClick={() => toggleSort('change')}>Change</th>
                <th className="pb-2 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {sortedItems.map((item) => (
                <tr key={item.itemId} className="border-b border-ink-700/50 last:border-0">
                  <td className="py-2.5"><Link to={`/stocks/${item.symbol}`} className="font-mono text-slate-200 hover:text-signal-blue">{item.symbol}</Link></td>
                  <td className="py-2.5 text-slate-400">{item.companyName}</td>
                  <td className="py-2.5 tabular-nums text-slate-300">{fmt(item.lastPrice)}</td>
                  <td className={`py-2.5 tabular-nums ${Number(item.dayChangePct) >= 0 ? 'text-gain' : 'text-loss'}`}>
                    {Number(item.dayChangePct || 0).toFixed(2)}%
                  </td>
                  <td className="py-2.5 text-right">
                    <button onClick={() => removeItem.mutate(item.stockId)} className="text-slate-600 hover:text-loss transition-colors">
                      <FiTrash2 className="text-sm" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {active && (
          <button onClick={() => deleteList.mutate(active.id)} className="mt-4 text-xs text-slate-600 hover:text-loss transition-colors">
            Delete this watchlist
          </button>
        )}
      </div>

      <div className="glass-panel p-5">
        <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Price alerts</p>
        {(!alerts || alerts.length === 0) ? (
          <p className="text-sm text-slate-600">No price alerts set.</p>
        ) : (
          <div className="space-y-2">
            {alerts.map((a) => (
              <div key={a.id} className="flex items-center justify-between text-sm">
                <span className="font-mono text-slate-300">{a.symbol}</span>
                <span className="text-slate-400">{a.condition === 'ABOVE' ? 'Above' : 'Below'} {fmt(a.targetPrice)}</span>
                <Badge variant={a.active ? 'amber' : 'neutral'}>{a.active ? 'active' : 'triggered'}</Badge>
                <button onClick={() => deleteAlert.mutate(a.id)} className="text-slate-600 hover:text-loss transition-colors">
                  <FiX className="text-sm" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={newListModal} onClose={() => setNewListModal(false)} title="New watchlist">
        <form onSubmit={(e) => { e.preventDefault(); createList.mutate(listName) }}>
          <input
            required value={listName} onChange={(e) => setListName(e.target.value)}
            placeholder="e.g. Tech Growth"
            className="w-full mb-4 rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70"
          />
          <button type="submit" className="w-full rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet py-2.5 text-sm font-medium text-white">
            Create
          </button>
        </form>
      </Modal>

      <Modal open={addStockModal} onClose={() => setAddStockModal(false)} title="Add stock to watchlist">
        <form onSubmit={(e) => { e.preventDefault(); addItem.mutate(symbolInput.toUpperCase()) }}>
          <input
            required value={symbolInput} onChange={(e) => setSymbolInput(e.target.value)}
            placeholder="e.g. AAPL"
            className="w-full mb-4 rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70"
          />
          <button type="submit" className="w-full rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet py-2.5 text-sm font-medium text-white">
            Add
          </button>
        </form>
      </Modal>

      <Modal open={alertModal} onClose={() => setAlertModal(false)} title="New price alert">
        <form onSubmit={(e) => { e.preventDefault(); createAlert.mutate({ ...alertForm, symbol: alertForm.symbol.toUpperCase() }) }}>
          <input
            required value={alertForm.symbol} onChange={(e) => setAlertForm({ ...alertForm, symbol: e.target.value })}
            placeholder="Symbol e.g. AAPL"
            className="w-full mb-3 rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70"
          />
          <div className="grid grid-cols-2 gap-2 mb-3">
            {['ABOVE', 'BELOW'].map((c) => (
              <button
                type="button" key={c}
                onClick={() => setAlertForm({ ...alertForm, condition: c })}
                className={`rounded-xl border py-2 text-sm font-medium transition-colors ${
                  alertForm.condition === c ? 'border-signal-blue/70 bg-signal-blue/10 text-signal-blue' : 'border-ink-700 text-slate-400'
                }`}
              >
                {c === 'ABOVE' ? 'Price rises above' : 'Price falls below'}
              </button>
            ))}
          </div>
          <input
            required type="number" step="0.01" value={alertForm.targetPrice}
            onChange={(e) => setAlertForm({ ...alertForm, targetPrice: e.target.value })}
            placeholder="Target price"
            className="w-full mb-4 rounded-xl bg-ink-900/80 border border-ink-700 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70"
          />
          <button type="submit" className="w-full rounded-xl bg-gradient-to-r from-amber to-signal-violet py-2.5 text-sm font-medium text-white">
            Create alert
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
