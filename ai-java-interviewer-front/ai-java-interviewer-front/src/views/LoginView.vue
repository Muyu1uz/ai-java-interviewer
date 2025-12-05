<template>
  <div class="login-view">
    <h1>登录</h1>
    <LoginForm @login-success="handleLoginSuccess" />
    <p class="register-link" @click="navigateToRegister">没有注册？先注册</p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import LoginForm from '../components/LoginForm.vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  components: {
    LoginForm,
  },
  setup() {
    const router = useRouter();

    const handleLoginSuccess = () => {
      router.push('/upload-resume');
    };

    const navigateToRegister = () => {
      router.push('/register');
    };

    return {
      handleLoginSuccess,
      navigateToRegister,
    };
  },
});
</script>

<style scoped>
.login-view {
  background-color: white;
  padding: 20px;
  text-align: center;
}

.register-link {
  color: green;
  cursor: pointer;
}
</style>