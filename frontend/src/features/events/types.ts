/** API response shape (camelCase). */
export interface Event {
  id: string
  ownerId: string
  assignedUserIds: string[]
  title: string
  description?: string | null
  startTime: string
  endTime?: string | null
  timezone: string
  source: number
  createdAt: string
}

/** Reminder channel values accepted by the API (create event). */
export type ReminderChannel = 'SMS' | 'PHONE' | 'EMAIL'

/** Request body for POST addevent (snake_case). */
export interface CreateEventRequest {
  owner_id: string
  assigned_user_ids: string[]
  title: string
  description?: string
  start_time: string
  end_time?: string
  source: string
  timezone: string
  /** Channels for reminders (SMS, PHONE, EMAIL). If empty, backend defaults to EMAIL. */
  channels?: ReminderChannel[]
}

/** User from GET getusers for owner/assignee dropdowns. */
export interface User {
  id: string
  phoneNumber?: string | null
  timezone?: string | null
  createdAt: string
}
