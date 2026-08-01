import { forwardRef } from 'react'

const Input = forwardRef(function Input({
  label, error, hint, className = '', ...props
}, ref) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-xs font-semibold text-text-muted
                           uppercase tracking-wide font-body">
          {label}
        </label>
      )}
      <input
        ref={ref}
        className={`
          w-full h-10 px-3 text-sm font-body text-text
          bg-white border rounded
          placeholder:text-text-muted/50
          focus:outline-none focus:ring-2 focus:ring-accent/30
          focus:border-accent transition-all duration-150
          disabled:bg-surface-alt disabled:cursor-not-allowed
          ${error ? 'border-danger' : 'border-border'}
          ${className}
        `}
        {...props}
      />
      {error && (
        <p className="text-xs text-danger font-body">{error}</p>
      )}
      {hint && !error && (
        <p className="text-xs text-text-muted font-body">{hint}</p>
      )}
    </div>
  )
})

export default Input