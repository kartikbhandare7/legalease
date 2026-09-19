import { useSelector } from 'react-redux'
import { Navigate } from 'react-router-dom'

export default function PublicRoute({ children }) {
  const { isAuthenticated, user } = useSelector(s => s.auth)

  // Only redirect if authenticated AND account is actually active
  // This prevents stale localStorage tokens from blocking the register page
  if (isAuthenticated && user?.accountStatus === 'ACTIVE') {
    return <Navigate to="/dashboard" replace />
  }

  return children
}