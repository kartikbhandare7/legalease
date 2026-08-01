import { NavLink, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  LayoutDashboard, FolderOpen, Users,
  CalendarDays, ShieldCheck, LogOut, Scale
} from 'lucide-react'
import { logout } from '@/features/auth/authSlice'
import toast from 'react-hot-toast'

const lawyerNav = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard'  },
  { to: '/cases',     icon: FolderOpen,      label: 'Cases'       },
  { to: '/clients',   icon: Users,           label: 'Clients'     },
  { to: '/hearings',  icon: CalendarDays,    label: 'Hearings'    },
]

const adminNav = [
  { to: '/admin',         icon: LayoutDashboard, label: 'Overview'        },
  { to: '/admin/pending', icon: ShieldCheck,     label: 'Pending Lawyers' },
  { to: '/admin/users',   icon: Users,           label: 'All Users'       },
]

export default function Sidebar() {
  const { user } = useSelector(s => s.auth)
  const dispatch  = useNavigate()
  const navigate  = useNavigate()

  const isAdmin = user?.role === 'ROLE_ADMIN'
  const navItems = isAdmin ? adminNav : lawyerNav

  function handleLogout() {
    dispatch(logout())
    navigate('/login')
    toast.success('Logged out successfully')
  }

  return (
    <aside className="w-60 bg-ink flex flex-col h-full shrink-0">

      {/* Brand */}
      <div className="flex items-center gap-2.5 px-6 py-5 border-b border-white/10">
        <div className="w-8 h-8 rounded bg-accent flex items-center justify-center">
          <Scale size={16} className="text-ink" />
        </div>
        <div>
          <p className="text-white font-display font-semibold text-sm leading-tight">
            LegalEase
          </p>
          <p className="text-white/40 text-xs font-body">AI</p>
        </div>
      </div>

      {/* Role badge */}
      <div className="px-6 pt-4 pb-2">
        <span className="text-xs font-mono text-white/30 uppercase tracking-widest">
          {isAdmin ? 'Admin Panel' : user?.role?.replace('ROLE_', '')}
        </span>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 py-2 space-y-0.5">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/dashboard' || to === '/admin'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-sm text-sm font-body
               transition-all duration-150 group
               ${isActive
                 ? 'nav-active'
                 : 'text-white/60 hover:text-white hover:bg-white/5'
               }`
            }
          >
            <Icon size={16} className="shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* User info + logout */}
      <div className="border-t border-white/10 px-4 py-4">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-8 h-8 rounded-full bg-accent/20 flex items-center
                          justify-center text-accent text-xs font-semibold font-mono">
            {user?.fullName?.charAt(0) ?? 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-white text-xs font-semibold truncate">
              {user?.fullName ?? 'User'}
            </p>
            <p className="text-white/40 text-xs truncate">{user?.email}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-white/40 hover:text-danger
                     text-xs font-body transition-colors w-full px-1 py-1"
        >
          <LogOut size={13} />
          Sign out
        </button>
      </div>

    </aside>
  )
}