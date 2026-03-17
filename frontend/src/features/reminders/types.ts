export type ReminderStatus = 'PENDING' | 'SENT' | 'FAILED'

export type ReminderChannel = 'SMS' | 'PHONE' | 'EMAIL'

export interface ReminderEvent {
  id: string
  /** Event title (API returns `title`, not `name`) */
  title?: string
  description?: string
}

export interface ReminderUser {
  id: string
  name?: string
  phoneNumber?: string
  email?: string
}

export interface Reminder {
  id: string
  event?: ReminderEvent | null
  user?: ReminderUser | null
  remindAt: string
  status: ReminderStatus
  channel: ReminderChannel
  createdAt: string
}

