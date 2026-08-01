import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useDispatch } from 'react-redux'
import { useNavigate, useLocation } from 'react-router-dom'
import { useLoginMutation } from './authApi'
import { setCredentials } from './authSlice'
import Input from '@/components/common/Input'
import Button from '@/components/common/Button'
import { Scale } from 'lucide-react'
import toast from 'react-hot-toast'

const schema = z.object({
  email:    z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})

export default function LoginPage() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const location  = useLocation()
  const from      = location.state?.from?.pathname ?? '/dashboard'

  const [login, { isLoading }] = useLoginMutation()

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema)
  })

  async function onSubmit(data) {
    try {
      const res = await login(data).unwrap()

      if (!res.approved) {
        // PENDING lawyer — redirect to waiting page
        navigate('/pending-approval')
        return
      }

      dispatch(setCredentials(res))
      toast.success(`Welcome back, ${res.fullName?.split(' ')[0]}`)
      navigate(from, { replace: true })

    } catch (err) {
      toast.error(err?.data?.error ?? 'Login failed. Try again.')
    }
  }

  function handleGoogleLogin() {
    // Redirect to Spring Boot OAuth2 endpoint
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <div>
      {/* Mobile logo — only shows when left panel is hidden */}
      <div className="flex items-center gap-2 mb-8 lg:hidden">
        <div className="w-8 h-8 rounded bg-ink flex items-center justify-center">
          <Scale size={15} className="text-accent" />
        </div>
        <span className="font-display font-semibold text-ink">LegalEase AI</span>
      </div>

      {/* Heading */}
      <div className="mb-8">
        <h2 className="font-display text-2xl font-semibold text-ink mb-1">
          Sign in
        </h2>
        <p className="text-sm text-text-muted font-body">
          Don't have an account?{' '}
          <a href="/register"
             className="text-accent font-semibold hover:underline">
            Register here
          </a>
        </p>
      </div>

      {/* Google OAuth button */}
      <button
        type="button"
        onClick={handleGoogleLogin}
        className="w-full h-10 flex items-center justify-center gap-3
                   border border-border rounded bg-white hover:bg-surface-alt
                   text-sm font-semibold font-body text-text
                   transition-colors duration-150 mb-6"
      >
        {/* Google SVG icon */}
        <svg width="16" height="16" viewBox="0 0 48 48">
          <path fill="#EA4335"
            d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38
               30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43
               13.72 17.74 9.5 24 9.5z"/>
          <path fill="#4285F4"
            d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58
               2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36
               7.09-17.65z"/>
          <path fill="#FBBC05"
            d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92
               16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
          <path fill="#34A853"
            d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15
               1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98
               6.19C6.51 42.62 14.62 48 24 48z"/>
          <path fill="none" d="M0 0h48v48H0z"/>
        </svg>
        Continue with Google
      </button>

      {/* Divider */}
      <div className="flex items-center gap-3 mb-6">
        <div className="flex-1 h-px bg-border" />
        <span className="text-xs text-text-muted font-body">or sign in with email</span>
        <div className="flex-1 h-px bg-border" />
      </div>

      {/* Email / password form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Email address"
          type="email"
          placeholder="rahul@example.com"
          error={errors.email?.message}
          {...register('email')}
        />

        <div className="space-y-1.5">
          <Input
            label="Password"
            type="password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password')}
          />
        </div>

        <Button
          type="submit"
          className="w-full mt-2"
          size="lg"
          loading={isLoading}
        >
          Sign in
        </Button>
      </form>

      {/* Delayed auth note */}
      <p className="text-xs text-text-muted font-body text-center mt-6">
        Browsing without an account?{' '}
        <a href="/" className="text-accent hover:underline">
          Explore the app first
        </a>
      </p>
    </div>
  )
}