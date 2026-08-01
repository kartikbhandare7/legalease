import { useNavigate } from 'react-router-dom'
import { CalendarDays, ArrowRight } from 'lucide-react'
import { formatDate } from '@/utils/formatDate'
import EmptyState from '@/components/common/EmptyState'

export default function UpcomingHearings({ hearings }) {
  const navigate = useNavigate()

  return (
    <div className="bg-white border border-border rounded-lg">

      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4
                       border-b border-border">
        <div className="flex items-center gap-2">
          <CalendarDays size={15} className="text-accent" />
          <h3 className="font-display text-sm font-semibold text-ink">
            Upcoming hearings
          </h3>
        </div>
        <button
          onClick={() => navigate('/hearings')}
          className="text-xs text-text-muted hover:text-accent
                      font-body flex items-center gap-1 transition-colors"
        >
          View all <ArrowRight size={12} />
        </button>
      </div>

      {/* List */}
      <div className="divide-y divide-border">
        {hearings.length === 0
          ? (
            <EmptyState
              icon={CalendarDays}
              title="No upcoming hearings"
              description="Hearings with a future next date will appear here."
            />
          )
          : hearings.slice(0, 5).map(h => (
            <div
              key={h.id}
              onClick={() => navigate(`/cases/${h.caseId}`)}
              className="px-5 py-3.5 hover:bg-surface cursor-pointer
                          transition-colors flex items-start gap-3"
            >
              {/* Date block */}
              <div className="w-10 shrink-0 text-center">
                <p className="font-mono text-lg font-semibold text-accent
                               leading-none">
                  {formatDate(h.nextDate, 'dd')}
                </p>
                <p className="font-body text-xs text-text-muted uppercase">
                  {formatDate(h.nextDate, 'MMM')}
                </p>
              </div>

              {/* Divider */}
              <div className="w-px bg-border self-stretch mx-1" />

              {/* Info */}
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-ink font-body
                               truncate">
                  {h.caseTitle}
                </p>
                <p className="text-xs text-text-muted font-body mt-0.5
                               truncate">
                  {h.outcome ?? 'No outcome noted'}
                </p>
              </div>
            </div>
          ))
        }
      </div>
    </div>
  )
}