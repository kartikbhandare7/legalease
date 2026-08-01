import { useNavigate } from 'react-router-dom'
import { useGetAdminDashboardQuery } from './adminApi'
import Spinner from '@/components/common/Spinner'
import { formatDate } from '@/utils/formatDate'
import Badge from '@/components/common/Badge'
import {
  Users, ShieldCheck, ShieldX,
  Clock, ArrowRight, UserCheck
} from 'lucide-react'

function AdminStat({ label, value, icon: Icon, color = 'default' }) {
  const colors = {
    default: 'bg-surface-alt border-border text-ink',
    warning: 'bg-accent/10 border-accent/20 text-accent',
    success: 'bg-success/10 border-success/20 text-success',
    danger:  'bg-danger/10 border-danger/20 text-danger',
  }
  return (
    <div className="bg-white border border-border rounded-lg p-5">
      <div className={`w-9 h-9 rounded border flex items-center justify-center
                        mb-3 ${colors[color]}`}>
        <Icon size={16} />
      </div>
      <p className="font-mono text-3xl font-semibold text-ink leading-none mb-1">
        {value}
      </p>
      <p className="text-xs font-semibold text-text-muted uppercase
                     tracking-wide font-body">
        {label}
      </p>
    </div>
  )
}

export default function AdminDashboardPage() {
  const navigate = useNavigate()
  const { data, isLoading } = useGetAdminDashboardQuery()

  if (isLoading) return <Spinner size={28} className="py-24" />

  return (
    <div className="max-w-5xl space-y-8">

      <div className="border-b border-border pb-5">
        <h2 className="font-display text-xl font-semibold text-ink">
          Admin overview
        </h2>
        <p className="text-sm text-text-muted font-body mt-1">
          Platform health and user management
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <AdminStat
          label="Total users"
          value={data?.totalUsers ?? 0}
          icon={Users}
        />
        <AdminStat
          label="Total lawyers"
          value={data?.totalLawyers ?? 0}
          icon={UserCheck}
        />
        <AdminStat
          label="Pending approvals"
          value={data?.pendingApprovals ?? 0}
          icon={Clock}
          color="warning"
        />
        <AdminStat
          label="Approved lawyers"
          value={data?.approvedLawyers ?? 0}
          icon={ShieldCheck}
          color="success"
        />
        <AdminStat
          label="Rejected accounts"
          value={data?.rejectedLawyers ?? 0}
          icon={ShieldX}
          color="danger"
        />
        <AdminStat
          label="Total clerks"
          value={data?.totalClerks ?? 0}
          icon={Users}
        />
      </div>

      {/* Pending approvals alert */}
      {data?.pendingApprovals > 0 && (
        <div className="flex items-center justify-between p-4 rounded-lg
                         bg-accent/10 border border-accent/20">
          <div className="flex items-center gap-3">
            <Clock size={16} className="text-accent" />
            <div>
              <p className="text-sm font-semibold text-ink font-body">
                {data.pendingApprovals} lawyer{data.pendingApprovals > 1
                  ? 's' : ''} waiting for approval
              </p>
              <p className="text-xs text-text-muted font-body">
                Review their enrollment certificates and approve or reject
              </p>
            </div>
          </div>
          <button
            onClick={() => navigate('/admin/pending')}
            className="flex items-center gap-1.5 text-sm font-semibold
                        text-accent hover:underline font-body"
          >
            Review now <ArrowRight size={13} />
          </button>
        </div>
      )}

      {/* Recent registrations */}
      <div className="bg-white border border-border rounded-lg">
        <div className="flex items-center justify-between px-5 py-4
                         border-b border-border">
          <div className="flex items-center gap-2">
            <Users size={14} className="text-accent" />
            <h3 className="font-display text-sm font-semibold text-ink">
              Recent registrations
            </h3>
          </div>
          <button
            onClick={() => navigate('/admin/users')}
            className="text-xs text-text-muted hover:text-accent font-body
                        flex items-center gap-1 transition-colors"
          >
            All users <ArrowRight size={12} />
          </button>
        </div>

        <div className="divide-y divide-border">
          {data?.recentRegistrations?.length === 0
            ? (
              <p className="text-sm text-text-muted font-body text-center py-8">
                No registrations yet
              </p>
            )
            : data?.recentRegistrations?.map(u => (
              <div key={u.id}
                className="px-5 py-3.5 flex items-center gap-4">
                <div className="w-8 h-8 rounded-full bg-accent/10 flex
                                 items-center justify-center text-accent
                                 text-xs font-semibold font-mono shrink-0">
                  {u.fullName.charAt(0)}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-ink font-body">
                    {u.fullName}
                  </p>
                  <p className="text-xs text-text-muted font-body truncate">
                    {u.email}
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Badge value={u.role?.replace('ROLE_', '')} />
                  <Badge value={u.accountStatus} />
                </div>
                <span className="font-mono text-xs text-text-muted shrink-0">
                  {formatDate(u.createdAt)}
                </span>
              </div>
            ))
          }
        </div>
      </div>
    </div>
  )
}