<template>
  <div class="register-form">
    <h2>注册</h2>
    <form @submit.prevent="handleRegister">
      <div>
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="email">邮箱</label>
        <input type="email" id="email" v-model="email" required />
      </div>
      <div>
        <label for="password">密码</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit" class="submit-button">注册</button>
    </form>
    <p class="register-link" @click="goToLogin">没有注册？先注册</p>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '../api/auth';

export default {
  setup() {
    const username = ref('');
    const email = ref('');
    const password = ref('');
    const router = useRouter();

    const handleRegister = async () => {
      try {
        await register({ username: username.value, email: email.value, password: password.value });
        alert('注册成功！请登录。');
        router.push('/login');
      } catch (error) {
        alert('注册失败，请重试。');
      }
    };

    const goToLogin = () => {
      router.push('/login');
    };

    return {
      username,
      email,
      password,
      handleRegister,
      goToLogin,
    };
  },
};
</script>

<style scoped>
.register-form {
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.submit-button {
  background-color: green;
  color: white;
  border: none;
  padding: 10px 15px;
  cursor: pointer;
}

.register-link {
  color: green;
  cursor: pointer;
  text-align: center;
  margin-top: 10px;
}
</style>