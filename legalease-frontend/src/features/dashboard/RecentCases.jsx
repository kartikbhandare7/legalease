import { useNavigate } from 'react-router-dom'
import { FolderOpen, ArrowRight } from 'lucide-react'
import Badge from '@/components/common/Badge'
import EmptyState from '@/components/common/EmptyState'
import { formatDate } from '@/utils/formatDate'

export default function RecentCases({ cases }) {
  const navigate = useNavigate()

  return (
    <div className="bg-white border border-border rounded-lg">

      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4
                       border-b border-border">
        <div className="flex items-center gap-2">
          <FolderOpen size={15} className="text-accent" />
          <h3 className="font-display text-sm font-semibold text-ink">
            Recent cases
          </h3>
        </div>
        <button
          onClick={() => navigate('/cases')}
          className="text-xs text-text-muted hover:text-accent
                      font-body flex items-center gap-1 transition-colors"
        >
          View all <ArrowRight size={12} />
        </button>
      </div>

      {/* List */}
      <div className="divide-y divide-border">
        {cases.length === 0
          ? (
            <EmptyState
              icon={FolderOpen}
              title="No cases yet"
              description="Create your first case to get started."
            />
          )
          : cases.map(c => (
            <div
              key={c.id}
              onClick={() => navigate(`/cases/${c.id}`)}
              className="px-5 py-3.5 hover:bg-surface cursor-pointer
                          transition-colors flex items-center gap-3"
            >
              {/* Gold bookmark — signature element */}
              <div className="w-0.5 h-8 bg-accent rounded-full shrink-0" />

              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-ink font-body truncate">
                  {c.caseTitle}
                </p>
                <p className="text-xs text-text-muted font-body mt-0.5">
                  {formatDate(c.createdAt)}
                </p>
              </div>

              <div className="flex items-center gap-2 shrink-0">
                <Badge value={c.caseType} />
                <Badge value={c.caseStatus} />
              </div>
            </div>
          ))
        }
      </div>
    </div>
  )
}