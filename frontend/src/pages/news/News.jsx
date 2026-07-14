import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Badge from '../../components/dashboard/Badge'
import { newsApi } from '../../api/news'

const CATEGORIES = ['All', 'stock market', 'earnings', 'crypto', 'economy']

export default function News() {
  const [category, setCategory] = useState('All')

  const { data: articles, isLoading } = useQuery({
    queryKey: ['news', category],
    queryFn: () => newsApi.getLatest(category === 'All' ? undefined : category).then((r) => r.data.data),
  })

  return (
    <DashboardLayout title="Market News">
      <div className="flex gap-2 mb-6 flex-wrap">
        {CATEGORIES.map((c) => (
          <button
            key={c} onClick={() => setCategory(c)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
              category === c ? 'bg-signal-blue/10 text-signal-blue border border-signal-blue/30' : 'text-slate-400 border border-ink-700 hover:text-slate-200'
            }`}
          >
            {c}
          </button>
        ))}
      </div>

      {isLoading ? (
        <p className="text-sm text-slate-600">Loading…</p>
      ) : (!articles || articles.length === 0) ? (
        <div className="glass-panel p-8 text-center">
          <p className="text-sm text-slate-500">
            No news cached yet. Set <code className="text-signal-cyan">NEWS_API_KEY</code> in the backend
            environment, then call <code className="text-signal-cyan">POST /api/v1/news/refresh</code> (this
            also runs automatically every 15 minutes once configured).
          </p>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {articles.map((a) => (
            <a key={a.id} href={a.url} target="_blank" rel="noreferrer" className="glass-panel p-4 flex flex-col hover:border-signal-blue/30 border border-transparent transition-colors">
              {a.imageUrl && <img src={a.imageUrl} alt="" className="rounded-lg h-36 w-full object-cover mb-3" onError={(e) => (e.target.style.display = 'none')} />}
              <p className="text-sm text-slate-200 line-clamp-3 mb-2">{a.title}</p>
              <div className="mt-auto flex items-center justify-between">
                <span className="text-xs text-slate-600">{a.source}</span>
                <Badge variant={a.sentiment === 'BULLISH' ? 'gain' : a.sentiment === 'BEARISH' ? 'loss' : 'neutral'}>
                  {a.sentiment?.toLowerCase()}
                </Badge>
              </div>
            </a>
          ))}
        </div>
      )}
    </DashboardLayout>
  )
}
