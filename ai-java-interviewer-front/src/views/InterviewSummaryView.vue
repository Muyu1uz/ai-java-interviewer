<template>
  <div class="summary-container">
    <div class="summary-card">
      <div class="card-header">
        <h1>面试总结</h1>
        <p class="subtitle">本次面试已完成，以下是您的面试记录</p>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="summaryData" class="summary-content">
        <div class="user-info">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ summaryData.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">面试ID：</span>
            <span class="value">{{ summaryData.chatId }}</span>
          </div>
        </div>

        <div class="stats-section">
          <h3>技术点统计</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">{{ summaryData.count }}</div>
              <div class="stat-label">已提问技术点</div>
            </div>
          </div>
        </div>

        <div class="topics-section">
          <h3>已提问技术点列表</h3>
          <div v-if="summaryData.topics && summaryData.topics.length > 0" class="topics-list">
            <div v-for="(topic, index) in summaryData.topics" :key="index" class="topic-tag">
              {{ topic }}
            </div>
          </div>
          <div v-else class="empty-state">
            <p>暂无提问记录</p>
          </div>
        </div>

        <div class="actions">
          <button @click="handleNewInterview" class="primary-btn">
            开启新对话
          </button>
          <button @click="handleBackToUpload" class="secondary-btn">
            返回首页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAskedTopics, clearAskedTopics } from '../api/topics'
import { toast } from '../utils/toast'

interface SummaryData {
  chatId: string
  topics: string[]
  count: number
  username: string
}

const router = useRouter()
const loading = ref(true)
const summaryData = ref<SummaryData | null>(null)

onMounted(async () => {
  try {
    const result = await getAskedTopics()
    summaryData.value = result as SummaryData
  } catch (error) {
    console.error('Failed to load summary', error)
    toast.error('加载面试记录失败')
  } finally {
    loading.value = false
  }
})

const handleNewInterview = async () => {
  try {
    await clearAskedTopics()
    toast.success('已清除历史记录，开始新对话')
    router.push('/chat')
  } catch (error) {
    console.error('Failed to start new interview', error)
    toast.error('开启新对话失败')
  }
}

const handleBackToUpload = () => {
  router.push('/upload-resume')
}
</script>

<style scoped>
.summary-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.summary-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 800px;
  width: 100%;
  overflow: hidden;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, #35a372 100%);
  color: white;
  padding: 40px 30px;
  text-align: center;
}

.card-header h1 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.loading {
  padding: 60px 30px;
  text-align: center;
  color: #666;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.summary-content {
  padding: 30px;
}

.user-info {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  padding: 8px 0;
  font-size: 15px;
}

.info-item .label {
  font-weight: 600;
  color: #666;
  min-width: 100px;
}

.info-item .value {
  color: #2c3e50;
}

.stats-section,
.topics-section {
  margin-bottom: 30px;
}

.stats-section h3,
.topics-section h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: linear-gradient(135deg, var(--primary-color) 0%, #35a372 100%);
  color: white;
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(66, 185, 131, 0.3);
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.topics-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.topic-tag {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  border: 1px solid #a5d6a7;
  transition: all 0.2s;
}

.topic-tag:hover {
  background: #c8e6c9;
  transform: translateY(-2px);
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 40px;
}

.primary-btn,
.secondary-btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.primary-btn {
  background: var(--primary-color);
  color: white;
}

.primary-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.3);
}

.secondary-btn {
  background: #f5f7fa;
  color: #666;
}

.secondary-btn:hover {
  background: #eef1f6;
  color: #333;
}
</style>
