import type { Config } from 'tailwindcss'

const config: Config = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        tickr: {
          50: '#f3f7ff',
          100: '#e0ebff',
          200: '#bed3ff',
          300: '#92b2ff',
          400: '#5b88ff',
          500: '#315fff',
          600: '#1f44db',
          700: '#1a37ad',
          800: '#182f88',
          900: '#192a6d',
        },
      },
    },
  },
  plugins: [],
}

export default config

