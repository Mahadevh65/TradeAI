import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { FiSearch } from 'react-icons/fi'
import DashboardLayout from '../../components/layout/DashboardLayout'
import { stockApi } from '../../api/stock'

export default function Markets() {
  const [query, setQuery] = useState('')

  const { data: gainers } = useQuery({ queryKey: ['movers-gainers'], queryFn: () => stockApi.gainers().then((r) => r.data.data) })
  const { data: losers } = useQuery({ queryKey: ['movers-losers'], queryFn: () => stockApi.losers().then((r) => r.data.data) })
  const { data: results } = useQuery({
    queryKey: ['stock-search', query],
    queryFn: () => stockApi.search(query).then((r) => r.data.data),
    enabled: query.length > 0,
  })

  return (
    <DashboardLayout title="Markets">
      <div className="relative mb-6">
        <FiSearch className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
        <input
          value={query} onChange={(e) => setQuery(e.target.value)}
          placeholder="Search any symbol or company…"
          className="w-full rounded-xl bg-ink-900/80 border border-ink-700 pl-11 pr-4 py-3 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20"
        />
      </div>

      {query && (
        <div className="glass-panel p-5 mb-6">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-3">Results</p>
          {(!results || results.length === 0) ? (
            <p className="text-sm text-slate-600">No matches yet — try a different symbol.</p>
          ) : (
            <div className="grid sm:grid-cols-2 gap-2">
              {results.map((s) => (
                <Link key={s.id} to={`/stocks/${s.symbol}`} className="flex items-center justify-between rounded-xl border border-ink-700/60 px-4 py-3 hover:border-signal-blue/40 transition-colors">
                  <span>
                    <span className="font-mono text-slate-200 mr-2">{s.symbol}</span>
                    <span className="text-sm text-slate-500">{s.companyName}</span>
                  </span>
                  <span className={`text-xs font-medium ${Number(s.dayChangePct) >= 0 ? 'text-gain' : 'text-loss'}`}>
                    {Number(s.dayChangePct || 0).toFixed(2)}%
                  </span>
                </Link>
              ))}
            </div>
          )}
        </div>
      )}

      <div className="grid lg:grid-cols-2 gap-4">
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Top gainers</p>
          <MoverTable items={gainers} />
        </div>
        <div className="glass-panel p-5">
          <p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-4">Top losers</p>
          <MoverTable items={losers} />
        </div>
      </div>
    </DashboardLayout>
  )
}

// function MoverTable({ items }) {
//   if (!items || items.length === 0) return <p className="text-sm text-slate-600">No market data cached yet — configure a data provider API key.</p>
//   return (
//     <table className="w-full text-sm">
//       <tbody>
//         {items.map((s) => (
//           <tr key={s.id} className="border-b border-ink-700/50 last:border-0">
//             <td className="py-2"><Link to={`/stocks/${s.symbol}`} className="font-mono text-slate-200 hover:text-signal-blue">{s.symbol}</Link></td>
//             <td className="py-2 text-slate-500">{s.companyName}</td>
//             <td className="py-2 tabular-nums text-slate-300">{s.lastPrice}</td>
//             <td className={`py-2 tabular-nums text-right ${Number(s.dayChangePct) >= 0 ? 'text-gain' : 'text-loss'}`}>
//               {Number(s.dayChangePct || 0).toFixed(2)}%
//             </td>
//           </tr>
//         ))}
//       </tbody>
//     </table>
//   )
// }

function MoverTable({ items }) {

    if (!items || items.length === 0)
        return (
            <p className="text-sm text-slate-500">
                No market data available.
            </p>
        );

    return (

        <div className="space-y-4">

            {items.map((s) => (

                <Link
                    key={s.id}
                    to={`/stocks/${s.symbol}`}
                    className="block rounded-xl border border-ink-700 bg-ink-900/40 hover:border-signal-blue transition p-4"
                >

                    <div className="flex justify-between items-start">

                        <div>

                            <h3 className="text-white font-semibold text-lg">
                                {s.companyName}
                            </h3>

                            <p className="text-slate-400 text-sm mt-1">

                                <span className="font-semibold text-slate-300">
                                    Symbol :
                                </span>{" "}
                                {s.symbol}

                            </p>

                            <p className="text-slate-400 text-sm">

                                <span className="font-semibold text-slate-300">
                                    Exchange :
                                </span>{" "}
                                {s.exchange || "--"}

                            </p>

                            <p className="text-slate-400 text-sm">

                                <span className="font-semibold text-slate-300">
                                    Country :
                                </span>{" "}
                                {s.country || "--"}

                            </p>

                            <p className="text-slate-400 text-sm">

                                <span className="font-semibold text-slate-300">
                                    Currency :
                                </span>{" "}
                                {s.currency || "--"}

                            </p>

                        </div>

                        <div className="text-right">

                            <div className="text-xl font-bold text-white">

                                {s.lastPrice
                                    ? `$${Number(s.lastPrice).toFixed(2)}`
                                    : "--"}

                            </div>

                            <div
                                className={`font-semibold mt-2 ${
                                    Number(s.dayChangePct) >= 0
                                        ? "text-green-400"
                                        : "text-red-400"
                                }`}
                            >
                                {Number(s.dayChangePct || 0).toFixed(2)}%
                            </div>

                        </div>

                    </div>

                </Link>

            ))}

        </div>

    );

}
