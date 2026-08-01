import { ChevronLeft, ChevronRight } from 'lucide-react'

export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-between pt-4
                     border-t border-border mt-4">
      <p className="text-xs text-text-muted font-body">
        Page {page + 1} of {totalPages}
      </p>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                      disabled:opacity-30 disabled:cursor-not-allowed
                      transition-colors"
        >
          <ChevronLeft size={15} />
        </button>

        {/* Page number pills */}
        {Array.from({ length: totalPages }, (_, i) => (
          <button
            key={i}
            onClick={() => onPageChange(i)}
            className={`
              w-7 h-7 rounded text-xs font-mono font-semibold transition-colors
              ${i === page
                ? 'bg-ink text-white'
                : 'text-text-muted hover:bg-surface-alt'
              }
            `}
          >
            {i + 1}
          </button>
        ))}

        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="p-1.5 rounded hover:bg-surface-alt text-text-muted
                      disabled:opacity-30 disabled:cursor-not-allowed
                      transition-colors"
        >
          <ChevronRight size={15} />
        </button>
      </div>
    </div>
  )
}