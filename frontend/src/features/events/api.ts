import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '../../lib/apiClient'
import type { CreateEventRequest, Event, RemindNowRequest, User } from './types'

export const EVENTS_QUERY_KEY = ['events']
export const USERS_QUERY_KEY = ['users']

export function useEvents() {
  return useQuery({
    queryKey: EVENTS_QUERY_KEY,
    queryFn: async () => {
      const { data } = await apiClient.get<Event[]>('/api/v1/event/getevents')
      return data
    },
    staleTime: 30_000,
  })
}

export function useCreateEvent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: CreateEventRequest) => {
      const { data } = await apiClient.post<Event>(
        '/api/v1/event/addevent',
        body,
      )
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EVENTS_QUERY_KEY })
      queryClient.invalidateQueries({ queryKey: ['reminders'] })
    },
  })
}

export function useDeleteEvent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (eventId: string) => {
      await apiClient.delete(`/api/v1/event/deleteevent/${eventId}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EVENTS_QUERY_KEY })
      queryClient.invalidateQueries({ queryKey: ['reminders'] })
    },
  })
}

export function useRemindNow() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (body: RemindNowRequest) => {
      await apiClient.post('/api/v1/event/remindnow', body)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reminders'] })
    },
  })
}

export function useUsers() {
  return useQuery({
    queryKey: USERS_QUERY_KEY,
    queryFn: async () => {
      const { data } = await apiClient.get<User[]>('/api/v1/user/getusers')
      return data
    },
    staleTime: 60_000,
  })
}
