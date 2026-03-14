import type { Event, User } from '../features/events/types'
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
  isLoading: boolean
  isError: boolean
  showEmptyState: boolean
}

function getOwnerDisplay(users: User[], ownerId: string): string {
  const user = users.find((u) => u.id === ownerId)
  return user?.phoneNumber ?? ownerId ?? '—'
}

export function EventsTable({
  events,
  users,
  isLoading,
  isError,
  showEmptyState,
}: EventsTableProps) {
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
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white dark:divide-slate-700 dark:bg-slate-800">
                {events.map((event) => (
                  <tr
                    key={event.id}
                    className="hover:bg-slate-50/60 dark:bg-slate-800 dark:hover:bg-slate-700/80"
                  >
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
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="grid gap-3 p-3 md:hidden">
            {events.map((event) => (
              <article
                key={event.id}
                className="flex flex-col gap-2 rounded-xl border border-slate-200 bg-white p-3 shadow-sm dark:border-slate-600 dark:bg-slate-800"
              >
                <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {event.title}
                </h2>
                <p className="text-xs text-slate-600 line-clamp-2 dark:text-slate-300">
                  {descriptionSnippet(event.description, 80)}
                </p>
                <div className="flex flex-wrap gap-x-3 gap-y-1 text-xs text-slate-500 dark:text-slate-400">
                  <span>Start: {formatDateTime(event.startTime)}</span>
                  {event.endTime && <span>End: {formatDateTime(event.endTime)}</span>}
                  <span>{event.timezone}</span>
                  <span>Owner: {getOwnerDisplay(users, event.ownerId)}</span>
                </div>
              </article>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}
