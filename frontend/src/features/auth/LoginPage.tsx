import { type FormEvent, useEffect, useState } from 'react'
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

type HealthStatus = 'idle'| 'checking' | 'up' | 'down'

export function LoginPage({ onLoginSuccess }: LoginPageProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [healthStatus, setHealthStatus] = useState<HealthStatus>('idle')
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [snackbarSeverity, setSnackbarSeverity] = useState<'success' | 'error'>('success');

  useEffect(() => {
    if (!snackbarOpen) return
    const t = setTimeout(() => setSnackbarOpen(false), 4000)
    return () => clearTimeout(t)
  }, [snackbarOpen])

  const checkBackendHealth = async () => {
    setHealthStatus('checking')
    try {
      const { data } = await apiClient.get<string>('/health')
      if (data === 'OK') {
        setHealthStatus('up');
        setSnackbarMessage('Backend is up');
        setSnackbarSeverity('success');
        setSnackbarOpen(true);
        setTimeout(() => setHealthStatus('idle'), 4000);
      } else {
        setHealthStatus('down');
        setSnackbarMessage('Backend is down');
        setSnackbarSeverity('error');
        setSnackbarOpen(true);
        setTimeout(() => setHealthStatus('idle'), 4000);
      }
    } catch {
      setHealthStatus('down');
      setSnackbarMessage('Backend is down');
      setSnackbarSeverity('error');
      setSnackbarOpen(true);
      setTimeout(() => setHealthStatus('idle'), 4000);
    }
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
      <div className="absolute right-4 top-4 flex items-center gap-3">
        <ThemeToggle />
        <button
          type="button"
          onClick={checkBackendHealth}
          disabled={healthStatus === 'checking'}
          aria-label="Check backend health"
          className={[
            'inline-flex items-center gap-1.5 rounded-lg border bg-white px-3 py-1.5 text-sm font-medium transition-colors disabled:pointer-events-none disabled:opacity-70 dark:bg-slate-800',
            healthStatus === 'up' &&
              'border-emerald-500 text-emerald-600 dark:border-emerald-400 dark:text-emerald-400',
            healthStatus === 'down' &&
              'border-red-500 text-red-600 dark:border-red-400 dark:text-red-400',
            (healthStatus === 'idle' || healthStatus === 'checking') &&
              'border-slate-300 text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-700',
          ]
            .filter(Boolean)
            .join(' ')}
        >
          {healthStatus === 'checking' && (
            <svg
              className="h-4 w-4 animate-spin"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              aria-hidden
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
          )}
          {healthStatus === 'idle' && 'Check backend health'}
          {healthStatus === 'checking' && 'Checking...'}
          {healthStatus === 'up' && 'Backend up'}
          {healthStatus === 'down' && 'Backend down'}
        </button>
      </div>

      {snackbarOpen && (
        <div
          role="alert"
          className={`fixed left-1/2 top-4 z-50 w-full max-w-sm -translate-x-1/2 rounded-lg px-4 py-3 shadow-lg ${
            snackbarSeverity === 'success'
              ? 'bg-emerald-600 text-white dark:bg-emerald-500'
              : 'bg-red-600 text-white dark:bg-red-500'
          }`}
        >
          {snackbarMessage}
        </div>
      )}
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

          <p className="text-center text-[11px] text-slate-400">
            Use the phone number and password configured in your Tickr backend.
          </p>
        </form>
      </div>
    </div>
  )
}

