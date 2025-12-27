import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import './assets/main.css'   // Tailwind 読み込み

const app = createApp(App);
app.use(createPinia());
app.mount('#app');