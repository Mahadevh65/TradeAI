import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { FiX } from 'react-icons/fi'

export default function Modal({ open, onClose, title, children }) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center px-4"
          onClick={onClose}
        >
          <motion.div
            initial={{ opacity: 0, y: 12, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 12, scale: 0.98 }}
            transition={{ duration: 0.15 }}
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-md glass-panel p-6"
          >
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-display text-lg font-semibold text-slate-100">{title}</h3>
              <button onClick={onClose} className="text-slate-500 hover:text-slate-200 transition-colors">
                <FiX className="text-lg" />
              </button>
            </div>
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
