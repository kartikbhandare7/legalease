import { useSelector } from 'react-redux'
import { Navigate, Outlet } from 'react-router-dom'

export default function RoleRoute({ role }) {
  const { user } = useSelector(s => s.auth)

  if (user?.role !== role) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}