import { useEffect, useState } from 'react'
import { AdminPage } from './features/admin/AdminPage'
import { RemindersPage } from './features/reminders/RemindersPage'
import { EventsPage } from './features/events/EventsPage'
import { LoginPage } from './features/auth/LoginPage'
import logo from './assets/logo.png'
import {
  clearAuthStorage,
  setAuthToken,
  setOnUnauthorized,
} from './lib/apiClient'
import { ThemeToggle } from './components/ThemeToggle'

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

type MainTab = 'reminders' | 'events' | 'admin'

function App() {
  const [auth, setAuth] = useState<AuthState | null>(getStoredAuth)
  const [activeTab, setActiveTab] = useState<MainTab>('reminders')

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
    return (
      <LoginPage
        onLoginSuccess={setAuth}
      />
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-900 dark:text-slate-100">
      <div className="border-b border-slate-200 bg-white/80 backdrop-blur dark:border-slate-700 dark:bg-slate-800/80">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2">
            <img src={logo} alt="Tickr" className="h-12 w-12 rounded-lg object-contain" />
            <div>
              <div className="flex items-baseline gap-2">
                <span className="pl-1 text-base font-bold text-slate-900 dark:text-slate-100">
                  Tickr
                </span>
              </div>
              <nav className="mt-1 flex gap-1" aria-label="Main">
                <button
                  type="button"
                  onClick={() => setActiveTab('reminders')}
                  className={`rounded-full px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-1 dark:ring-offset-slate-800 ${
                    activeTab === 'reminders'
                      ? 'bg-slate-200 text-slate-800 dark:bg-slate-600 dark:text-slate-200'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-700 dark:text-slate-400 dark:hover:bg-slate-600'
                  }`}
                >
                  Reminders
                </button>
                <button
                  type="button"
                  onClick={() => setActiveTab('events')}
                  className={`rounded-full px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-1 dark:ring-offset-slate-800 ${
                    activeTab === 'events'
                      ? 'bg-slate-200 text-slate-800 dark:bg-slate-600 dark:text-slate-200'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-700 dark:text-slate-400 dark:hover:bg-slate-600'
                  }`}
                >
                  Events
                </button>
                <button
                  type="button"
                  onClick={() => setActiveTab('admin')}
                  className={`rounded-full px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-1 dark:ring-offset-slate-800 ${
                    activeTab === 'admin'
                      ? 'bg-slate-200 text-slate-800 dark:bg-slate-600 dark:text-slate-200'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-700 dark:text-slate-400 dark:hover:bg-slate-600'
                  }`}
                >
                  Admin
                </button>
              </nav>
              <p className="py-2 pl-1 text-xs text-slate-500 dark:text-slate-400">
                {activeTab === 'reminders'
                  ? 'Human-friendly view of your scheduled nudges.'
                  : activeTab === 'events'
                    ? 'Create and manage events.'
                    : activeTab === 'admin'
                      ? 'Manage users.'
                      : ''}
              </p>
            </div>
          </div>
          <nav className="flex items-center gap-2 text-sm font-medium text-slate-600 dark:text-slate-300" aria-label="User menu">
            <ThemeToggle />
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-full border border-slate-300 bg-white px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-600 shadow-sm hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-tickr-500 focus-visible:ring-offset-2 dark:border-slate-600 dark:bg-slate-700/80 dark:text-slate-200 dark:hover:bg-slate-600/80 dark:focus-visible:ring-offset-slate-800"
            >
              Log out
            </button>
          </nav>
        </div>
      </div>

      <main className="mx-auto flex max-w-6xl flex-1 flex-col px-4 pb-8 pt-6 sm:px-6 lg:px-8">
        {activeTab === 'reminders' && <RemindersPage />}
        {activeTab === 'events' && <EventsPage />}
        {activeTab === 'admin' && <AdminPage />}
      </main>
    </div>
  )
}

export default App
