import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { useRegisterMutation } from './authApi'
import { setCredentials } from './authSlice'
import Input from '@/components/common/Input'
import Button from '@/components/common/Button'
import { Scale, Upload, CheckCircle2, X } from 'lucide-react'
import toast from 'react-hot-toast'

// ── Validation schemas — one per role ─────────────────────────────────────────

const lawyerSchema = z.object({
  fullName:         z.string().min(2, 'Full name is required'),
  email:            z.string().email('Enter a valid email'),
  password:         z.string().min(8, 'Password must be at least 8 characters'),
  role:             z.literal('ROLE_LAWYER'),
  barCouncilNumber: z.string().min(1, 'Bar Council number is required'),
})

const clerkSchema = z.object({
  fullName:     z.string().min(2, 'Full name is required'),
  email:        z.string().email('Enter a valid email'),
  password:     z.string().min(8, 'Password must be at least 8 characters'),
  role:         z.literal('ROLE_CLERK'),
  referralCode: z.string().min(1, 'Referral code from your lawyer is required'),
})

const schemaMap = {
  ROLE_LAWYER: lawyerSchema,
  ROLE_CLERK:  clerkSchema,
}

export default function RegisterPage() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()

  const [role,        setRole]        = useState('ROLE_LAWYER')
  const [certificate, setCertificate] = useState(null)
  const [certError,   setCertError]   = useState('')

  const [register, { isLoading }] = useRegisterMutation()

  const { register: field, handleSubmit, reset,
          formState: { errors } } = useForm({
    resolver: zodResolver(schemaMap[role]),
    defaultValues: { role: 'ROLE_LAWYER' }
  })

  // Switch role — reset form to clear stale errors
  function switchRole(newRole) {
    setRole(newRole)
    setCertificate(null)
    setCertError('')
    reset({ role: newRole })
  }

  function handleCertUpload(e) {
    const file = e.target.files?.[0]
    if (!file) return

    const allowed = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg']
    if (!allowed.includes(file.type)) {
      setCertError('Only PDF or image files accepted')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      setCertError('File must be under 10MB')
      return
    }

    setCertificate(file)
    setCertError('')
  }

  function removeCertificate() {
    setCertificate(null)
    setCertError('')
  }

  async function onSubmit(data) {
    // Lawyer must upload certificate
    if (role === 'ROLE_LAWYER' && !certificate) {
      setCertError('Please upload your Bar Council enrollment certificate')
      return
    }

    try {
      // Build multipart form — Spring Boot expects form-data
      const formData = new FormData()
      formData.append('data', new Blob(
        [JSON.stringify({ ...data, role })],
        { type: 'application/json' }
      ))
      if (certificate) {
        formData.append('certificate', certificate)
      }

      const res = await register(formData).unwrap()

      if (!res.approved) {
        // Lawyer — goes to pending page, no token yet
        navigate('/pending-approval')
        return
      }

      // Clerk — auto-approved, gets token immediately
      dispatch(setCredentials(res))
      toast.success('Account created! Welcome to LegalEase.')
      navigate('/dashboard')

    } catch (err) {
      toast.error(err?.data?.error ?? 'Registration failed. Try again.')
    }
  }

  function handleGoogleLogin() {
    window.location.href =
      `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <div>
      {/* Mobile logo */}
      <div className="flex items-center gap-2 mb-8 lg:hidden">
        <div className="w-8 h-8 rounded bg-ink flex items-center justify-center">
          <Scale size={15} className="text-accent" />
        </div>
        <span className="font-display font-semibold text-ink">LegalEase AI</span>
      </div>

      {/* Heading */}
      <div className="mb-6">
        <h2 className="font-display text-2xl font-semibold text-ink mb-1">
          Create account
        </h2>
        <p className="text-sm text-text-muted font-body">
          Already registered?{' '}
          <a href="/login"
             className="text-accent font-semibold hover:underline">
            Sign in
          </a>
        </p>
      </div>

      {/* Role selector */}
      <div className="flex rounded border border-border bg-surface-alt
                       p-0.5 mb-6">
        {[
          { value: 'ROLE_LAWYER', label: 'I am a Lawyer' },
          { value: 'ROLE_CLERK',  label: 'I am a Clerk'  },
        ].map(opt => (
          <button
            key={opt.value}
            type="button"
            onClick={() => switchRole(opt.value)}
            className={`
              flex-1 py-2 text-sm font-semibold font-body rounded
              transition-all duration-150
              ${role === opt.value
                ? 'bg-ink text-white shadow-sm'
                : 'text-text-muted hover:text-text'
              }
            `}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {/* Google OAuth */}
      <button
        type="button"
        onClick={handleGoogleLogin}
        className="w-full h-10 flex items-center justify-center gap-3
                   border border-border rounded bg-white hover:bg-surface-alt
                   text-sm font-semibold font-body text-text
                   transition-colors duration-150 mb-5"
      >
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
      <div className="flex items-center gap-3 mb-5">
        <div className="flex-1 h-px bg-border" />
        <span className="text-xs text-text-muted font-body">
          or register with email
        </span>
        <div className="flex-1 h-px bg-border" />
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

        <Input
          label="Full name"
          placeholder={role === 'ROLE_LAWYER'
            ? 'Adv. Rahul Sharma' : 'Priya Mehta'}
          error={errors.fullName?.message}
          {...field('fullName')}
        />

        <Input
          label="Email address"
          type="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...field('email')}
        />

        <Input
          label="Password"
          type="password"
          placeholder="Min 8 characters"
          error={errors.password?.message}
          {...field('password')}
        />

        {/* Lawyer-only fields */}
        {role === 'ROLE_LAWYER' && (
          <>
            <Input
              label="Bar Council enrollment number"
              placeholder="e.g. MH/1234/2020"
              error={errors.barCouncilNumber?.message}
              hint="As printed on your Bar Council certificate"
              {...field('barCouncilNumber')}
            />

            {/* Certificate upload */}
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-text-muted
                                 uppercase tracking-wide font-body">
                Enrollment certificate
              </label>

              {certificate ? (
                /* File selected — show name + remove button */
                <div className="flex items-center gap-3 px-3 py-2.5 rounded
                                 border border-success/40 bg-success/5">
                  <CheckCircle2 size={15} className="text-success shrink-0" />
                  <span className="text-sm font-body text-text flex-1 truncate">
                    {certificate.name}
                  </span>
                  <button
                    type="button"
                    onClick={removeCertificate}
                    className="text-text-muted hover:text-danger transition-colors"
                  >
                    <X size={14} />
                  </button>
                </div>
              ) : (
                /* Upload zone */
                <label className="flex flex-col items-center justify-center
                                   gap-2 px-4 py-6 border-2 border-dashed
                                   border-border rounded cursor-pointer
                                   hover:border-accent/50 hover:bg-accent-soft/20
                                   transition-all duration-150">
                  <Upload size={18} className="text-text-muted" />
                  <div className="text-center">
                    <p className="text-sm font-semibold font-body text-text">
                      Click to upload certificate
                    </p>
                    <p className="text-xs text-text-muted font-body mt-0.5">
                      PDF, JPG or PNG · Max 10MB
                    </p>
                  </div>
                  <input
                    type="file"
                    className="hidden"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={handleCertUpload}
                  />
                </label>
              )}

              {certError && (
                <p className="text-xs text-danger font-body">{certError}</p>
              )}
            </div>
          </>
        )}

        {/* Clerk-only fields */}
        {role === 'ROLE_CLERK' && (
          <Input
            label="Lawyer referral code"
            placeholder="e.g. LAW-A1B2C3"
            error={errors.referralCode?.message}
            hint="Get this code from the lawyer you work with"
            {...field('referralCode')}
          />
        )}

        <Button
          type="submit"
          className="w-full mt-2"
          size="lg"
          loading={isLoading}
        >
          {role === 'ROLE_LAWYER'
            ? 'Submit for approval'
            : 'Create account'
          }
        </Button>

      </form>

      {/* Lawyer approval note */}
      {role === 'ROLE_LAWYER' && (
        <div className="mt-5 p-3 rounded bg-surface-alt border border-border">
          <p className="text-xs text-text-muted font-body leading-relaxed">
            <span className="font-semibold text-text">Note:</span>{' '}
            Lawyer accounts are reviewed manually. You'll be notified once
            your enrollment certificate is verified. This usually takes
            1–2 business days.
          </p>
        </div>
      )}
    </div>
  )
}