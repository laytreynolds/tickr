import { type FormEvent, useMemo, useState } from 'react'
import axios from 'axios'
import { apiClient } from '../../lib/apiClient'
import { CreateUserForm, type CreateUserFormErrors } from '../../components/CreateUserForm'
import FadeIn from '../../components/FadeIn'

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
  if (trimmed.includes('@')) return 'Email is not supported. Use a phone number.'
  return null
}

export function AdminPage() {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<CreateUserFormErrors>({})

  const timezone = useMemo(() => getLocalTimezone(), [])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSuccessMessage(null)

    const nextFieldErrors: CreateUserFormErrors = {}
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
      await apiClient.post('/api/v1/user/adduser', {
        phoneNumber: phoneNumber.trim(),
        timezone,
        password,
      })

      setPhoneNumber('')
      setPassword('')
      setConfirmPassword('')
      setSuccessMessage('User created.')
    } catch (err: unknown) {
      let message = 'Could not create user. Please try again.'
      if (axios.isAxiosError(err)) {
        const status = err.response?.status
        if (status === 409) message = 'A user with that phone number already exists.'
        else if (status === 401) message = 'Session expired. Please sign in again.'
        else if (status === 400) message = 'Please check the details and try again.'
        else if (status != null && status >= 500) message = 'Server error. Please try again in a moment.'
      }

      setError(message)
      if (import.meta.env.DEV) {
        console.error('Create user failed', err)
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex flex-1 flex-col gap-6">
      <FadeIn>
      <header className="flex flex-col gap-4 border-b border-slate-200 pb-4 dark:border-slate-700 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-slate-100">
            Admin
          </h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Create user accounts. Only logged-in users can access this page.
          </p>
        </div>
      </header>
      </FadeIn>
      {successMessage && (
        <div
          role="status"
          className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-200"
        >
          {successMessage}
        </div>
      )}
      <FadeIn delay={0.2}>
        <CreateUserForm
          phoneNumber={phoneNumber}
          password={password}
          confirmPassword={confirmPassword}
          fieldErrors={fieldErrors}
          error={error}
          isSubmitting={isSubmitting}
          timezone={timezone}
          onPhoneNumberChange={setPhoneNumber}
          onPasswordChange={setPassword}
          onConfirmPasswordChange={setConfirmPassword}
          onSubmit={handleSubmit}
        />
      </FadeIn>
    </div>
  )
}
