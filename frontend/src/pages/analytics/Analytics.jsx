import React from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts'
import DashboardLayout from '../../components/layout/DashboardLayout'
import { analyticsApi } from '../../api/analytics'

const PIE_COLORS = ['#4C6FFF', '#8B5CF6', '#39D0D8', '#22D3A6', '#F5B94D', '#FB5A5A']

export default function Analytics() {
  const { data: growth } = useQuery({ queryKey: ['analytics-growth'], queryFn: () => analyticsApi.growth(180).then((r) => r.data.data) })
  const { data: sectors } = useQuery({ queryKey: ['analytics-sectors'], queryFn: () => analyticsApi.sectorAllocation().then((r) => r.data.data) })
  const { data: monthly } = useQuery({ queryKey: ['analytics-monthly'], queryFn: () => analyticsApi.monthlyReturns(12).then((r) => r.data.data) })
  const { data: dividends } = useQuery({ queryKey: ['analytics-dividends'], queryFn: () => analyticsApi.dividendIncome().then((r) => r.data.data) })

  const totalMonthlyDividend = (dividends || []).reduce((sum, d) => sum + Number(d.estimatedMonthlyIncome || 0), 0)

  return (
    <DashboardLayout title="Analytics">
      <div className="grid lg:grid-cols-2 gap-4 mb-6">
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Portfolio growth (180d)</p>
          <ResponsiveContainer width="100%" height={260}>
            <AreaChart data={growth || []}>
              <defs>
                <linearGradient id="growthGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#22D3A6" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#22D3A6" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="#1B2233" strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fill: '#64748B', fontSize: 10 }} axisLine={false} tickLine={false} minTickGap={40} />
              <YAxis tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={60} />
              <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
              <Area type="monotone" dataKey="totalValue" stroke="#22D3A6" fill="url(#growthGrad)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Sector allocation</p>
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={sectors || []} dataKey="percentOfPortfolio" nameKey="sector" innerRadius={60} outerRadius={95} paddingAngle={2}>
                {(sectors || []).map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
              </Pie>
              <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-4">
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Monthly returns</p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={monthly || []}>
              <CartesianGrid stroke="#1B2233" strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="month" tick={{ fill: '#64748B', fontSize: 10 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={50} />
              <Tooltip contentStyle={{ background: '#111624', border: '1px solid #1B2233', borderRadius: 12 }} />
              <Bar dataKey="returnPercent" radius={[6, 6, 0, 0]}>
                {(monthly || []).map((m, i) => (
                  <Cell key={i} fill={Number(m.returnPercent) >= 0 ? '#22D3A6' : '#FB5A5A'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">
            Dividend income (est. ${totalMonthlyDividend.toFixed(2)}/mo)
          </p>
          {(!dividends || dividends.length === 0) ? (
            <p className="text-sm text-slate-600">No dividend-paying holdings detected.</p>
          ) : (
            <table className="w-full text-sm">
              <tbody>
                {dividends.map((d) => (
                  <tr key={d.symbol} className="border-b border-ink-700/50 last:border-0">
                    <td className="py-2 font-mono text-slate-300">{d.symbol}</td>
                    <td className="py-2 tabular-nums text-slate-400">${Number(d.estimatedMonthlyIncome).toFixed(2)}/mo</td>
                    <td className="py-2 tabular-nums text-slate-400">${Number(d.estimatedAnnualIncome).toFixed(2)}/yr</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <p className="text-[11px] text-slate-600 mt-3">Estimates based on current dividend yield; actual payouts vary.</p>
        </div>
      </div>
    </DashboardLayout>
  )
}
