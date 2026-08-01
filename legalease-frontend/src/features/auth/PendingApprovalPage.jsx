import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { logout } from './authSlice'
import { Clock, Scale, Mail } from 'lucide-react'
import Button from '@/components/common/Button'

export default function PendingApprovalPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()

  function handleLogout() {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-ink flex items-center justify-center p-8">
      <div className="bg-surface rounded-xl shadow-lg p-10 max-w-md w-full
                       text-center">

        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="w-8 h-8 rounded bg-ink flex items-center justify-center">
            <Scale size={15} className="text-accent" />
          </div>
          <span className="font-display font-semibold text-ink">
            LegalEase AI
          </span>
        </div>

        {/* Icon */}
        <div className="w-16 h-16 rounded-full bg-accent/10 flex items-center
                         justify-center mx-auto mb-5">
          <Clock size={28} className="text-accent" />
        </div>

        {/* Content */}
        <h2 className="font-display text-xl font-semibold text-ink mb-2">
          Verification in progress
        </h2>
        <p className="text-sm text-text-muted font-body leading-relaxed mb-6">
          Your enrollment certificate has been submitted and is currently
          being reviewed by our team. You'll receive access once verified.
        </p>

        {/* Steps */}
        <div className="space-y-3 text-left mb-8">
          {[
            { done: true,  label: 'Account registered'              },
            { done: true,  label: 'Certificate uploaded'            },
            { done: false, label: 'Admin verification — in progress'},
            { done: false, label: 'Account activated'               },
          ].map((step, i) => (
            <div key={i} className="flex items-center gap-3">
              <div className={`
                w-5 h-5 rounded-full flex items-center justify-center
                shrink-0 text-xs font-bold
                ${step.done
                  ? 'bg-success text-white'
                  : 'bg-border text-text-muted'
                }
              `}>
                {step.done ? '✓' : i + 1}
              </div>
              <span className={`text-sm font-body
                ${step.done ? 'text-text' : 'text-text-muted'}`}>
                {step.label}
              </span>
            </div>
          ))}
        </div>

        {/* Contact note */}
        <div className="flex items-start gap-2.5 p-3 rounded
                         bg-surface-alt border border-border mb-6 text-left">
          <Mail size={14} className="text-text-muted mt-0.5 shrink-0" />
          <p className="text-xs text-text-muted font-body">
            Typically takes 1–2 business days. Contact{' '}
            <a href="mailto:admin@legalease.com"
               className="text-accent hover:underline">
              admin@legalease.com
            </a>
            {' '}if you've been waiting longer.
          </p>
        </div>

        <Button
          variant="secondary"
          className="w-full"
          onClick={handleLogout}
        >
          Sign out
        </Button>
      </div>
    </div>
  )
}