/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // TradeMind AI palette — deep space navy base with a violet/blue
        // "signal" gradient and a single acid-green used only for gains.
        ink: {
          950: '#05070D',   // page background
          900: '#0A0E17',   // panel background
          800: '#111624',   // card background
          700: '#1B2233',   // borders / dividers
          600: '#2A3348',
        },
        signal: {
          blue: '#4C6FFF',
          violet: '#8B5CF6',
          cyan: '#39D0D8',
        },
        gain: '#22D3A6',   // profit / bullish
        loss: '#FB5A5A',   // loss / bearish
        amber: '#F5B94D',  // neutral alerts
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      boxShadow: {
        glass: '0 8px 32px rgba(0, 0, 0, 0.45)',
        glow: '0 0 0 1px rgba(139, 92, 246, 0.15), 0 8px 24px rgba(76, 111, 255, 0.15)',
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
}
