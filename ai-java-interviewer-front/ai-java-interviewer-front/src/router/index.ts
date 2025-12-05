import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import UploadResumeView from '../views/UploadResumeView.vue';

const routes = [
  {
    path: '/',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/upload',
    name: 'UploadResume',
    component: UploadResumeView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;