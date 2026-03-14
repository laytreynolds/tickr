import { type FormEvent, useMemo, useState } from 'react'
import axios from 'axios'
import { apiClient } from '../../lib/apiClient'
import logo from '../../assets/logo.png'

interface RegisterPageProps {
  onBackToLogin: (notice?: string) => void
}

type RegisterValidationErrors = Partial<{
  phoneNumber: string
  password: string
  confirmPassword: string
}>

function getLocalTimezone(): string {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
  } catch {
    return 'UTC'
  }
}

function validatePhoneNumber(value: string): string | null {
  const trimmed = value.trim()
  if (!trimmed) return 'Phone number is required.'
  if (trimmed.includes('@')) return 'Email registration is not supported yet. Use a phone number.'
  return null
}

export function RegisterPage({ onBackToLogin }: RegisterPageProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<RegisterValidationErrors>({})

  const timezone = useMemo(() => getLocalTimezone(), [])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)

    const nextFieldErrors: RegisterValidationErrors = {}
    const phoneNumberError = validatePhoneNumber(phoneNumber)
    if (phoneNumberError) nextFieldErrors.phoneNumber = phoneNumberError

    const trimmedPassword = password
    if (trimmedPassword.length < 8) {
      nextFieldErrors.password = 'Password must be at least 8 characters.'
    }
    if (trimmedPassword !== confirmPassword) {
      nextFieldErrors.confirmPassword = 'Passwords do not match.'
    }

    setFieldErrors(nextFieldErrors)
    if (Object.keys(nextFieldErrors).length > 0) return

    setIsSubmitting(true)
    try {
      // Backend currently supports creating users by phone number + timezone + password.
      await apiClient.post('/api/v1/user/adduser', {
        phoneNumber: phoneNumber.trim(),
        timezone,
        password,
      })

      setPassword('')
      setConfirmPassword('')
      onBackToLogin('Account created. Please sign in.')
    } catch (err: unknown) {
      let message = 'Could not create your account. Please try again.'
      if (axios.isAxiosError(err)) {
        const status = err.response?.status
        if (status === 409) message = 'An account with that phone number already exists.'
        else if (status === 400) message = 'Please check your details and try again.'
        else if (status && status >= 500) message = 'Server error. Please try again in a moment.'
      }

      setError(message)
      if (import.meta.env.DEV) {
        console.error('Registration failed', err)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-6 flex items-center gap-3">
          <img src={logo} alt="Tickr" className="h-12 w-12 rounded-lg object-contain" />
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900">Tickr</h1>
            <p className="text-xs text-slate-500">Create an account to manage reminders.</p>
          </div>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label
              htmlFor="register-phone"
              className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
            >
              Phone number
            </label>
            <input
              id="register-phone"
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              required
              placeholder="+15551234567"
              className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              aria-invalid={fieldErrors.phoneNumber ? 'true' : undefined}
              aria-describedby={fieldErrors.phoneNumber ? 'register-phone-error' : undefined}
            />
            {fieldErrors.phoneNumber && (
              <p id="register-phone-error" className="mt-1 text-sm text-red-600" role="alert">
                {fieldErrors.phoneNumber}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="register-password"
              className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
            >
              Password
            </label>
            <input
              id="register-password"
              type="password"
              autoComplete="new-password"
              required
              className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              aria-invalid={fieldErrors.password ? 'true' : undefined}
              aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
            />
            {fieldErrors.password && (
              <p id="register-password-error" className="mt-1 text-sm text-red-600" role="alert">
                {fieldErrors.password}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="register-confirm-password"
              className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
            >
              Confirm password
            </label>
            <input
              id="register-confirm-password"
              type="password"
              autoComplete="new-password"
              required
              className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              aria-invalid={fieldErrors.confirmPassword ? 'true' : undefined}
              aria-describedby={fieldErrors.confirmPassword ? 'register-confirm-password-error' : undefined}
            />
            {fieldErrors.confirmPassword && (
              <p
                id="register-confirm-password-error"
                className="mt-1 text-sm text-red-600"
                role="alert"
              >
                {fieldErrors.confirmPassword}
              </p>
            )}
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
            {isSubmitting ? 'Creating account…' : 'Create account'}
          </button>

          <div className="flex items-center justify-between">
            <p className="text-[11px] text-slate-400">Timezone: {timezone}</p>
            <button
              type="button"
              onClick={() => onBackToLogin()}
              className="text-xs font-medium text-tickr-700 hover:text-tickr-800"
            >
              Back to sign in
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

