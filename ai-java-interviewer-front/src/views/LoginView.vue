<template>
  <div class="page-wrapper">
    <div class="form-container">
      <div class="brand-header">
        <div class="logo-small">AI</div>
        <h2 class="form-title">欢迎回来</h2>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input v-model="userAccount" type="text" placeholder="请输入账号" required />
        </div>
        <div class="input-group">
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="full-width-btn">登录</button>
      </form>
      <p class="small-text">
        还没有账号？ <router-link to="/register">立即注册</router-link>
      </p>
    </div>

    <!-- Custom Modal -->
    <div v-if="showResumeModal" class="modal-overlay">
      <div class="modal-content">
        <h3>简历已存在</h3>
        <p>检测到您已经上传过简历，是否直接开始模拟面试？</p>
        <div class="modal-actions">
          <button @click="handleToUpload" class="secondary-btn">重新上传</button>
          <button @click="handleToChat" class="primary-btn">开始面试</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { login } from '../api/auth'
import { checkResume } from '../api/resume'
import { toast } from '../utils/toast'

const userAccount = ref('')
const password = ref('')
const showResumeModal = ref(false)
const router = useRouter()
const authStore = useAuthStore()

const handleToChat = () => {
  showResumeModal.value = false
  router.push('/chat')
}

const handleToUpload = () => {
  showResumeModal.value = false
  router.push('/upload-resume')
}

const handleLogin = async () => {
  try {
    // Real code:
    // Send both userAccount and username just in case
    const res = await login({ 
      userAccount: userAccount.value, 
      username: userAccount.value, 
      password: password.value 
    })
    
    console.log('Login response:', res)

    let token = ''
    // Handle various response structures
    if (res) {
      if (typeof res === 'string') {
        token = res
      } else if (res.data) {
        if (typeof res.data === 'string') {
          token = res.data
        } else if (typeof res.data === 'object' && res.data.token) {
          token = res.data.token
        }
      } else if (res.token) {
        token = res.token
      }
    }

    if (token && typeof token === 'string') {
         // Clean up token if it has extra quotes
         if (token.startsWith('"') && token.endsWith('"')) {
           token = token.slice(1, -1)
         }
         console.log('Token extracted (final):', token)
         authStore.setToken(token)
         
         // Check if resume exists
         try {
           const resumeRes = await checkResume()
           console.log('Resume check response:', resumeRes)
           // Handle both raw boolean or wrapped response
           const hasResume = resumeRes === true || (resumeRes && resumeRes.data === true)
           
           if (hasResume) {
             showResumeModal.value = true
             return
           }
         } catch (e) {
           console.error('Failed to check resume status', e)
           // Optional: Notify user about the glitch but continue
           // alert('简历状态检查服务暂时不可用，将直接进入上传页面。')
         }
    } else {
         console.error('Token not found in response', res)
         throw new Error('Token not found in response')
    }
    
    router.push('/upload-resume')
  } catch (error) {
    console.error('Login failed', error)
    toast.error('登录失败,请检查用户名和密码')
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
  box-shadow: 0 4px 10px rgba(66, 185, 131, 0.3);
}

.full-width-btn {
  width: 100%;
  margin-top: 10px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  animation: slideUp 0.3s ease-out;
}

.modal-content h3 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1.2rem;
}

.modal-content p {
  color: #666;
  margin-bottom: 25px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.modal-actions button {
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  border: none;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 14px;
}

.primary-btn {
  background: var(--primary-color);
  color: white;
}

.secondary-btn {
  background: #f5f7fa;
  color: #666;
}

.primary-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.2);
}

.secondary-btn:hover {
  background: #eef1f6;
  color: #333;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
