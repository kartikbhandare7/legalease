import { useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { Bell } from 'lucide-react'

const pageTitles = {
  '/dashboard': 'Dashboard',
  '/cases':     'Cases',
  '/clients':   'Clients',
  '/hearings':  'Hearings',
  '/admin':     'Admin Overview',
  '/admin/pending': 'Pending Approvals',
  '/admin/users':   'All Users',
}

export default function Topbar() {
  const { pathname } = useLocation()
  const { user }     = useSelector(s => s.auth)

  // Match exact or prefix
  const title = Object.entries(pageTitles)
    .find(([path]) => pathname === path || pathname.startsWith(path + '/'))
    ?.[1] ?? 'LegalEase AI'

  return (
    <header className="h-14 bg-surface border-b border-border
                        flex items-center justify-between px-8 shrink-0">
      <div>
        <h1 className="font-display text-base font-semibold text-ink">
          {title}
        </h1>
      </div>

      <div className="flex items-center gap-4">
        {/* Notification bell — placeholder for v2 */}
        <button className="text-text-muted hover:text-ink transition-colors">
          <Bell size={16} />
        </button>

        <div className="h-5 w-px bg-border" />

        <div className="text-right">
          <p className="text-xs font-semibold text-ink leading-none">
            {user?.fullName}
          </p>
          <p className="text-xs text-text-muted mt-0.5">
            {new Date().toLocaleDateString('en-IN', {
              weekday: 'short', day: 'numeric', month: 'short'
            })}
          </p>
        </div>
      </div>
    </header>
  )
}