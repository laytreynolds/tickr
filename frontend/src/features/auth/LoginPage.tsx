import { FormEvent, useState } from 'react'
import { apiClient, setAuthToken } from '../../lib/apiClient'

interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
}

interface LoginPageProps {
  onLoginSuccess: (auth: AuthResponse) => void
}

export function LoginPage({ onLoginSuccess }: LoginPageProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-tickr-600 text-sm font-bold text-white shadow-sm">
            T
          </div>
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900">
              Tickr
            </h1>
            <p className="text-xs text-slate-500">
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
              placeholder="+15551234567"
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

          <button
            type="submit"
            disabled={isSubmitting}
            className="flex w-full items-center justify-center rounded-lg bg-tickr-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-tickr-700 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </button>

          <p className="text-center text-[11px] text-slate-400">
            Use the phone number and password configured in your Tickr backend.
          </p>
        </form>
      </div>
    </div>
  )
}

