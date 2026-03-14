import { type FormEvent, useState } from 'react'
import { apiClient, setAuthToken } from '../../lib/apiClient'
import logo from '../../assets/logo.png'
import { ThemeToggle } from '../../components/ThemeToggle'

interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
}

interface LoginPageProps {
  onLoginSuccess: (auth: AuthResponse) => void
}

type HealthStatus = 'idle' | 'checking' | 'ok' | 'error'

export function LoginPage({ onLoginSuccess }: LoginPageProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [healthStatus, setHealthStatus] = useState<HealthStatus>('idle')

  const checkBackendHealth = async () => {
    setHealthStatus('checking')
    try {
      const { data } = await apiClient.get<string>('/api/v1/ping')
      if (data === 'pong') {
        setHealthStatus('ok')
      } else {
        setHealthStatus('error')
      }
    } catch {
      setHealthStatus('error')
    }
    // Reset status after a few seconds so user can try again
    setTimeout(() => setHealthStatus('idle'), 4000)
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)

    try {
      const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/login', {
        phoneNumber,
        password,
      })

      setAuthToken(data.accessToken)
      try {
        window.localStorage.setItem('tickr_auth', JSON.stringify(data))
      } catch {
        // ignore storage errors
      }

      onLoginSuccess(data)
    } catch (err: unknown) {
      setError('Invalid phone number or password. Please try again.')
      if (import.meta.env.DEV) {
        console.error('Login failed', err)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-slate-50 px-4 dark:bg-slate-900">
      <div className="absolute right-4 top-4">
        <ThemeToggle />
      </div>
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-600 dark:bg-slate-800">
        <div className="mb-6 flex items-center gap-3">
          <img src={logo} alt="Tickr" className="h-12 w-12 rounded-lg object-contain" />
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">
              Tickr
            </h1>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Sign in to manage your reminders.
            </p>
          </div>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="login-phone" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Phone number
            </label>
            <input
              id="login-phone"
              type="tel"
              autoComplete="tel"
              required
              placeholder="07585585585"
              className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
            />
          </div>

          <div>
            <label htmlFor="login-password" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Password
            </label>
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              required
              className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {error && (
            <p className="text-sm text-red-600" role="alert">
              {error}
            </p>
          )}

          <div className="flex flex-row gap-3">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex flex-1 items-center justify-center rounded-lg bg-tickr-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-tickr-700 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isSubmitting ? 'Signing in…' : 'Sign in'}
            </button>
          </div>

          <div className="space-y-2">
            <div className="flex flex-col items-center gap-1">
              <button
                type="button"
                onClick={checkBackendHealth}
                disabled={healthStatus === 'checking'}
                className="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-70"
                aria-label="Check backend health"
              >
                {healthStatus === 'checking' ? 'Checking…' : 'Check backend health'}
              </button>
              {healthStatus === 'ok' && (
                <p className="text-xs text-emerald-600" role="status">
                  Backend is reachable.
                </p>
              )}
              {healthStatus === 'error' && (
                <p className="text-xs text-red-600" role="alert">
                  Backend unreachable.
                </p>
              )}
            </div>
            <p className="text-center text-[11px] text-slate-400">
              Use the phone number and password configured in your Tickr backend.
            </p>
          </div>
        </form>
      </div>
    </div>
  )
}

