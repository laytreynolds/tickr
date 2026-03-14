import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '../../lib/apiClient'
import type { Reminder } from './types'

const REMINDERS_QUERY_KEY = ['reminders']

export function useReminders() {
  return useQuery({
    queryKey: REMINDERS_QUERY_KEY,
    queryFn: async () => {
      const { data } = await apiClient.get<Reminder[]>(
        '/api/v1/reminder/reminders',
      )
      return data
    },
    staleTime: 30_000,
  })
}

export function useDeleteReminder() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(`/api/v1/reminder/reminders/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: REMINDERS_QUERY_KEY })
    },
  })
}

