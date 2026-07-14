import React, { useState, useRef, useEffect } from 'react'
import { useMutation } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { FiSend, FiCpu, FiUser } from 'react-icons/fi'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Badge from '../../components/dashboard/Badge'
import { aiApi } from '../../api/ai'

const SUGGESTIONS = [
  'Summarize today\'s market',
  'Should I buy AAPL?',
  'Compare MSFT and GOOGL',
  'Which sectors look undervalued?',
]

const RECOMMENDATION_VARIANT = { BUY: 'gain', SELL: 'loss', HOLD: 'amber', WATCH: 'neutral' }

export default function AiCopilot() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const scrollRef = useRef(null)

  const askMutation = useMutation({
    mutationFn: (question) => aiApi.ask({ question }).then((r) => r.data.data),
    onSuccess: (data, question) => {
      setMessages((m) => [...m, { role: 'user', text: question }, { role: 'ai', data }])
    },
    onError: (err, question) => {
      setMessages((m) => [...m, { role: 'user', text: question },
        { role: 'ai', data: { summary: err.response?.data?.message || 'Something went wrong.', recommendation: 'WATCH', confidenceScore: 0, pros: [], cons: [] } }])
    },
  })

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages, askMutation.isPending])

  const send = (question) => {
    if (!question.trim()) return
    setInput('')
    askMutation.mutate(question)
  }

  return (
    <DashboardLayout title="AI Copilot">
      <div className="glass-panel flex flex-col h-[calc(100vh-160px)]">
        <div ref={scrollRef} className="flex-1 overflow-y-auto p-5 space-y-4">
          {messages.length === 0 && (
            <div className="h-full flex flex-col items-center justify-center text-center px-6">
              <FiCpu className="text-4xl text-signal-violet mb-3" />
              <p className="text-sm text-slate-400 mb-5 max-w-xs">
                Ask about a stock, your portfolio, or the broader market.
              </p>
              <div className="flex flex-wrap gap-2 justify-center">
                {SUGGESTIONS.map((s) => (
                  <button key={s} onClick={() => send(s)} className="rounded-full border border-ink-700 px-3.5 py-1.5 text-xs text-slate-400 hover:border-signal-violet/50 hover:text-signal-violet transition-colors">
                    {s}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messages.map((m, i) => m.role === 'user' ? (
            <div key={i} className="flex justify-end">
              <div className="flex items-start gap-2 max-w-lg">
                <div className="rounded-2xl rounded-tr-sm bg-signal-blue/15 border border-signal-blue/25 px-4 py-2.5 text-sm text-slate-100">
                  {m.text}
                </div>
                <div className="h-7 w-7 shrink-0 rounded-full bg-ink-700 flex items-center justify-center"><FiUser className="text-xs text-slate-400" /></div>
              </div>
            </div>
          ) : (
            <motion.div key={i} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="flex justify-start">
              <div className="flex items-start gap-2 max-w-2xl w-full">
                <div className="h-7 w-7 shrink-0 rounded-full bg-gradient-to-br from-signal-violet to-signal-cyan flex items-center justify-center"><FiCpu className="text-xs text-white" /></div>
                <AiResponseCard data={m.data} />
              </div>
            </motion.div>
          ))}

          {askMutation.isPending && (
            <div className="flex items-start gap-2">
              <div className="h-7 w-7 shrink-0 rounded-full bg-gradient-to-br from-signal-violet to-signal-cyan flex items-center justify-center"><FiCpu className="text-xs text-white" /></div>
              <div className="rounded-2xl rounded-tl-sm bg-ink-800/60 border border-ink-700 px-4 py-3 text-sm text-slate-500">
                Thinking…
              </div>
            </div>
          )}
        </div>

        <form
          onSubmit={(e) => { e.preventDefault(); send(input) }}
          className="border-t border-ink-700 p-4 flex items-center gap-2"
        >
          <input
            value={input} onChange={(e) => setInput(e.target.value)}
            placeholder="Ask the copilot anything…"
            className="flex-1 rounded-xl bg-ink-900/80 border border-ink-700 px-4 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-signal-blue/70 focus:ring-2 focus:ring-signal-blue/20"
          />
          <button type="submit" disabled={askMutation.isPending} className="rounded-xl bg-gradient-to-r from-signal-blue to-signal-violet p-2.5 text-white shadow-glow disabled:opacity-60">
            <FiSend />
          </button>
        </form>
      </div>
    </DashboardLayout>
  )
}

function AiResponseCard({ data }) {
  return (
    <div className="rounded-2xl rounded-tl-sm bg-ink-800/60 border border-ink-700 p-4 text-sm text-slate-300 w-full space-y-3">
      <p>{data.summary}</p>

      {(data.recommendation || data.confidenceScore !== undefined) && (
        <div className="flex items-center gap-2">
          <Badge variant={RECOMMENDATION_VARIANT[data.recommendation] || 'neutral'}>{data.recommendation}</Badge>
          {data.confidenceScore > 0 && (
            <span className="text-xs text-slate-500">Confidence: {Number(data.confidenceScore).toFixed(0)}%</span>
          )}
        </div>
      )}

      {data.pros?.length > 0 && (
        <div>
          <p className="text-xs font-mono uppercase tracking-widest text-gain mb-1">Pros</p>
          <ul className="list-disc list-inside text-xs text-slate-400 space-y-0.5">
            {data.pros.map((p, i) => <li key={i}>{p}</li>)}
          </ul>
        </div>
      )}
      {data.cons?.length > 0 && (
        <div>
          <p className="text-xs font-mono uppercase tracking-widest text-loss mb-1">Cons</p>
          <ul className="list-disc list-inside text-xs text-slate-400 space-y-0.5">
            {data.cons.map((c, i) => <li key={i}>{c}</li>)}
          </ul>
        </div>
      )}
      {data.technicalAnalysis && (
        <div><p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-1">Technical</p><p className="text-xs text-slate-400">{data.technicalAnalysis}</p></div>
      )}
      {data.fundamentalAnalysis && (
        <div><p className="text-xs font-mono uppercase tracking-widest text-slate-500 mb-1">Fundamental</p><p className="text-xs text-slate-400">{data.fundamentalAnalysis}</p></div>
      )}
      {data.riskAnalysis && (
        <div><p className="text-xs font-mono uppercase tracking-widest text-amber mb-1">Risk</p><p className="text-xs text-slate-400">{data.riskAnalysis}</p></div>
      )}
    </div>
  )
}
