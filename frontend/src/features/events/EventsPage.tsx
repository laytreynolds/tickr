import { useCallback, useEffect, useRef, useState } from 'react'
import type { CreateEventRequest, Event, ReminderChannel, User } from './types'
import { useCreateEvent, useDeleteEvent, useEvents, useRemindNow, useUsers } from './api'
import { useReminders } from '../reminders/api'
import { EventsTable } from '../../components/EventsTable'
import FadeIn from '../../components/FadeIn'


const TITLE_MAX_LENGTH = 500
const DESCRIPTION_MAX_LENGTH = 2000

const COMMON_TIMEZONES = [
  'UTC',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Paris',
  'Europe/Berlin',
  'Asia/Tokyo',
  'Australia/Sydney',
]

function getTimeZoneOptions(): string[] {
  if (typeof Intl !== 'undefined' && 'supportedValuesOf' in Intl) {
    try {
      return (Intl as unknown as { supportedValuesOf: (key: string) => string[] })
        .supportedValuesOf('timeZone') as string[]
    } catch {
      // fallback
    }
  }
  const resolved = Intl?.DateTimeFormat?.().resolvedOptions?.().timeZone
  const list = [...COMMON_TIMEZONES]
  if (resolved && !list.includes(resolved)) {
    list.unshift(resolved)
  }
  return list
}

/** Convert local date + time in a given IANA timezone to ISO 8601 UTC string. */
function localInZoneToISO(
  dateStr: string,
  timeStr: string,
  timeZone: string,
): string {
  const [y, mo, d] = dateStr.split('-').map(Number)
  const [h, min] = timeStr.split(':').map(Number)
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
  const base = new Date(Date.UTC(y, mo - 1, d, 0, 0, 0, 0))
  for (let u = 0; u < 48; u++) {
    const cand = new Date(base.getTime() + u * 30 * 60 * 1000)
    const parts = formatter.formatToParts(cand)
    const get = (type: string) =>
      parts.find((p) => p.type === type)?.value ?? ''
    const fy = Number(get('year'))
    const fm = Number(get('month'))
    const fd = Number(get('day'))
    const fh = Number(get('hour'))
    const fmin = Number(get('minute'))
    if (fy === y && fm === mo && fd === d && fh === h && fmin === min) {
      return cand.toISOString()
    }
  }
  const nextDay = new Date(base.getTime() + 24 * 60 * 60 * 1000)
  for (let u = 0; u < 48; u++) {
    const cand = new Date(nextDay.getTime() + u * 30 * 60 * 1000)
    const parts = formatter.formatToParts(cand)
    const get = (type: string) =>
      parts.find((p) => p.type === type)?.value ?? ''
    const fy = Number(get('year'))
    const fm = Number(get('month'))
    const fd = Number(get('day'))
    const fh = Number(get('hour'))
    const fmin = Number(get('minute'))
    if (fy === y && fm === mo && fd === d && fh === h && fmin === min) {
      return cand.toISOString()
    }
  }
  return new Date(Date.UTC(y, mo - 1, d, h, min ?? 0, 0, 0)).toISOString()
}

const REMINDER_CHANNELS: ReminderChannel[] = ['SMS', 'PHONE', 'EMAIL']

interface FormState {
  title: string
  description: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
  timezone: string
  ownerId: string
  assignedUserIds: string[]
  source: string
  reminderChannels: ReminderChannel[]
}

const defaultFormState: FormState = {
  title: '',
  description: '',
  startDate: '',
  startTime: '09:00',
  endDate: '',
  endTime: '',
  timezone: Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC',
  ownerId: '',
  assignedUserIds: [],
  source: '0',
  reminderChannels: ['SMS'],
}

export function EventsPage() {
  const [showAddModal, setShowAddModal] = useState(false)
  const [eventToDelete, setEventToDelete] = useState<Event | null>(null)
  const [expandedEventId, setExpandedEventId] = useState<string | null>(null)
  const [formState, setFormState] = useState<FormState>(defaultFormState)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [remindNowMessage, setRemindNowMessage] = useState<string | null>(null)
  const [remindNowError, setRemindNowError] = useState<string | null>(null)
  const [remindNowEventIdPending, setRemindNowEventIdPending] = useState<string | null>(null)
  const cancelButtonRef = useRef<HTMLButtonElement>(null)
  const deleteCancelRef = useRef<HTMLButtonElement>(null)

  const eventsQuery = useEvents()
  const usersQuery = useUsers()
  const remindersQuery = useReminders()
  const createEvent = useCreateEvent()
  const deleteEvent = useDeleteEvent()
  const remindNow = useRemindNow()
  const timeZoneOptions = getTimeZoneOptions()

  const handleToggleReminders = useCallback((eventId: string) => {
    setExpandedEventId((prev) => (prev === eventId ? null : eventId))
  }, [])

  useEffect(() => {
    if (showAddModal) {
      cancelButtonRef.current?.focus()
    }
  }, [showAddModal])

  useEffect(() => {
    if (eventToDelete) {
      deleteCancelRef.current?.focus()
    }
  }, [eventToDelete])

  useEffect(() => {
    if (!showAddModal) return
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        setShowAddModal(false)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [showAddModal])

  useEffect(() => {
    if (!eventToDelete) return
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault()
        setEventToDelete(null)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [eventToDelete])

  const openAddModal = useCallback(() => {
    setFormState({ ...defaultFormState, timezone: Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC' })
    setSubmitError(null)
    setSuccessMessage(null)
    setRemindNowMessage(null)
    setRemindNowError(null)
    setShowAddModal(true)
  }, [])

  const closeAddModal = useCallback(() => {
    setShowAddModal(false)
    setSubmitError(null)
  }, [])

  const handleDeleteClick = useCallback((event: Event) => {
    setEventToDelete(event)
  }, [])

  const handleRemindNow = useCallback(
    (event: Event, channel: ReminderChannel) => {
      setRemindNowMessage(null)
      setRemindNowError(null)
      setRemindNowEventIdPending(event.id)
      remindNow.mutate(
        { event_id: event.id, channels: [channel] },
        {
          onSuccess: () => {
            setRemindNowMessage(`Reminder queued via ${channel === 'PHONE' ? 'Phone' : channel}.`)
          },
          onError: (err: Error & { response?: { data?: unknown } }) => {
            const message =
              err.response?.data != null && typeof (err.response.data as { message?: string }).message === 'string'
                ? (err.response.data as { message: string }).message
                : err.message ?? 'Failed to send reminder.'
            setRemindNowError(message)
          },
          onSettled: () => setRemindNowEventIdPending(null),
        },
      )
    },
    [remindNow],
  )

  const handleConfirmDeleteEvent = useCallback(() => {
    if (!eventToDelete) return
    deleteEvent.mutate(eventToDelete.id, {
      onSettled: () => setEventToDelete(null),
    })
  }, [eventToDelete, deleteEvent])

  const handleCancelDeleteEvent = useCallback(() => {
    setEventToDelete(null)
  }, [])

  const userById = useCallback(
    (id: string): User | undefined => usersQuery.data?.find((u) => u.id === id),
    [usersQuery.data],
  )

  const validateAndBuildRequest = useCallback((): CreateEventRequest | string => {
    const title = formState.title.trim()
    if (title.length === 0) return 'Title is required.'
    if (title.length > TITLE_MAX_LENGTH) return `Title must be at most ${TITLE_MAX_LENGTH} characters.`

    const description = formState.description.trim()
    if (description.length > DESCRIPTION_MAX_LENGTH) return `Description must be at most ${DESCRIPTION_MAX_LENGTH} characters.`

    if (!formState.startDate || !formState.startTime) return 'Start date and time are required.'
    const startTimeISO = localInZoneToISO(
      formState.startDate,
      formState.startTime,
      formState.timezone,
    )

    let endTimeISO: string | undefined
    if (formState.endDate && formState.endTime) {
      endTimeISO = localInZoneToISO(
        formState.endDate,
        formState.endTime,
        formState.timezone,
      )
      if (endTimeISO < startTimeISO) return 'End time must be after start time.'
    } else if (formState.endDate || formState.endTime) {
      return 'Both end date and end time must be set, or leave both empty.'
    }

    if (!formState.ownerId) return 'Owner is required.'

    const owner = userById(formState.ownerId)
    if (!owner) return 'Selected owner is not valid.'

    if (formState.reminderChannels.length === 0) {
      return 'Select at least one reminder channel (SMS, Phone, or Email).'
    }

    const payload: CreateEventRequest = {
      owner_id: formState.ownerId,
      assigned_user_ids: formState.assignedUserIds.filter((id) => id !== formState.ownerId),
      title,
      start_time: startTimeISO,
      source: formState.source.trim() || '0',
      timezone: formState.timezone,
      channels: formState.reminderChannels,
    }
    if (description.length > 0) payload.description = description
    if (endTimeISO) payload.end_time = endTimeISO

    return payload
  }, [formState, userById])

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault()
      setSubmitError(null)
      const result = validateAndBuildRequest()
      if (typeof result === 'string') {
        setSubmitError(result)
        return
      }
      createEvent.mutate(result, {
        onSuccess: () => {
          setSuccessMessage('Event created.')
          closeAddModal()
        },
        onError: (err: Error & { response?: { data?: unknown } }) => {
          const message =
            err.response?.data != null && typeof (err.response.data as { message?: string }).message === 'string'
              ? (err.response.data as { message: string }).message
              : err.message ?? 'Failed to create event.'
          setSubmitError(message)
        },
      })
    },
    [validateAndBuildRequest, createEvent, closeAddModal],
  )

  const showEmptyState = Boolean(
    !eventsQuery.isLoading &&
      !eventsQuery.isError &&
      eventsQuery.data &&
      eventsQuery.data.length === 0,
  )

  return (
    <div className="flex flex-1 flex-col gap-6">
      <FadeIn>
      <header className="flex flex-col gap-4 border-b border-slate-200 pb-4 dark:border-slate-700 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-slate-100">
            Events
          </h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Create and view events. Times are shown in each event&apos;s timezone.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => eventsQuery.refetch()}
            className="inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
            disabled={eventsQuery.isFetching}
          >
            {eventsQuery.isFetching ? (
              <span className="inline-flex items-center gap-2">
                <span className="h-3 w-3 animate-spin rounded-full border-2 border-slate-400 border-t-transparent dark:border-slate-500 dark:border-t-tickr-400" />
                Refreshing…
              </span>
            ) : (
              'Refresh'
            )}
          </button>
          <button
            type="button"
            onClick={openAddModal}
            className="inline-flex items-center justify-center rounded-lg border border-tickr-500 bg-tickr-500 px-3 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-tickr-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2"
          >
            Add event
          </button>
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

      {remindNowMessage && (
        <div
          role="status"
          className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-200"
        >
          {remindNowMessage}
        </div>
      )}

      {remindNowError && (
        <div
          role="alert"
          className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800 dark:border-red-800 dark:bg-red-900/30 dark:text-red-200"
        >
          {remindNowError}
        </div>
      )}

      <FadeIn delay={0.2}>

      <EventsTable
        events={eventsQuery.data ?? []}
        users={usersQuery.data ?? []}
        reminders={remindersQuery.data ?? []}
        expandedEventId={expandedEventId}
        onToggleExpand={handleToggleReminders}
        isLoading={eventsQuery.isLoading}
        isError={eventsQuery.isError}
        showEmptyState={showEmptyState}
        onDeleteClick={handleDeleteClick}
        isDeletePending={deleteEvent.isPending}
        onRemindNow={handleRemindNow}
        remindNowEventIdPending={remindNowEventIdPending}
      />
      </FadeIn>

      {showAddModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="add-event-dialog-title"
          aria-describedby="add-event-dialog-desc"
        >
          <div
            className="absolute inset-0 bg-slate-900/50"
            aria-hidden="true"
            onClick={closeAddModal}
          />
          <div className="relative z-10 w-full max-w-lg rounded-xl border border-slate-200 bg-white p-5 shadow-xl max-h-[90vh] overflow-y-auto">
            <h2
              id="add-event-dialog-title"
              className="text-lg font-semibold text-slate-900"
            >
              Add event
            </h2>
            <p id="add-event-dialog-desc" className="mt-1 text-sm text-slate-500">
              Fill in the required fields. Times are interpreted in the selected timezone.
            </p>

            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              {submitError && (
                <p
                  role="alert"
                  className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800"
                >
                  {submitError}
                </p>
              )}

              <div>
                <label htmlFor="event-title" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Title (required)
                </label>
                <input
                  id="event-title"
                  type="text"
                  required
                  maxLength={TITLE_MAX_LENGTH}
                  value={formState.title}
                  onChange={(e) =>
                    setFormState((prev) => ({ ...prev, title: e.target.value }))
                  }
                  className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  placeholder="Event title"
                  autoComplete="off"
                />
                <p className="mt-0.5 text-xs text-slate-500">
                  {formState.title.length} / {TITLE_MAX_LENGTH}
                </p>
              </div>

              <div>
                <label htmlFor="event-description" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Description (optional)
                </label>
                <textarea
                  id="event-description"
                  rows={3}
                  maxLength={DESCRIPTION_MAX_LENGTH}
                  value={formState.description}
                  onChange={(e) =>
                    setFormState((prev) => ({ ...prev, description: e.target.value }))
                  }
                  className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  placeholder="Optional description"
                />
                <p className="mt-0.5 text-xs text-slate-500">
                  {formState.description.length} / {DESCRIPTION_MAX_LENGTH}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label htmlFor="event-start-date" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                    Start date (required)
                  </label>
                  <input
                    id="event-start-date"
                    type="date"
                    required
                    value={formState.startDate}
                    onChange={(e) =>
                      setFormState((prev) => ({ ...prev, startDate: e.target.value }))
                    }
                    className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  />
                </div>
                <div>
                  <label htmlFor="event-start-time" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                    Start time (required)
                  </label>
                  <input
                    id="event-start-time"
                    type="time"
                    required
                    value={formState.startTime}
                    onChange={(e) =>
                      setFormState((prev) => ({ ...prev, startTime: e.target.value }))
                    }
                    className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label htmlFor="event-end-date" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                    End date (optional)
                  </label>
                  <input
                    id="event-end-date"
                    type="date"
                    value={formState.endDate}
                    onChange={(e) =>
                      setFormState((prev) => ({ ...prev, endDate: e.target.value }))
                    }
                    className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  />
                </div>
                <div>
                  <label htmlFor="event-end-time" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                    End time (optional)
                  </label>
                  <input
                    id="event-end-time"
                    type="time"
                    value={formState.endTime}
                    onChange={(e) =>
                      setFormState((prev) => ({ ...prev, endTime: e.target.value }))
                    }
                    className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="event-timezone" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Timezone (required)
                </label>
                <select
                  id="event-timezone"
                  required
                  value={formState.timezone}
                  onChange={(e) =>
                    setFormState((prev) => ({ ...prev, timezone: e.target.value }))
                  }
                  className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                >
                  {timeZoneOptions.map((tz) => (
                    <option key={tz} value={tz}>
                      {tz}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Reminder channels (required)
                </span>
                <p className="mb-2 text-xs text-slate-500 dark:text-slate-400">
                  Choose how attendees receive reminders for this event.
                </p>
                <div className="flex flex-wrap gap-4">
                  {REMINDER_CHANNELS.map((channel) => (
                    <label
                      key={channel}
                      className="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 bg-slate-50/50 px-3 py-2 transition-colors has-[:checked]:border-tickr-500 has-[:checked]:bg-tickr-50 has-[:checked]:ring-1 has-[:checked]:ring-tickr-500 dark:border-slate-600 dark:bg-slate-800/50 dark:has-[:checked]:border-tickr-400 dark:has-[:checked]:bg-tickr-900/20 dark:has-[:checked]:ring-tickr-400"
                    >
                      <input
                        type="checkbox"
                        checked={formState.reminderChannels.includes(channel)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setFormState((prev) => ({
                              ...prev,
                              reminderChannels: [...prev.reminderChannels, channel].sort(
                                (a, b) =>
                                  REMINDER_CHANNELS.indexOf(a) - REMINDER_CHANNELS.indexOf(b),
                              ),
                            }))
                          } else {
                            setFormState((prev) => ({
                              ...prev,
                              reminderChannels: prev.reminderChannels.filter((c) => c !== channel),
                            }))
                          }
                        }}
                        className="h-4 w-4 rounded border-slate-300 text-tickr-500 focus:ring-tickr-500"
                      />
                      <span className="text-sm font-medium text-slate-700 dark:text-slate-200">
                        {channel === 'PHONE' ? 'Phone' : channel}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label htmlFor="event-owner" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Owner (required)
                </label>
                <select
                  id="event-owner"
                  required
                  value={formState.ownerId}
                  onChange={(e) =>
                    setFormState((prev) => ({ ...prev, ownerId: e.target.value }))
                  }
                  className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                >
                  <option value="">Select owner</option>
                  {(usersQuery.data ?? []).map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.phoneNumber ?? user.id}
                    </option>
                  ))}
                </select>
                {usersQuery.isLoading && (
                  <p className="mt-0.5 text-xs text-slate-500">Loading users…</p>
                )}
              </div>

              <div>
                <label htmlFor="event-assigned" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
                  Assigned users (optional)
                </label>
                <select
                  id="event-assigned"
                  multiple
                  size={4}
                  value={formState.assignedUserIds}
                  onChange={(e) => {
                    const selected = Array.from(
                      e.target.selectedOptions,
                      (opt) => opt.value,
                    )
                    setFormState((prev) => ({ ...prev, assignedUserIds: selected }))
                  }}
                  className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200"
                >
                  {(usersQuery.data ?? []).map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.phoneNumber ?? user.id}
                    </option>
                  ))}
                </select>
                <p className="mt-0.5 text-xs text-slate-500">
                  Hold Ctrl/Cmd to select multiple.
                </p>
              </div>

              <div className="flex flex-wrap justify-end gap-2 pt-2">
                <button
                  type="button"
                  ref={cancelButtonRef}
                  onClick={closeAddModal}
                  className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createEvent.isPending}
                  className="rounded-lg border border-tickr-500 bg-tickr-500 px-3 py-2 text-sm font-medium text-white shadow-sm hover:bg-tickr-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2 disabled:opacity-70"
                >
                  {createEvent.isPending ? 'Creating…' : 'Create event'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {eventToDelete && (
        <ConfirmDialog
          title="Delete event?"
          message={`Delete "${eventToDelete.title}"? This will also delete all reminders for this event.`}
          confirmLabel="Delete"
          cancelLabel="Cancel"
          variant="danger"
          onConfirm={handleConfirmDeleteEvent}
          onCancel={handleCancelDeleteEvent}
          cancelRef={deleteCancelRef}
          isPending={deleteEvent.isPending}
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
      <div className="relative z-10 w-full max-w-md rounded-xl border border-slate-200 bg-white p-5 shadow-xl dark:border-slate-600 dark:bg-slate-800">
        <h2
          id="confirm-dialog-title"
          className="text-lg font-semibold text-slate-900 dark:text-slate-100"
        >
          {title}
        </h2>
        <p id="confirm-dialog-desc" className="mt-2 text-sm text-slate-600 dark:text-slate-300">
          {message}
        </p>
        <div className="mt-5 flex flex-wrap justify-end gap-2">
          <button
            type="button"
            ref={cancelRef}
            onClick={onCancel}
            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isPending}
            className="rounded-lg border border-red-200 bg-red-600 px-3 py-2 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-2 disabled:opacity-70"
          >
            {isPending ? 'Deleting…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
