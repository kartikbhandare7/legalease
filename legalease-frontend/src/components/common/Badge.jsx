const variants = {
  ACTIVE:    'bg-success/10 text-success border-success/20',
  CLOSED:    'bg-text-muted/10 text-text-muted border-text-muted/20',
  ON_HOLD:   'bg-accent/10 text-accent border-accent/20',
  PENDING:   'bg-accent/10 text-accent border-accent/20',
  REJECTED:  'bg-danger/10 text-danger border-danger/20',
  APPROVED:  'bg-success/10 text-success border-success/20',
  CRIMINAL:  'bg-danger/10 text-danger border-danger/20',
  CIVIL:     'bg-blue-50 text-blue-700 border-blue-200',
  FAMILY:    'bg-purple-50 text-purple-700 border-purple-200',
  PROPERTY:  'bg-amber-50 text-amber-700 border-amber-200',
  CORPORATE: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  LABOUR:    'bg-orange-50 text-orange-700 border-orange-200',
  OTHER:     'bg-surface-alt text-text-muted border-border',
}

export default function Badge({ value, className = '' }) {
  const style = variants[value] ?? variants.OTHER
  return (
    <span className={`
      inline-flex items-center px-2 py-0.5 rounded text-xs
      font-semibold font-body border ${style} ${className}
    `}>
      {value?.replace('_', ' ')}
    </span>
  )
}