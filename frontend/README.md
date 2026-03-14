## Tickr Frontend (Reminders UI)

This is a small React + TypeScript frontend for viewing and managing **reminders** from the Tickr backend.

It is intentionally focused on a single use case: listing reminders, searching/filtering, and deleting individual reminders.

---

## Tech stack

- **Build tool**: Vite
- **UI**: React 19 + TypeScript
- **Styling**: Tailwind CSS
- **Data fetching**: Axios
- **Server state / caching**: TanStack Query (React Query)

---

## Running the frontend

### 1. Start the backend

From the repo root:

```bash
./mvnw spring-boot:run
```

The backend is expected to be available at:

- `http://localhost:8080/tickr/api/v1/...`

### 2. Start the frontend dev server

From the `frontend` directory:

```bash
cd frontend
npm install    # first time only
npm run dev
```

Then open the printed URL in your browser (usually `http://localhost:5173`).

---

## API base URL & CORS

In development, the frontend is configured to **proxy** API calls beginning with `/tickr` to the backend on `http://localhost:8080` via Vite:

- See `vite.config.ts` for `server.proxy['/tickr']`.

The API client uses:

- `VITE_API_BASE_URL` (if set) **or**
- `/tickr` (default) – which works with the dev proxy and avoids CORS issues without changing the backend.

If you want to point the frontend at a different backend host/port, create a `.env` file in `frontend`:

```bash
VITE_API_BASE_URL=http://localhost:8080/tickr
```

---

## Reminders UI

The reminders page (`RemindersPage`) provides:

- **List view** of reminders with:
  - Event name and user info
  - `remindAt` and `createdAt` times
  - Channel (SMS / PHONE / EMAIL)
  - Status (PENDING / SENT / FAILED)
- **Search** across event + user fields
- **Filter** by:
  - Status
  - Channel
- **Sort** by `remindAt` (ascending, soonest first)
- **Refresh** button to refetch from the backend
- **Delete** action with a browser confirmation prompt
- **Empty / loading / error** states

The layout is:

- **Table** view on larger screens
- **Card** layout on smaller screens (mobile-friendly)

---

## Build

To build a production bundle:

```bash
cd frontend
npm run build
```

You can then preview the production build locally:

```bash
npm run preview
```

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
