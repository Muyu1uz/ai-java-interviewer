<template>
  <div class="login-container">
    <h1>登录</h1>
    <form @submit.prevent="handleLogin">
      <div>
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="password">密码</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit" class="login-button">登录</button>
    </form>
    <p class="register-link" @click="goToRegister">没有注册？先注册</p>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/auth';

export default {
  setup() {
    const username = ref('');
    const password = ref('');
    const router = useRouter();

    const handleLogin = async () => {
      try {
        await login(username.value, password.value);
        router.push('/upload'); // 登录成功后跳转到上传简历页面
      } catch (error) {
        console.error('登录失败:', error);
      }
    };

    const goToRegister = () => {
      router.push('/register'); // 跳转到注册页面
    };

    return {
      username,
      password,
      handleLogin,
      goToRegister,
    };
  },
};
</script>

<style scoped>
.login-container {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  margin: auto;
}

.login-button {
  background-color: green;
  color: white;
  border: none;
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 5px;
}

.register-link {
  color: green;
  cursor: pointer;
  text-align: center;
  margin-top: 10px;
}
</style>