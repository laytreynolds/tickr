import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { Reminder } from './types'
import { useDeleteReminder, useReminders } from './api'
import FadeIn from '../../components/FadeIn'
import { RemindersSearchBar, type RemindersFilterState } from '../../components/RemindersSearchBar'
import { RemindersTable } from '../../components/RemindersTable'
import { formatDateTime } from '../../utils/formatDateTime'

export function RemindersPage() {
  const [filters, setFilters] = useState<RemindersFilterState>({
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

  const showEmptyState = Boolean(
    !remindersQuery.isLoading &&
      !remindersQuery.isError &&
      remindersQuery.data &&
      remindersQuery.data.length === 0,
  )

  return (
    <div className="flex flex-1 flex-col gap-6">
      <FadeIn>
      <header className="flex flex-col gap-4 border-b border-slate-200 pb-4 dark:border-slate-700 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-slate-100">
            Reminders
          </h1>
          
            
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Search, filter, and manage outgoing reminders from Tickr.
          </p>
        </div>
        <button
          type="button"
          onClick={() => remindersQuery.refetch()}
          className="inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
          disabled={remindersQuery.isFetching}
          >
          {remindersQuery.isFetching ? (
            <span className="inline-flex items-center gap-2">
              <span className="h-3 w-3 animate-spin rounded-full border-2 border-slate-400 border-t-transparent dark:border-slate-500 dark:border-t-tickr-400" />
              Refreshing…
            </span>
          ) : (
            'Refresh'
          )}
        </button>
      </header>
      </FadeIn>

      <FadeIn delay={0.1}>
      <RemindersSearchBar filters={filters} onFiltersChange={setFilters} />
      </FadeIn>

      <FadeIn delay={0.2}>
      <RemindersTable
        reminders={filteredReminders}
        isLoading={remindersQuery.isLoading}
        isError={remindersQuery.isError}
        showEmptyState={showEmptyState}
        onDeleteClick={handleDeleteClick}
        isDeletePending={deleteReminder.isPending}
      />
      </FadeIn>

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
