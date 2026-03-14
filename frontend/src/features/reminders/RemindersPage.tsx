import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { Reminder, ReminderChannel, ReminderStatus } from './types'
import { useDeleteReminder, useReminders } from './api'

type StatusFilter = ReminderStatus | 'ALL'
type ChannelFilter = ReminderChannel | 'ALL'

interface FilterState {
  search: string
  status: StatusFilter
  channel: ChannelFilter
}

const STATUS_OPTIONS: StatusFilter[] = ['ALL', 'PENDING', 'SENT', 'FAILED']
const CHANNEL_OPTIONS: ChannelFilter[] = ['ALL', 'SMS', 'PHONE', 'EMAIL']

export function RemindersPage() {
  const [filters, setFilters] = useState<FilterState>({
    search: '',
    status: 'ALL',
    channel: 'ALL',
  })
  const [reminderToDelete, setReminderToDelete] = useState<Reminder | null>(
    null,
  )

  const remindersQuery = useReminders()
  const deleteReminder = useDeleteReminder()
  const cancelButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (reminderToDelete) {
      cancelButtonRef.current?.focus()
    }
  }, [reminderToDelete])

  useEffect(() => {
    if (!reminderToDelete) return
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        setReminderToDelete(null)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [reminderToDelete])

  const filteredReminders = useMemo(() => {
    if (!remindersQuery.data) return []

    return [...remindersQuery.data]
      .sort(
        (a, b) =>
          new Date(a.remindAt).getTime() - new Date(b.remindAt).getTime(),
      )
      .filter((reminder) => {
        const matchesStatus =
          filters.status === 'ALL' || reminder.status === filters.status
        const matchesChannel =
          filters.channel === 'ALL' || reminder.channel === filters.channel

        const terms = filters.search
          .trim()
          .toLowerCase()
          .split(/\s+/)
          .filter(Boolean)
        if (terms.length === 0) return matchesStatus && matchesChannel

        const fields = [
          reminder.event?.name,
          reminder.event?.description,
          reminder.user?.name,
          reminder.user?.email,
          reminder.user?.phoneNumber,
          reminder.channel,
          reminder.status,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()

        const matchesSearch = terms.every((term) => fields.includes(term))
        return matchesStatus && matchesChannel && matchesSearch
      })
  }, [filters, remindersQuery.data])

  const handleDeleteClick = useCallback((reminder: Reminder) => {
    setReminderToDelete(reminder)
  }, [])

  const handleConfirmDelete = useCallback(() => {
    if (!reminderToDelete) return
    deleteReminder.mutate(reminderToDelete.id)
    setReminderToDelete(null)
  }, [reminderToDelete, deleteReminder])

  const handleCancelDelete = useCallback(() => {
    setReminderToDelete(null)
  }, [])

  const showEmptyState =
    !remindersQuery.isLoading &&
    !remindersQuery.isError &&
    remindersQuery.data &&
    remindersQuery.data.length === 0

  return (
    <div className="flex flex-1 flex-col gap-6">
      <header className="flex flex-col gap-4 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">
            Reminders
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Search, filter, and manage outgoing reminders from Tickr.
          </p>
        </div>
        <button
          type="button"
          onClick={() => remindersQuery.refetch()}
          className="inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={remindersQuery.isFetching}
        >
          {remindersQuery.isFetching ? (
            <span className="inline-flex items-center gap-2">
              <span className="h-3 w-3 animate-spin rounded-full border-2 border-slate-400 border-t-transparent" />
              Refreshing…
            </span>
          ) : (
            'Refresh'
          )}
        </button>
      </header>

      <section className="flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
        <div className="flex-1 min-w-[220px]">
          <label htmlFor="reminders-search" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
            Search
          </label>
          <input
            id="reminders-search"
            type="search"
            placeholder="Search by event, user, email, phone…"
            className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
            value={filters.search}
            onChange={(e) =>
              setFilters((prev) => ({ ...prev, search: e.target.value }))
            }
          />
        </div>

        <div>
          <label htmlFor="reminders-status" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
            Status
          </label>
          <select
            id="reminders-status"
            className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
            value={filters.status}
            onChange={(e) =>
              setFilters((prev) => ({
                ...prev,
                status: e.target.value as StatusFilter,
              }))
            }
          >
            {STATUS_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option === 'ALL' ? 'All statuses' : option}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="reminders-channel" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
            Channel
          </label>
          <select
            id="reminders-channel"
            className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
            value={filters.channel}
            onChange={(e) =>
              setFilters((prev) => ({
                ...prev,
                channel: e.target.value as ChannelFilter,
              }))
            }
          >
            {CHANNEL_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option === 'ALL' ? 'All channels' : option}
              </option>
            ))}
          </select>
        </div>
      </section>

      <section className="flex-1 rounded-xl border border-slate-200 bg-white shadow-sm">
        {remindersQuery.isLoading ? (
          <div className="flex h-64 items-center justify-center">
            <div className="flex flex-col items-center gap-2 text-slate-500">
              <span className="h-6 w-6 animate-spin rounded-full border-2 border-slate-300 border-t-tickr-500" />
              <span className="text-sm">Loading reminders…</span>
            </div>
          </div>
        ) : remindersQuery.isError ? (
          <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
            <p className="text-sm font-medium text-red-600">
              Failed to load reminders
            </p>
            <p className="max-w-md text-xs text-slate-500">
              Please ensure the Tickr backend is running on{' '}
              <code className="rounded bg-slate-100 px-1 py-0.5 text-[11px]">
                http://localhost:8080
              </code>{' '}
              (or update <code>VITE_API_BASE_URL</code>) and try again.
            </p>
          </div>
        ) : showEmptyState ? (
          <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
            <p className="text-sm font-medium text-slate-900">
              No reminders yet
            </p>
            <p className="max-w-md text-xs text-slate-500">
              When your backend starts scheduling reminders, they will appear
              here in real time.
            </p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-xl">
            <div className="hidden md:block">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Event / User
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Remind at
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Channel
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Status
                    </th>
                    <th scope="col" className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {filteredReminders.map((reminder) => (
                    <tr key={reminder.id} className="hover:bg-slate-50/60">
                      <td className="px-4 py-3 text-sm text-slate-900">
                        <div className="font-medium">
                          {reminder.event?.name || 'Untitled event'}
                        </div>
                        <div className="mt-0.5 text-xs text-slate-500">
                          {reminder.user?.name || 'Unknown user'}
                          {reminder.user?.email
                            ? ` • ${reminder.user.email}`
                            : ''}
                          {reminder.user?.phoneNumber
                            ? ` • ${reminder.user.phoneNumber}`
                            : ''}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-900">
                        <div>{formatDateTime(reminder.remindAt)}</div>
                        <div className="mt-0.5 text-xs text-slate-500">
                          Created {formatDateTime(reminder.createdAt)}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <ChannelBadge channel={reminder.channel} />
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <StatusBadge status={reminder.status} />
                      </td>
                      <td className="px-4 py-3 text-right text-sm">
                        <button
                          type="button"
                          onClick={() => handleDeleteClick(reminder)}
                          className="inline-flex items-center rounded-md border border-red-100 bg-red-50 px-3 py-1 text-xs font-medium text-red-700 shadow-sm transition-colors hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-200"
                          disabled={deleteReminder.isPending}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="grid gap-3 p-3 md:hidden">
              {filteredReminders.map((reminder) => (
                <article
                  key={reminder.id}
                  className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-3 shadow-sm"
                >
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <h2 className="text-sm font-semibold text-slate-900">
                        {reminder.event?.name || 'Untitled event'}
                      </h2>
                      <p className="mt-0.5 text-xs text-slate-500">
                        {reminder.user?.name || 'Unknown user'}
                        {reminder.user?.email
                          ? ` • ${reminder.user.email}`
                          : ''}
                        {reminder.user?.phoneNumber
                          ? ` • ${reminder.user.phoneNumber}`
                          : ''}
                      </p>
                    </div>
                    <StatusBadge status={reminder.status} />
                  </div>

                  <div className="flex items-center justify-between gap-2 text-xs text-slate-600">
                    <div>
                      <p className="font-medium text-slate-700">Remind at</p>
                      <p>{formatDateTime(reminder.remindAt)}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium text-slate-700">Channel</p>
                      <ChannelBadge channel={reminder.channel} />
                    </div>
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="button"
                      onClick={() => handleDeleteClick(reminder)}
                      className="inline-flex items-center rounded-md border border-red-100 bg-red-50 px-3 py-1 text-xs font-medium text-red-700 shadow-sm transition-colors hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-200"
                      disabled={deleteReminder.isPending}
                    >
                      Delete
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </div>
        )}
      </section>

      {reminderToDelete && (
        <ConfirmDialog
          title="Delete reminder?"
          message={`Delete reminder for "${reminderToDelete.event?.name ?? reminderToDelete.user?.name ?? reminderToDelete.user?.email ?? reminderToDelete.user?.phoneNumber ?? reminderToDelete.id}" scheduled at ${formatDateTime(reminderToDelete.remindAt)}?`}
          confirmLabel="Delete"
          cancelLabel="Cancel"
          variant="danger"
          onConfirm={handleConfirmDelete}
          onCancel={handleCancelDelete}
          cancelRef={cancelButtonRef}
          isPending={deleteReminder.isPending}
        />
      )}
    </div>
  )
}

interface ConfirmDialogProps {
  title: string
  message: string
  confirmLabel: string
  cancelLabel: string
  variant: 'danger'
  onConfirm: () => void
  onCancel: () => void
  cancelRef: React.RefObject<HTMLButtonElement | null>
  isPending: boolean
}

function ConfirmDialog({
  title,
  message,
  confirmLabel,
  cancelLabel,
  variant,
  onConfirm,
  onCancel,
  cancelRef,
  isPending,
}: ConfirmDialogProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-desc"
    >
      <div
        className="absolute inset-0 bg-slate-900/50"
        aria-hidden="true"
        onClick={onCancel}
      />
      <div className="relative z-10 w-full max-w-md rounded-xl border border-slate-200 bg-white p-5 shadow-xl">
        <h2
          id="confirm-dialog-title"
          className="text-lg font-semibold text-slate-900"
        >
          {title}
        </h2>
        <p id="confirm-dialog-desc" className="mt-2 text-sm text-slate-600">
          {message}
        </p>
        <div className="mt-5 flex flex-wrap justify-end gap-2">
          <button
            type="button"
            ref={cancelRef}
            onClick={onCancel}
            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isPending}
            className={
              variant === 'danger'
                ? 'rounded-lg border border-red-200 bg-red-600 px-3 py-2 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-2 disabled:opacity-70'
                : ''
            }
          >
            {isPending ? 'Deleting…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}

function StatusBadge({ status }: { status: ReminderStatus }) {
  const styles: Record<ReminderStatus, string> = {
    PENDING:
      'bg-amber-50 text-amber-800 border-amber-100 ring-amber-100/60 dark:bg-amber-900/20 dark:text-amber-200 dark:border-amber-900',
    SENT: 'bg-emerald-50 text-emerald-800 border-emerald-100 ring-emerald-100/60',
    FAILED:
      'bg-red-50 text-red-800 border-red-100 ring-red-100/60 dark:bg-red-900/20 dark:text-red-200 dark:border-red-900',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${styles[status]}`}
    >
      <span className="mr-1.5 h-1.5 w-1.5 rounded-full bg-current" />
      {status}
    </span>
  )
}

function ChannelBadge({ channel }: { channel: ReminderChannel }) {
  const styles: Record<ReminderChannel, string> = {
    SMS: 'bg-blue-50 text-blue-700 border-blue-100',
    PHONE: 'bg-indigo-50 text-indigo-700 border-indigo-100',
    EMAIL: 'bg-sky-50 text-sky-700 border-sky-100',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${styles[channel]}`}
    >
      {channel}
    </span>
  )
}

function formatDateTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Invalid date'

  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

