import React, { useState } from 'react'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { FiLock, FiUnlock } from 'react-icons/fi'
import DashboardLayout from '../../components/layout/DashboardLayout'
import StatCard from '../../components/dashboard/StatCard'
import Badge from '../../components/dashboard/Badge'
import { TableSkeleton, CardSkeleton } from '../../components/dashboard/Skeleton'
import { adminApi } from '../../api/admin'

const TABS = ['Users', 'Trades', 'Audit logs', 'Market config']

export default function Admin() {
  const [tab, setTab] = useState('Users')
  const queryClient = useQueryClient()

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['admin-stats'], queryFn: () => adminApi.dashboardStats().then((r) => r.data.data),
  })
  const { data: usersPage, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users'], queryFn: () => adminApi.listUsers(0, 20).then((r) => r.data.data),
    enabled: tab === 'Users',
  })
  const { data: tradesPage } = useQuery({
    queryKey: ['admin-trades'], queryFn: () => adminApi.allTrades(0, 20).then((r) => r.data.data),
    enabled: tab === 'Trades',
  })
  const { data: logsPage } = useQuery({
    queryKey: ['admin-logs'], queryFn: () => adminApi.auditLogs(undefined, 0, 30).then((r) => r.data.data),
    enabled: tab === 'Audit logs',
  })
  const { data: marketConfig } = useQuery({
    queryKey: ['admin-market-config'], queryFn: () => adminApi.marketConfig().then((r) => r.data.data),
    enabled: tab === 'Market config',
  })

  const toggleActive = useMutation({
    mutationFn: ({ id, active }) => adminApi.setActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  })
  const unlock = useMutation({
    mutationFn: (id) => adminApi.unlock(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  })
  const toggleMarket = useMutation({
    mutationFn: ({ marketName, open }) => adminApi.updateMarketConfig(marketName, { open }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-market-config'] }),
  })

  return (
    <DashboardLayout title="Admin">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {statsLoading ? Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />) : (
          <>
            <StatCard label="Total users" value={stats?.totalUsers} prefix="" />
            <StatCard label="Active users" value={stats?.activeUsers} prefix="" />
            <StatCard label="Total trades" value={stats?.totalTrades} prefix="" />
            <StatCard label="Trades today" value={stats?.tradesToday} prefix="" />
          </>
        )}
      </div>

      <div className="flex gap-2 mb-5 flex-wrap">
        {TABS.map((t) => (
          <button
            key={t} onClick={() => setTab(t)}
            className={`rounded-xl px-3.5 py-1.5 text-sm font-medium transition-colors ${
              tab === t ? 'bg-amber/10 text-amber border border-amber/30' : 'text-slate-400 border border-transparent hover:text-slate-200'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Users' && (
        <div className="glass-panel p-5 overflow-x-auto">
          {usersLoading ? <TableSkeleton /> : (
            <table className="w-full text-sm min-w-[720px]">
              <thead>
                <tr className="text-left text-xs text-slate-500 border-b border-ink-700">
                  <th className="pb-2 font-medium">Name</th>
                  <th className="pb-2 font-medium">Email</th>
                  <th className="pb-2 font-medium">Roles</th>
                  <th className="pb-2 font-medium">Status</th>
                  <th className="pb-2 font-medium"></th>
                </tr>
              </thead>
              <tbody>
                {usersPage?.content?.map((u) => (
                  <tr key={u.id} className="border-b border-ink-700/50 last:border-0">
                    <td className="py-2.5 text-slate-200">{u.fullName}</td>
                    <td className="py-2.5 text-slate-400">{u.email}</td>
                    <td className="py-2.5">
                      <div className="flex gap-1 flex-wrap">
                        {u.roles.map((r) => <Badge key={r} variant="violet">{r}</Badge>)}
                      </div>
                    </td>
                    <td className="py-2.5">
                      {u.locked ? <Badge variant="loss">Locked</Badge> : u.active ? <Badge variant="gain">Active</Badge> : <Badge variant="neutral">Inactive</Badge>}
                    </td>
                    <td className="py-2.5 text-right space-x-2">
                      {u.locked && (
                        <button onClick={() => unlock.mutate(u.id)} className="text-xs text-amber hover:underline inline-flex items-center gap-1"><FiUnlock className="text-[10px]" /> Unlock</button>
                      )}
                      <button
                        onClick={() => toggleActive.mutate({ id: u.id, active: !u.active })}
                        className="text-xs text-slate-500 hover:text-slate-300 inline-flex items-center gap-1"
                      >
                        <FiLock className="text-[10px]" /> {u.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'Trades' && (
        <div className="glass-panel p-5 overflow-x-auto">
          <table className="w-full text-sm min-w-[600px]">
            <tbody>
              {tradesPage?.content?.map((t) => (
                <tr key={t.id} className="border-b border-ink-700/50 last:border-0">
                  <td className="py-2 font-mono text-slate-300">{t.symbol}</td>
                  <td className="py-2"><Badge variant={t.tradeType === 'BUY' ? 'gain' : 'loss'}>{t.tradeType}</Badge></td>
                  <td className="py-2 tabular-nums text-slate-400">{t.quantity} sh</td>
                  <td className="py-2 tabular-nums text-slate-400">{t.price}</td>
                  <td className="py-2 text-xs text-slate-600">{new Date(t.executedAt).toLocaleString()}</td>
                </tr>
              ))}
              {(!tradesPage || tradesPage.content?.length === 0) && (
                <tr><td className="py-4 text-slate-600 text-sm">No trades on the platform yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'Audit logs' && (
        <div className="glass-panel p-5 overflow-x-auto">
          <table className="w-full text-sm min-w-[600px]">
            <tbody>
              {logsPage?.content?.map((l) => (
                <tr key={l.id} className="border-b border-ink-700/50 last:border-0">
                  <td className="py-2"><Badge variant="neutral">{l.action}</Badge></td>
                  <td className="py-2 text-slate-400">{l.userEmail || '—'}</td>
                  <td className="py-2 text-xs text-slate-600">{l.ipAddress || '—'}</td>
                  <td className="py-2 text-xs text-slate-600">{new Date(l.createdAt).toLocaleString()}</td>
                </tr>
              ))}
              {(!logsPage || logsPage.content?.length === 0) && (
                <tr><td className="py-4 text-slate-600 text-sm">No audit events yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'Market config' && (
        <div className="glass-panel p-5 space-y-3">
          {(marketConfig || []).map((m) => (
            <div key={m.id} className="flex items-center justify-between border-b border-ink-700/50 last:border-0 py-3">
              <div>
                <p className="text-sm font-medium text-slate-200">{m.marketName}</p>
                <p className="text-xs text-slate-500">{m.openTime} – {m.closeTime} ({m.timezone})</p>
              </div>
              <button
                onClick={() => toggleMarket.mutate({ marketName: m.marketName, open: !m.open })}
                className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                  m.open ? 'bg-gain/10 text-gain border border-gain/30' : 'bg-slate-500/10 text-slate-400 border border-slate-500/30'
                }`}
              >
                {m.open ? 'Open' : 'Closed'}
              </button>
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  )
}
