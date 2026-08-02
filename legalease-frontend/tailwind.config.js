export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],

  theme: {
    extend: {
      colors: {
        ink:          '#1A1A2E',
        'ink-light':  '#16213E',
        surface:      '#F8F7F4',
        'surface-alt':'#EFEEE9',
        accent:       '#C9A84C',
        'accent-soft':'#F0E6C8',
        danger:       '#C0392B',
        success:      '#1E8449',
        border:       '#D9D7D0',
        text:         '#2C2C2C',
        'text-muted': '#6B6B6B',
      },
      fontFamily: {
        display: ['"Playfair Display"', 'serif'],
        body:    ['"Inter"', 'sans-serif'],
        mono:    ['"JetBrains Mono"', 'monospace'],
      }
    }
  },
  plugins: []
}