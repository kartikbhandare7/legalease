import { Outlet } from 'react-router-dom'
import { Scale } from 'lucide-react'

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-ink flex">

      {/* Left panel — branding */}
      <div className="hidden lg:flex w-2/5 flex-col justify-between
                       p-12 border-r border-white/10">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded bg-accent flex items-center justify-center">
            <Scale size={18} className="text-ink" />
          </div>
          <div>
            <p className="text-white font-display font-semibold">LegalEase AI</p>
            <p className="text-white/30 text-xs font-body">
              Case management for independent lawyers
            </p>
          </div>
        </div>

        {/* Tagline */}
        <div>
          <blockquote className="font-display text-3xl text-white/90
                                  leading-snug mb-6">
            "Your practice, intelligently organised."
          </blockquote>
          <div className="space-y-3">
            {[
              'AI-powered case intake — type rough notes, get structured forms',
              'Hearing logs filled in seconds, not minutes',
              'PDF export for every case, client, and hearing',
            ].map(point => (
              <div key={point} className="flex items-start gap-2.5">
                <div className="w-1 h-1 rounded-full bg-accent mt-2 shrink-0" />
                <p className="text-white/50 text-sm font-body">{point}</p>
              </div>
            ))}
          </div>
        </div>

        <p className="text-white/20 text-xs font-body">
          © {new Date().getFullYear()} LegalEase AI
        </p>
      </div>

      {/* Right panel — form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-surface">
        <div className="w-full max-w-md">
          <Outlet />
        </div>
      </div>

    </div>
  )
}