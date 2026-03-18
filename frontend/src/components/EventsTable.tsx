import { Fragment, useCallback, useEffect, useId, useMemo, useState } from 'react'
import type { Event, ReminderChannel, User } from '../features/events/types'
import type { Reminder } from '../features/reminders/types'
import { formatDateTime } from '../utils/formatDateTime'

function descriptionSnippet(description: string | null | undefined, maxLen: number): string {
  if (description == null || description === '') return '—'
  const trimmed = description.trim()
  if (trimmed.length <= maxLen) return trimmed
  return trimmed.slice(0, maxLen) + '…'
}

interface EventsTableProps {
  events: Event[]
  users: User[]
  reminders: Reminder[]
  expandedEventId: string | null
  onToggleExpand: (eventId: string) => void
  isLoading: boolean
  isError: boolean
  showEmptyState: boolean
  onDeleteClick: (event: Event) => void
  isDeletePending: boolean
  onRemindNow: (event: Event, channel: ReminderChannel) => void
  remindNowEventIdPending: string | null
}

function getOwnerDisplay(users: User[], ownerId: string): string {
  const user = users.find((u) => u.id === ownerId)
  return user?.phoneNumber ?? ownerId ?? '—'
}

export function EventsTable({
  events,
  users,
  reminders,
  expandedEventId,
  onToggleExpand,
  isLoading,
  isError,
  showEmptyState,
  onDeleteClick,
  isDeletePending,
  onRemindNow,
  remindNowEventIdPending,
}: EventsTableProps) {
  const remindNowDialogId = useId()
  const [eventToRemindNow, setEventToRemindNow] = useState<Event | null>(null)

  const remindNowChannels = useMemo<ReminderChannel[]>(
    () => ['SMS', 'EMAIL', 'PHONE'],
    [],
  )

  const closeRemindNowDialog = useCallback(() => {
    setEventToRemindNow(null)
  }, [])

  useEffect(() => {
    if (!eventToRemindNow) return
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        closeRemindNowDialog()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [eventToRemindNow, closeRemindNowDialog])

  const baseActionButtonClassName =
    'inline-flex items-center rounded-md border px-3 py-1 text-xs font-medium shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-2 disabled:opacity-60'

  const remindNowButtonClassName =
    `${baseActionButtonClassName} border-emerald-100 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 focus-visible:ring-emerald-200 ` +
    'dark:border-emerald-900/50 dark:bg-emerald-900/20 dark:text-emerald-200 dark:hover:bg-emerald-900/40 dark:focus-visible:ring-emerald-800'

  const deleteButtonClassName =
    `${baseActionButtonClassName} border-red-100 bg-red-50 text-red-700 hover:bg-red-100 focus-visible:ring-red-200 ` +
    'dark:border-red-900/50 dark:bg-red-900/20 dark:text-red-200 dark:hover:bg-red-900/40 dark:focus-visible:ring-red-800'

  function getRemindersForEvent(eventId: string): Reminder[] {
    return reminders.filter((r) => r.event?.id === eventId)
  }
  return (
    <section className="flex-1 rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-600 dark:bg-slate-800">
      {isLoading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="flex flex-col items-center gap-2 text-slate-500 dark:text-slate-400">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-slate-300 border-t-tickr-500 dark:border-slate-600 dark:border-t-tickr-400" />
            <span className="text-sm">Loading events…</span>
          </div>
        </div>
      ) : isError ? (
        <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
          <p className="text-sm font-medium text-red-600 dark:text-red-400">
            Failed to load events
          </p>
          <p className="max-w-md text-xs text-slate-500 dark:text-slate-400">
            Please ensure the Tickr backend is running and try again.
          </p>
        </div>
      ) : showEmptyState ? (
        <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
          <p className="text-sm font-medium text-slate-900 dark:text-slate-100">No events yet</p>
          <p className="max-w-md text-xs text-slate-500 dark:text-slate-400">
            Click &quot;Add event&quot; to create your first event.
          </p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl">
          <div className="hidden md:block">
            <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-600">
              <thead className="bg-slate-50 dark:bg-slate-800">
                <tr>
                  <th
                    scope="col"
                    className="w-9 px-1 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                    aria-label="Expand reminders"
                  >
                    <span className="sr-only">Expand</span>
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Title
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Description
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Start
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    End
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Timezone
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Owner
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white dark:divide-slate-700 dark:bg-slate-800">
                {events.map((event) => {
                  const eventReminders = getRemindersForEvent(event.id)
                  const isExpanded = expandedEventId === event.id
                  return (
                    <Fragment key={event.id}>
                      <tr
                        className="hover:bg-slate-50/60 dark:bg-slate-800 dark:hover:bg-slate-700/80"
                      >
                        <td className="w-9 px-1 py-3 align-top">
                          {eventReminders.length > 0 ? (
                            <button
                              type="button"
                              onClick={() => onToggleExpand(event.id)}
                              className="inline-flex h-7 w-7 items-center justify-center rounded text-slate-500 hover:bg-slate-100 hover:text-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 dark:text-slate-400 dark:hover:bg-slate-600 dark:hover:text-slate-200 dark:focus-visible:ring-tickr-400"
                              aria-expanded={isExpanded}
                              aria-label={isExpanded ? 'Collapse reminders' : `Expand ${eventReminders.length} reminder(s)`}
                            >
                              <span
                                className={`inline-block transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                                aria-hidden
                              >
                                ▼
                              </span>
                            </button>
                          ) : (
                            <span className="inline-block h-7 w-7" aria-hidden />
                          )}
                        </td>
                        <td className="px-4 py-3 text-sm font-medium text-slate-900 dark:text-slate-200">
                          {event.title}
                        </td>
                        <td className="px-4 py-3 text-sm text-slate-600 max-w-[200px] truncate dark:text-slate-300">
                          {descriptionSnippet(event.description, 60)}
                        </td>
                        <td className="px-4 py-3 text-sm text-slate-900 dark:text-slate-200">
                          {formatDateTime(event.startTime)}
                        </td>
                        <td className="px-4 py-3 text-sm text-slate-900 dark:text-slate-200">
                          {event.endTime ? formatDateTime(event.endTime) : '—'}
                        </td>
                        <td className="px-4 py-3 text-sm text-slate-600 dark:text-slate-300">
                          {event.timezone}
                        </td>
                        <td className="px-4 py-3 text-sm text-slate-600 dark:text-slate-300">
                          {getOwnerDisplay(users, event.ownerId)}
                        </td>
                        <td className="px-4 py-3 text-right text-sm">
                          <div className="inline-flex items-center gap-2">
                            <button
                              type="button"
                              onClick={() => setEventToRemindNow(event)}
                              className={remindNowButtonClassName}
                              disabled={remindNowEventIdPending === event.id}
                            >
                              {remindNowEventIdPending === event.id ? 'Reminding…' : 'Remind now'}
                            </button>
                            <button
                              type="button"
                              onClick={() => onDeleteClick(event)}
                              className={deleteButtonClassName}
                              disabled={isDeletePending}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                      {isExpanded && eventReminders.length > 0 && (
                        <tr key={`${event.id}-reminders`} className="bg-slate-50/80 dark:bg-slate-800/80">
                          <td colSpan={8} className="px-4 py-3 pt-0">
                            <div className="rounded-lg border border-slate-200 bg-white py-2 shadow-sm dark:border-slate-600 dark:bg-slate-800">
                              <div className="px-3 pb-1.5 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                                Reminders ({eventReminders.length})
                              </div>
                              <ul className="divide-y divide-slate-100 dark:divide-slate-700">
                                {eventReminders.map((reminder) => (
                                  <li
                                    key={reminder.id}
                                    className="flex flex-wrap items-center gap-x-4 gap-y-1 px-3 py-2 text-sm"
                                  >
                                    <span className="font-medium text-slate-700 dark:text-slate-200">
                                      {formatDateTime(reminder.remindAt)}
                                    </span>
                                    <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-600 dark:text-slate-200">
                                      {reminder.channel}
                                    </span>
                                    <span
                                      className={`rounded px-2 py-0.5 text-xs font-medium ${
                                        reminder.status === 'PENDING'
                                          ? 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-200'
                                          : reminder.status === 'SENT'
                                            ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-200'
                                            : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-200'
                                      }`}
                                    >
                                      {reminder.status}
                                    </span>
                                    {reminder.user?.phoneNumber && (
                                      <span className="text-slate-500 dark:text-slate-400">
                                        {reminder.user.phoneNumber}
                                      </span>
                                    )}
                                  </li>
                                ))}
                              </ul>
                            </div>
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  )
                })}
              </tbody>
            </table>
          </div>

          <div className="grid gap-3 p-3 md:hidden">
            {events.map((event) => {
              const eventReminders = getRemindersForEvent(event.id)
              const isExpanded = expandedEventId === event.id
              return (
                <article
                  key={event.id}
                  className="flex flex-col gap-2 rounded-xl border border-slate-200 bg-white p-3 shadow-sm dark:border-slate-600 dark:bg-slate-800"
                >
                  <div className="flex items-start justify-between gap-2">
                    <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                      {event.title}
                    </h2>
                    {eventReminders.length > 0 && (
                      <button
                        type="button"
                        onClick={() => onToggleExpand(event.id)}
                        className="inline-flex items-center rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-600"
                        aria-expanded={isExpanded}
                      >
                        {eventReminders.length} reminder{eventReminders.length !== 1 ? 's' : ''}{' '}
                        {isExpanded ? '▲' : '▼'}
                      </button>
                    )}
                  </div>
                  <p className="text-xs text-slate-600 line-clamp-2 dark:text-slate-300">
                    {descriptionSnippet(event.description, 80)}
                  </p>
                  <div className="flex flex-wrap gap-x-3 gap-y-1 text-xs text-slate-500 dark:text-slate-400">
                    <span>Start: {formatDateTime(event.startTime)}</span>
                    {event.endTime && <span>End: {formatDateTime(event.endTime)}</span>}
                    <span>{event.timezone}</span>
                    <span>Owner: {getOwnerDisplay(users, event.ownerId)}</span>
                  </div>
                  {isExpanded && eventReminders.length > 0 && (
                    <div className="rounded-lg border border-slate-200 bg-slate-50/50 py-2 dark:border-slate-600 dark:bg-slate-700/30">
                      <div className="px-2 pb-1 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                        Reminders
                      </div>
                      <ul className="space-y-1.5 px-2">
                        {eventReminders.map((reminder) => (
                          <li
                            key={reminder.id}
                            className="flex flex-wrap items-center gap-x-2 text-xs text-slate-700 dark:text-slate-300"
                          >
                            <span>{formatDateTime(reminder.remindAt)}</span>
                            <span className="rounded bg-slate-200 px-1.5 py-0.5 dark:bg-slate-600 dark:text-slate-200">
                              {reminder.channel}
                            </span>
                            <span>{reminder.status}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  <div className="flex justify-end pt-1">
                    <div className="inline-flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => setEventToRemindNow(event)}
                        className={remindNowButtonClassName}
                        disabled={remindNowEventIdPending === event.id}
                      >
                        {remindNowEventIdPending === event.id ? 'Reminding…' : 'Remind now'}
                      </button>
                      <button
                        type="button"
                        onClick={() => onDeleteClick(event)}
                        className={deleteButtonClassName}
                        disabled={isDeletePending}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        </div>
      )}

      {eventToRemindNow && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby={remindNowDialogId}
        >
          <div
            className="absolute inset-0 bg-slate-900/50"
            aria-hidden="true"
            onClick={closeRemindNowDialog}
          />
          <div className="relative z-10 w-full max-w-xs rounded-xl border border-slate-200 bg-white p-4 shadow-xl dark:border-slate-600 dark:bg-slate-800">
            <h2
              id={remindNowDialogId}
              className="text-sm font-semibold text-slate-900 dark:text-slate-100"
            >
              Remind now
            </h2>
            <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
              Choose a channel for &quot;{eventToRemindNow.title}&quot;.
            </p>

            <div className="mt-3 grid grid-cols-3 gap-2">
              {remindNowChannels.map((channel) => {
                const isPendingForThisEvent = remindNowEventIdPending === eventToRemindNow.id
                return (
                  <button
                    key={channel}
                    type="button"
                    className="btn btn-outline btn-xs"
                    disabled={isPendingForThisEvent}
                    onClick={() => {
                      onRemindNow(eventToRemindNow, channel)
                      closeRemindNowDialog()
                    }}
                  >
                    {channel === 'PHONE' ? 'Phone' : channel}
                  </button>
                )
              })}
            </div>

            <div className="mt-4 flex justify-end">
              <button type="button" className="btn btn-ghost btn-xs" onClick={closeRemindNowDialog}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
