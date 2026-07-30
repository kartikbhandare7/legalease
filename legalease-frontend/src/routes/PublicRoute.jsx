import { useSelector } from 'react-redux'
import { Navigate } from 'react-router-dom'

// Logged-in users shouldn't see login/register
export default function PublicRoute({ children }) {
  const { isAuthenticated } = useSelector(s => s.auth)
  return isAuthenticated
    ? <Navigate to="/dashboard" replace />
    : children
}