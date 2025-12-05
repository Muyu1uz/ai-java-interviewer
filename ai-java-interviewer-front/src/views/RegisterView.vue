<template>
  <div class="page-wrapper">
    <div class="form-container">
      <div class="brand-header">
        <div class="logo-small">AI</div>
        <h2 class="form-title">创建账号</h2>
      </div>
      <form @submit.prevent="handleRegister">
        <input v-model="username" type="text" placeholder="昵称/用户名" required />
        <input v-model="userAccount" type="text" placeholder="账号" required />
        <input v-model="password" type="password" placeholder="密码" required />
        <button type="submit" class="full-width-btn">立即注册</button>
      </form>
      <p class="small-text">
        已有账号？ <router-link to="/login">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { toast } from '../utils/toast'

const userAccount = ref('')
const username = ref('')
const password = ref('')
const router = useRouter()

const handleRegister = async () => {
  try {
    await register({ 
      userAccount: userAccount.value, 
      username: username.value, 
      password: password.value,
      checkPassword: password.value
    })
    toast.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('Register failed', error)
    toast.error('注册失败')
  }
}
</script>

<style scoped>
.page-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}

.brand-header {
  margin-bottom: 30px;
}

.logo-small {
  width: 50px;
  height: 50px;
  background: var(--primary-color);
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 20px;
  margin: 0 auto 15px;
}

.full-width-btn {
  width: 100%;
  margin-top: 10px;
}
</style>
