import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './stores/auth';
import './assets/base.css';
import './assets/main.css';

const app = createApp(App);

app.use(router);
app.use(store);

app.mount('#app');