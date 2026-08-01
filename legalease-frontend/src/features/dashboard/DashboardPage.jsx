import { useGetDashboardQuery } from './dashboardApi'
import StatCard from './StatCard'
import UpcomingHearings from './UpcomingHearings'
import RecentCases from './RecentCases'
import Spinner from '@/components/common/Spinner'
import { useRole } from '@/hooks/useRole'
import {
  FolderOpen, Users, CalendarDays,
  Sparkles, FolderCheck, FolderMinus
} from 'lucide-react'

export default function DashboardPage() {
  const { data, isLoading } = useGetDashboardQuery()
  const { isLawyer } = useRole()

  if (isLoading) return <Spinner size={28} className="py-24" />

  return (
    <div className="space-y-8 max-w-6xl">

      {/* Page header */}
      <div className="border-b border-border pb-5">
        <h2 className="font-display text-xl font-semibold text-ink">
          Overview
        </h2>
        <p className="text-sm text-text-muted font-body mt-1">
          Your practice at a glance
        </p>
      </div>

      {/* Stat cards — top row */}
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard
          label="Total cases"
          value={data?.totalCases ?? 0}
          icon={FolderOpen}
          accent
        />
        <StatCard
          label="Active cases"
          value={data?.activeCases ?? 0}
          icon={FolderCheck}
          color="success"
        />
        <StatCard
          label="On hold"
          value={data?.onHoldCases ?? 0}
          icon={FolderMinus}
          color="muted"
        />
        <StatCard
          label="Total clients"
          value={data?.totalClients ?? 0}
          icon={Users}
        />
        <StatCard
          label="Total hearings"
          value={data?.totalHearings ?? 0}
          icon={CalendarDays}
        />

        {/* AI adoption card — only shown to lawyers */}
        {isLawyer && (
          <StatCard
            label="AI-assisted logs"
            value={
              (data?.aiAssistedIntakes ?? 0) +
              (data?.aiAssistedHearings ?? 0)
            }
            icon={Sparkles}
            color="accent"
            hint="Intakes + hearings filled using AI"
          />
        )}
      </div>

      {/* Bottom row — upcoming hearings + recent cases */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <UpcomingHearings hearings={data?.upcomingHearings ?? []} />
        <RecentCases cases={data?.recentCases ?? []} />
      </div>

    </div>
  )
}