import React from 'react'

export function CardSkeleton({ className = '' }) {
  return (
    <div className={`glass-panel p-5 animate-pulse ${className}`}>
      <div className="h-3 w-24 bg-ink-700 rounded mb-4" />
      <div className="h-7 w-32 bg-ink-700 rounded mb-2" />
      <div className="h-3 w-16 bg-ink-700 rounded" />
    </div>
  )
}

export function TableSkeleton({ rows = 5 }) {
  return (
    <div className="glass-panel p-5 animate-pulse space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-8 bg-ink-700/60 rounded" />
      ))}
    </div>
  )
}
