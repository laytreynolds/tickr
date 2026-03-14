import type { FormEvent } from 'react'

export interface CreateUserFormErrors {
  phoneNumber?: string
  password?: string
  confirmPassword?: string
}

interface CreateUserFormProps {
  phoneNumber: string
  password: string
  confirmPassword: string
  fieldErrors: CreateUserFormErrors
  error: string | null
  isSubmitting: boolean
  timezone: string
  onPhoneNumberChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onConfirmPasswordChange: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

const inputClasses =
  'block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100 dark:placeholder:text-slate-500 dark:ring-slate-600 dark:focus:border-tickr-400 dark:focus:ring-tickr-900/50'
const labelClasses =
  'mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400'

export function CreateUserForm({
  phoneNumber,
  password,
  confirmPassword,
  fieldErrors,
  error,
  isSubmitting,
  timezone,
  onPhoneNumberChange,
  onPasswordChange,
  onConfirmPasswordChange,
  onSubmit,
}: CreateUserFormProps) {
  return (
    <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-600 dark:bg-slate-800">
      <h2 className="mb-4 text-lg font-semibold text-slate-900 dark:text-slate-100">
        Create user
      </h2>

      <form className="max-w-md space-y-4" onSubmit={onSubmit}>
        <div>
          <label htmlFor="admin-phone" className={labelClasses}>
            Phone number
          </label>
          <input
            id="admin-phone"
            type="tel"
            inputMode="tel"
            autoComplete="tel"
            required
            placeholder="07585585585"
            className={inputClasses}
            value={phoneNumber}
            onChange={(e) => onPhoneNumberChange(e.target.value)}
            aria-invalid={fieldErrors.phoneNumber ? 'true' : undefined}
            aria-describedby={fieldErrors.phoneNumber ? 'admin-phone-error' : undefined}
          />
          {fieldErrors.phoneNumber && (
            <p id="admin-phone-error" className="mt-1 text-sm text-red-600 dark:text-red-400" role="alert">
              {fieldErrors.phoneNumber}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="admin-password" className={labelClasses}>
            Password
          </label>
          <input
            id="admin-password"
            type="password"
            autoComplete="new-password"
            required
            className={inputClasses}
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            aria-invalid={fieldErrors.password ? 'true' : undefined}
            aria-describedby={fieldErrors.password ? 'admin-password-error' : undefined}
          />
          {fieldErrors.password && (
            <p id="admin-password-error" className="mt-1 text-sm text-red-600 dark:text-red-400" role="alert">
              {fieldErrors.password}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="admin-confirm-password" className={labelClasses}>
            Confirm password
          </label>
          <input
            id="admin-confirm-password"
            type="password"
            autoComplete="new-password"
            required
            className={inputClasses}
            value={confirmPassword}
            onChange={(e) => onConfirmPasswordChange(e.target.value)}
            aria-invalid={fieldErrors.confirmPassword ? 'true' : undefined}
            aria-describedby={fieldErrors.confirmPassword ? 'admin-confirm-password-error' : undefined}
          />
          {fieldErrors.confirmPassword && (
            <p
              id="admin-confirm-password-error"
              className="mt-1 text-sm text-red-600 dark:text-red-400"
              role="alert"
            >
              {fieldErrors.confirmPassword}
            </p>
          )}
        </div>

        <p className="text-xs text-slate-500 dark:text-slate-400">
          Timezone: {timezone}
        </p>

        {error && (
          <p className="text-sm text-red-600 dark:text-red-400" role="alert">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center justify-center rounded-lg border border-tickr-500 bg-tickr-500 px-3 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-tickr-600 disabled:cursor-not-allowed disabled:opacity-70 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2 dark:ring-offset-slate-800"
        >
          {isSubmitting ? 'Creating…' : 'Create user'}
        </button>
      </form>
    </section>
  )
}
