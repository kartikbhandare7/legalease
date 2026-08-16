import { useSelector } from 'react-redux'
import { Navigate, useLocation } from 'react-router-dom'

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, user } = useSelector(s => s.auth)
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // Admins are always ACTIVE — never redirect them to pending
  if (user?.role !== 'ROLE_ADMIN' && user?.accountStatus === 'PENDING') {
    return <Navigate to="/pending-approval" replace />
  }

  return children
}
