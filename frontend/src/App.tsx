import { useEffect, useState } from 'react'
import { RemindersPage } from './features/reminders/RemindersPage'
import { LoginPage } from './features/auth/LoginPage'
import logo from './assets/logo.png'
import {
  clearAuthStorage,
  setAuthToken,
  setOnUnauthorized,
} from './lib/apiClient'

interface AuthState {
  accessToken: string
  tokenType: string
  expiresInMs: number
}

function getStoredAuth(): AuthState | null {
  try {
    const stored = window.localStorage.getItem('tickr_auth')
    return stored ? (JSON.parse(stored) as AuthState) : null
  } catch {
    return null
  }
}

function App() {
  const [auth, setAuth] = useState<AuthState | null>(getStoredAuth)

  useEffect(() => {
    if (auth) setAuthToken(auth.accessToken)
    else setAuthToken(null)
  }, [auth])

  const handleLogout = () => {
    setAuth(null)
    setAuthToken(null)
    clearAuthStorage()
  }

  useEffect(() => {
    setOnUnauthorized(() => {
      setAuth(null)
      setAuthToken(null)
      clearAuthStorage()
    })
    return () => setOnUnauthorized(null)
  }, [])

  if (!auth) {
    return <LoginPage onLoginSuccess={setAuth} />
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <div className="border-b border-slate-200 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2">
            <img src={logo} alt="Tickr" className="h-12 w-12 rounded-lg object-contain" />
            <div>
              <div className="flex items-baseline gap-2">
                <span className="text-base font-semibold tracking-tight text-slate-900">
                  Tickr
                </span>
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-slate-500">
                  Reminders
                </span>
              </div>
              <p className="text-xs text-slate-500">
                Human-friendly view of your scheduled nudges.
              </p>
            </div>
          </div>
          <nav className="flex items-center text-sm font-medium text-slate-600" aria-label="User menu">
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-full border border-slate-300 bg-white px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-600 shadow-sm hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2"
            >
              Log out
            </button>
          </nav>
        </div>
      </div>

      <main className="mx-auto flex max-w-6xl flex-1 flex-col px-4 pb-8 pt-6 sm:px-6 lg:px-8">
        <RemindersPage />
      </main>
    </div>
  )
}

export default App
