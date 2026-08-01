import Spinner from './Spinner'
import EmptyState from './EmptyState'
import { FileX } from 'lucide-react'

export default function Table({ columns, data, loading, emptyTitle = 'No records found' }) {
  if (loading) return <Spinner size={24} className="py-16" />

  if (!data?.length) {
    return (
      <EmptyState
        icon={FileX}
        title={emptyTitle}
        description="Records will appear here once created."
      />
    )
  }

  return (
    <div className="overflow-x-auto rounded border border-border">
      <table className="w-full text-sm font-body">
        <thead>
          <tr className="bg-surface-alt border-b border-border">
            {columns.map(col => (
              <th
                key={col.key}
                className="text-left text-xs font-semibold text-text-muted
                            uppercase tracking-wide px-4 py-3"
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border bg-white">
          {data.map((row, i) => (
            <tr
              key={row.id ?? i}
              className="hover:bg-surface transition-colors"
            >
              {columns.map(col => (
                <td key={col.key} className="px-4 py-3 text-text">
                  {col.render ? col.render(row) : row[col.key] ?? '—'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}