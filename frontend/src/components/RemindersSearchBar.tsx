import type { ReminderChannel, ReminderStatus } from '../features/reminders/types'

export type StatusFilter = ReminderStatus | 'ALL'
export type ChannelFilter = ReminderChannel | 'ALL'

export interface RemindersFilterState {
  search: string
  status: StatusFilter
  channel: ChannelFilter
}

const STATUS_OPTIONS: StatusFilter[] = ['ALL', 'PENDING', 'SENT', 'FAILED']
const CHANNEL_OPTIONS: ChannelFilter[] = ['ALL', 'SMS', 'PHONE', 'EMAIL']

interface RemindersSearchBarProps {
  filters: RemindersFilterState
  onFiltersChange: (updater: (prev: RemindersFilterState) => RemindersFilterState) => void
}

export function RemindersSearchBar({ filters, onFiltersChange }: RemindersSearchBarProps) {
  return (
    <section className="flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-600 dark:bg-slate-800">
      <div className="flex-1 min-w-[220px]">
        <label
          htmlFor="reminders-search"
          className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400"
        >
          Search
        </label>
        <input
          id="reminders-search"
          type="search"
          placeholder="Search by event, user, email, phone…"
          className="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 placeholder:text-slate-400 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100 dark:placeholder:text-slate-500 dark:ring-slate-600 dark:focus:border-tickr-400 dark:focus:ring-tickr-900/50"
          value={filters.search}
          onChange={(e) => onFiltersChange((prev) => ({ ...prev, search: e.target.value }))}
        />
      </div>

      <div>
        <label
          htmlFor="reminders-status"
          className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400"
        >
          Status
        </label>
        <select
          id="reminders-status"
          className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100 dark:ring-slate-600 dark:focus:border-tickr-400 dark:focus:ring-tickr-900/50"
          value={filters.status}
          onChange={(e) =>
            onFiltersChange((prev) => ({
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
        <label
          htmlFor="reminders-channel"
          className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400"
        >
          Channel
        </label>
        <select
          id="reminders-channel"
          className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none ring-slate-300 focus:border-tickr-500 focus:ring-2 focus:ring-tickr-200 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100 dark:ring-slate-600 dark:focus:border-tickr-400 dark:focus:ring-tickr-900/50"
          value={filters.channel}
          onChange={(e) =>
            onFiltersChange((prev) => ({
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
  )
}
