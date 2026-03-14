import axios from 'axios'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.toString().replace(/\/$/, '') || '/tickr'

let onUnauthorized: (() => void) | null = null

export function setOnUnauthorized(callback: (() => void) | null) {
  onUnauthorized = callback
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && onUnauthorized) {
      onUnauthorized()
    }
    return Promise.reject(error)
  },
)

export function setAuthToken(token: string | null) {
  if (token) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete apiClient.defaults.headers.common.Authorization
  }
}

export function clearAuthStorage() {
  try {
    window.localStorage.removeItem('tickr_auth')
  } catch {
    // ignore
  }
}

