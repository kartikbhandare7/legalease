const colorMap = {
  default: 'text-ink bg-surface-alt border-border',
  accent:  'text-accent bg-accent-soft border-accent/20',
  success: 'text-success bg-success/5 border-success/20',
  muted:   'text-text-muted bg-surface-alt border-border',
}

export default function StatCard({
  label, value, icon: Icon,
  accent = false, color = 'default', hint
}) {
  const scheme = accent ? colorMap.accent : colorMap[color]

  return (
    <div className="bg-white border border-border rounded-lg p-5
                     hover:shadow-sm transition-shadow duration-150">
      <div className="flex items-start justify-between mb-3">
        <div className={`
          w-9 h-9 rounded flex items-center justify-center
          border ${scheme}
        `}>
          <Icon size={16} />
        </div>
      </div>

      <p className="font-mono text-3xl font-semibold text-ink leading-none mb-1">
        {value}
      </p>
      <p className="text-xs font-semibold text-text-muted uppercase
                     tracking-wide font-body">
        {label}
      </p>
      {hint && (
        <p className="text-xs text-text-muted/70 font-body mt-1">{hint}</p>
      )}
    </div>
  )
}