import { useSelector } from 'react-redux'
import { Navigate, useLocation } from 'react-router-dom'

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, user } = useSelector(s => s.auth)
  const location = useLocation()

  if (!isAuthenticated) {
    // Save intended destination — redirect back after login
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // Approved check — PENDING lawyers see waiting page
  if (user?.accountStatus === 'PENDING') {
    return <Navigate to="/pending-approval" replace />
  }

  return children
}