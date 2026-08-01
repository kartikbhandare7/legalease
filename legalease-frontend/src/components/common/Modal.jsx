import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Modal({ open, onClose, title, children, width = 'max-w-lg' }) {
  // Close on Escape key
  useEffect(() => {
    function handler(e) {
      if (e.key === 'Escape') onClose()
    }
    if (open) document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [open, onClose])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-ink/50 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Panel */}
      <div className={`
        relative bg-white rounded-lg shadow-xl w-full ${width}
        max-h-[90vh] flex flex-col
      `}>
        {/* Header */}
        <div className="flex items-center justify-between
                         px-6 py-4 border-b border-border shrink-0">
          <h2 className="font-display text-base font-semibold text-ink">
            {title}
          </h2>
          <button
            onClick={onClose}
            className="text-text-muted hover:text-ink transition-colors"
          >
            <X size={16} />
          </button>
        </div>

        {/* Body — scrollable */}
        <div className="overflow-y-auto flex-1 px-6 py-5">
          {children}
        </div>
      </div>
    </div>
  )
}