import React from 'react'
import { motion } from 'framer-motion'
import { FiArrowUpRight, FiArrowDownRight } from 'react-icons/fi'

export default function StatCard({ label, value, delta, prefix = '$', suffix = '', icon: Icon }) {
  const isPositive = delta !== undefined && delta !== null && Number(delta) >= 0

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="glass-panel p-5"
    >
      <div className="flex items-center justify-between mb-3">
        <p className="text-xs font-mono uppercase tracking-widest text-slate-500">{label}</p>
        {Icon && <Icon className="text-signal-blue/70 text-base" />}
      </div>
      <p className="font-display text-2xl font-semibold text-slate-50 tabular-nums">
        {prefix}{value}{suffix}
      </p>
      {delta !== undefined && delta !== null && (
        <p className={`mt-1.5 flex items-center gap-1 text-xs font-medium ${isPositive ? 'text-gain' : 'text-loss'}`}>
          {isPositive ? <FiArrowUpRight /> : <FiArrowDownRight />}
          {Math.abs(Number(delta)).toFixed(2)}{suffix === '%' ? '' : '%'}
        </p>
      )}
    </motion.div>
  )
}
