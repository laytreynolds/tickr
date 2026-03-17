import { Fragment, useMemo } from 'react'
import type { Reminder, ReminderChannel, ReminderStatus } from '../features/reminders/types'
import { formatDateTime } from '../utils/formatDateTime'

const CHANNEL_ORDER: ReminderChannel[] = ['SMS', 'PHONE', 'EMAIL']

interface EventChannelGroup {
  eventId: string
  eventName: string
  channels: { channel: ReminderChannel; reminders: Reminder[] }[]
}

function groupRemindersByEventAndChannel(reminders: Reminder[]): EventChannelGroup[] {
  const byEvent = new Map<string, Map<ReminderChannel, Reminder[]>>()

  for (const r of reminders) {
    const eventId = r.event?.id ?? '__no_event__'
    if (!byEvent.has(eventId)) {
      byEvent.set(eventId, new Map())
    }
    const channels = byEvent.get(eventId)!
    if (!channels.has(r.channel)) {
      channels.set(r.channel, [])
    }
    channels.get(r.channel)!.push(r)
  }

  const eventNames = new Map<string, string>()
  for (const r of reminders) {
    const id = r.event?.id ?? '__no_event__'
    const name = r.event?.title?.trim() || 'Untitled event'
    if (!eventNames.has(id)) eventNames.set(id, name)
  }

  return Array.from(byEvent.entries())
    .map(([eventId, channelMap]) => {
      const channels: { channel: ReminderChannel; reminders: Reminder[] }[] = []
      for (const ch of CHANNEL_ORDER) {
        const list = channelMap.get(ch)
        if (list?.length) {
          list.sort((a, b) => new Date(a.remindAt).getTime() - new Date(b.remindAt).getTime())
          channels.push({ channel: ch, reminders: list })
        }
      }
      return {
        eventId,
        eventName: eventNames.get(eventId) ?? 'Untitled event',
        channels,
      }
    })
    .filter((g) => g.channels.length > 0)
    .sort((a, b) => a.eventName.localeCompare(b.eventName, undefined, { sensitivity: 'base' }))
}

interface RemindersTableProps {
  reminders: Reminder[]
  isLoading: boolean
  isError: boolean
  showEmptyState: boolean
  onDeleteClick: (reminder: Reminder) => void
  isDeletePending: boolean
}

export function RemindersTable({
  reminders,
  isLoading,
  isError,
  showEmptyState,
  onDeleteClick,
  isDeletePending,
}: RemindersTableProps) {
  const grouped = useMemo(
    () => groupRemindersByEventAndChannel(reminders),
    [reminders],
  )

  return (
    <section className="flex-1 rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-600 dark:bg-slate-800">
      {isLoading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="flex flex-col items-center gap-2 text-slate-500 dark:text-slate-400">
            <span className="h-6 w-6 animate-spin rounded-full border-2 border-slate-300 border-t-tickr-500 dark:border-slate-600 dark:border-t-tickr-400" />
            <span className="text-sm">Loading reminders…</span>
          </div>
        </div>
      ) : isError ? (
        <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
          <p className="text-sm font-medium text-red-600 dark:text-red-400">
            Failed to load reminders
          </p>
          <p className="max-w-md text-xs text-slate-500 dark:text-slate-400">
            Please ensure the Tickr backend is running on{' '}
            <code className="rounded bg-slate-100 px-1 py-0.5 text-[11px] dark:bg-slate-700 dark:text-slate-200">
              http://localhost:8080
            </code>{' '}
            (or update <code>VITE_API_BASE_URL</code>) and try again.
          </p>
        </div>
      ) : showEmptyState ? (
        <div className="flex h-64 flex-col items-center justify-center gap-2 text-center">
          <p className="text-sm font-medium text-slate-900 dark:text-slate-100">
            No reminders yet
          </p>
          <p className="max-w-md text-xs text-slate-500 dark:text-slate-400">
            When your backend starts scheduling reminders, they will appear here in real time.
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
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Event / User
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Remind at
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Channel
                  </th>
                  <th
                    scope="col"
                    className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                  >
                    Status
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
                {grouped.map((group) => (
                  <Fragment key={group.eventId}>
                    {group.channels.map(({ channel, reminders: channelReminders }) => (
                      <Fragment key={`${group.eventId}-${channel}`}>
                        <tr className="bg-slate-100 dark:bg-slate-700/50">
                          <td
                            colSpan={5}
                            className="px-4 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600 dark:text-slate-300"
                          >
                            {group.eventName} — {channel}
                          </td>
                        </tr>
                        {channelReminders.map((reminder) => (
                          <tr
                            key={reminder.id}
                            className="hover:bg-slate-50/60 dark:bg-slate-800 dark:hover:bg-slate-700/80"
                          >
                            <td className="px-4 py-3 text-sm text-slate-900 dark:text-slate-200">
                              <div className="font-medium">
                                {reminder.event?.title || 'Untitled event'}
                              </div>
                              <div className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
                                {reminder.user?.name || 'Unknown user'}
                                {reminder.user?.email ? ` • ${reminder.user.email}` : ''}
                                {reminder.user?.phoneNumber ? ` • ${reminder.user.phoneNumber}` : ''}
                              </div>
                            </td>
                            <td className="px-4 py-3 text-sm text-slate-900 dark:text-slate-200">
                              <div>{formatDateTime(reminder.remindAt)}</div>
                              <div className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
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
                                onClick={() => onDeleteClick(reminder)}
                                className="inline-flex items-center rounded-md border border-red-100 bg-red-50 px-3 py-1 text-xs font-medium text-red-700 shadow-sm transition-colors hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-200 dark:border-red-900/50 dark:bg-red-900/20 dark:text-red-200 dark:hover:bg-red-900/40 dark:focus-visible:ring-red-800"
                                disabled={isDeletePending}
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))}
                      </Fragment>
                    ))}
                  </Fragment>
                ))}
              </tbody>
            </table>
          </div>

          <div className="grid gap-4 p-3 md:hidden">
            {grouped.map((group) => (
              <div
                key={group.eventId}
                className="rounded-xl border border-slate-200 dark:border-slate-600"
              >
                <h2 className="border-b border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-900 dark:border-slate-600 dark:bg-slate-700/50 dark:text-slate-100">
                  {group.eventName}
                </h2>
                <div className="divide-y divide-slate-100 dark:divide-slate-700">
                  {group.channels.map(({ channel, reminders: channelReminders }) => (
                    <div key={channel} className="bg-white dark:bg-slate-800">
                      <h3 className="px-3 py-1.5 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                        {channel}
                      </h3>
                      {channelReminders.map((reminder) => (
                        <article
                          key={reminder.id}
                          className="flex flex-col gap-3 border-t border-slate-100 px-3 py-3 dark:border-slate-700"
                        >
                          <div className="flex items-start justify-between gap-2">
                            <p className="text-xs text-slate-500 dark:text-slate-400">
                              {reminder.user?.name || 'Unknown user'}
                              {reminder.user?.email ? ` • ${reminder.user.email}` : ''}
                              {reminder.user?.phoneNumber ? ` • ${reminder.user.phoneNumber}` : ''}
                            </p>
                            <StatusBadge status={reminder.status} />
                          </div>
                          <div className="flex items-center justify-between gap-2 text-xs text-slate-600 dark:text-slate-300">
                            <div>
                              <p className="font-medium text-slate-700 dark:text-slate-200">Remind at</p>
                              <p>{formatDateTime(reminder.remindAt)}</p>
                            </div>
                            <ChannelBadge channel={reminder.channel} />
                          </div>
                          <div className="flex justify-end">
                            <button
                              type="button"
                              onClick={() => onDeleteClick(reminder)}
                              className="inline-flex items-center rounded-md border border-red-100 bg-red-50 px-3 py-1 text-xs font-medium text-red-700 shadow-sm transition-colors hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-200 dark:border-red-900/50 dark:bg-red-900/20 dark:text-red-200 dark:hover:bg-red-900/40 dark:focus-visible:ring-red-800"
                              disabled={isDeletePending}
                            >
                              Delete
                            </button>
                          </div>
                        </article>
                      ))}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}

function StatusBadge({ status }: { status: ReminderStatus }) {
  const styles: Record<ReminderStatus, string> = {
    PENDING:
      'bg-amber-50 text-amber-800 border-amber-100 ring-amber-100/60 dark:bg-amber-900/20 dark:text-amber-200 dark:border-amber-900',
    SENT:
      'bg-emerald-50 text-emerald-800 border-emerald-100 ring-emerald-100/60 dark:bg-emerald-900/20 dark:text-emerald-200 dark:border-emerald-900',
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
    SMS: 'bg-blue-50 text-blue-700 border-blue-100 dark:bg-blue-900/20 dark:text-blue-200 dark:border-blue-800',
    PHONE:
      'bg-indigo-50 text-indigo-700 border-indigo-100 dark:bg-indigo-900/20 dark:text-indigo-200 dark:border-indigo-800',
    EMAIL: 'bg-sky-50 text-sky-700 border-sky-100 dark:bg-sky-900/20 dark:text-sky-200 dark:border-sky-800',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${styles[channel]}`}
    >
      {channel}
    </span>
  )
}
