export default function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center
                     py-16 text-center">
      {Icon && (
        <div className="w-12 h-12 rounded-full bg-surface-alt
                         flex items-center justify-center mb-4">
          <Icon size={22} className="text-text-muted" />
        </div>
      )}
      <h3 className="font-display text-base font-semibold text-ink mb-1">
        {title}
      </h3>
      {description && (
        <p className="text-sm text-text-muted font-body max-w-xs mb-5">
          {description}
        </p>
      )}
      {action}
    </div>
  )
}