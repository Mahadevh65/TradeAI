import React from 'react'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

export default function DashboardLayout({ title, children }) {
  return (
    <div className="flex min-h-screen bg-ink-950">
      <Sidebar />
      <div className="flex-1 min-w-0 flex flex-col">
        <TopBar title={title} />
        <main className="flex-1 px-4 md:px-6 py-6 max-w-[1400px] w-full mx-auto">
          {children}
        </main>
      </div>
    </div>
  )
}
