/** @type {import('tailwindcss').Config} */
console.log("Tailwind config loaded");
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx,css}",
  ],
  theme: {
    extend: {
      colors: {
        // Material Design 3 color scheme
        primary: '#6750A4',
        'primary-container': '#E8DEF8',
        secondary: '#625B71',
        'secondary-container': '#E8DEF8',
        surface: '#FFF',
        'surface-container': '#ECE6F0',
        'surface-container-low': '#F7F2FA',
        'surface-container-lowest': '#FFF',
        'surface-container-high': '#ECE6F0',
        'on-surface': '#1D1B20',
        'on-surface-variant': '#49454F',
        'outline-variant': '#CAC4D0',
        'inverse-on-surface': '#F5EFF7',
        // Custom gradient colors
        'gradient-start': '#FDE0FF',
        'gradient-end': '#C18CD9',

        'warm': '#fef6f0',
      },
      fontFamily: {
        roboto: ['Roboto', 'system-ui', 'sans-serif'],
      },
      fontSize: {
        'title-large': ['22px', { lineHeight: '28px', letterSpacing: '0' }],
        'title-medium': ['16px', { lineHeight: '24px', letterSpacing: '0.15px' }],
        'label-large': ['14px', { lineHeight: '20px', letterSpacing: '0.1px' }],
        'label-medium': ['12px', { lineHeight: '16px', letterSpacing: '0.5px' }],
        'label-small': ['11px', { lineHeight: '16px', letterSpacing: '0.5px' }],
      },
      boxShadow: {
        'elevation-1': '0 1px 2px 0 rgba(0, 0, 0, 0.30), 0 2px 6px 2px rgba(0, 0, 0, 0.15)',
      },
      borderRadius: {
        '18': '18px',
      },
    },
  },
  plugins: [
    function ({ addUtilities }) {
      // 完全な自作クラスは以下に追加する
      const newUtilities = {
        // ボーダーにfrom green-400 to teal-400 のグラデーション
        '.border-gradient': {
          borderImage: 'linear-gradient(to right, #4ade80, #2dd4bf) 1',
        },
      };

      addUtilities(newUtilities);
    },
  ],
}