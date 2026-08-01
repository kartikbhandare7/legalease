import { Loader2 } from 'lucide-react'

const variants = {
  primary:   'bg-accent text-ink hover:bg-accent/90 font-semibold',
  secondary: 'bg-surface-alt text-text border border-border hover:bg-border',
  danger:    'bg-danger text-white hover:bg-danger/90',
  ghost:     'text-text-muted hover:text-ink hover:bg-surface-alt',
  outline:   'border border-accent text-accent hover:bg-accent-soft',
}

const sizes = {
  sm: 'text-xs px-3 py-1.5 h-7',
  md: 'text-sm px-4 py-2 h-9',
  lg: 'text-sm px-6 py-2.5 h-11',
}

export default function Button({
  children, variant = 'primary', size = 'md',
  loading = false, disabled = false,
  className = '', ...props
}) {
  return (
    <button
      disabled={disabled || loading}
      className={`
        inline-flex items-center justify-center gap-2 rounded
        font-body transition-all duration-150
        disabled:opacity-50 disabled:cursor-not-allowed
        ${variants[variant]} ${sizes[size]} ${className}
      `}
      {...props}
    >
      {loading && <Loader2 size={13} className="animate-spin" />}
      {children}
    </button>
  )
}