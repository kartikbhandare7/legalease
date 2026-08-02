import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from '@/components/layout/AppLayout'
import AuthLayout from '@/components/layout/AuthLayout'
import ProtectedRoute from '@/routes/ProtectedRoute'
import RoleRoute from '@/routes/RoleRoute'
import PublicRoute from '@/routes/PublicRoute'

// Auth pages
import LoginPage from '@/features/auth/LoginPage'
import RegisterPage from '@/features/auth/RegisterPage'
import PendingApprovalPage from '@/features/auth/PendingApprovalPage'

// App pages
import DashboardPage from '@/features/dashboard/DashboardPage'
import CasesPage from '@/features/cases/CasesPage'
import CaseDetailPage from '@/features/cases/CaseDetailPage'
import ClientsPage from '@/features/clients/ClientsPage'
import ClientDetailPage from '@/features/clients/ClientDetailPage'
import HearingsPage from '@/features/hearings/HearingsPage'

// Admin pages
import AdminDashboardPage from '@/features/admin/AdminDashboardPage'
import PendingLawyersPage from '@/features/admin/PendingLawyersPage'
import AllUsersPage from '@/features/admin/AllUsersPage'

// OAuth2 callback handler
import OAuth2Callback from '@/features/auth/OAuth2Callback'

// new inports 
import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { refreshCurrentUser } from '@/features/auth/authSlice'

export default function App() {
  const dispatch = useDispatch()
const { isAuthenticated } = useSelector(s => s.auth)

useEffect(() => {
  if (isAuthenticated) {
    dispatch(refreshCurrentUser())
  }
}, [])  
  return (
    <Routes>

      {/* Public routes — delayed auth gate */}
      <Route element={<AuthLayout />}>
        <Route path="/login"    element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
        <Route path="/pending-approval" element={<PendingApprovalPage />} />
        <Route path="/oauth2/callback"  element={<OAuth2Callback />} />
      </Route>

      {/* Protected app routes */}
      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/cases"           element={<CasesPage />} />
        <Route path="/cases/:caseId"   element={<CaseDetailPage />} />
        <Route path="/clients"         element={<ClientsPage />} />
        <Route path="/clients/:clientId" element={<ClientDetailPage />} />
        <Route path="/hearings"        element={<HearingsPage />} />

        {/* Admin only routes */}
        <Route path="/admin" element={<RoleRoute role="ROLE_ADMIN" />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="pending" element={<PendingLawyersPage />} />
          <Route path="users"   element={<AllUsersPage />} />
        </Route>
      </Route>

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />

    </Routes>
  )
}