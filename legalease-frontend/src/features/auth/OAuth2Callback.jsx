import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { setCredentials } from './authSlice'

// Google redirects here after OAuth — grabs token from URL param
export default function OAuth2Callback() {
  const [params]   = useSearchParams()
  const navigate   = useNavigate()
  const dispatch   = useDispatch()

  useEffect(() => {
    const token = params.get('token')
    if (token) {
      // Decode payload from JWT to get user info
      const payload = JSON.parse(atob(token.split('.')[1]))
      dispatch(setCredentials({
        token,
        email:         payload.sub,
        role:          payload.role,
        accountStatus: 'ACTIVE'
      }))
      navigate('/dashboard', { replace: true })
    } else {
      navigate('/pending-approval', { replace: true })
    }
  }, [])

  return <div className="flex items-center justify-center h-screen">
    <p className="text-text-muted font-body">Signing you in...</p>
  </div>
}