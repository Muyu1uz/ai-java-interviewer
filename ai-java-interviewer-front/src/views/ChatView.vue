<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <div class="avatar ai-avatar">AI</div>
        <div class="header-info">
          <h2>AI 面试官</h2>
          <span class="status-dot"></span> 在线
        </div>
      </div>
      <div class="header-actions">
        <button @click="handleRestart" class="action-btn restart-btn" :disabled="isThinking || isStreaming">重新对话</button>
        <button @click="handleEnd" class="action-btn end-btn" :disabled="isThinking || isStreaming">结束对话</button>
      </div>
    </div>

    <div class="messages-area" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
        <div class="avatar" :class="msg.role === 'ai' ? 'ai-avatar' : 'user-avatar'">
          {{ msg.role === 'ai' ? 'AI' : 'Me' }}
        </div>
        <div class="message-bubble">
          <div class="message-content" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>
      <div v-if="isThinking" class="message-row ai">
        <div class="avatar ai-avatar">AI</div>
        <div class="message-bubble thinking">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="input-wrapper">
        <textarea 
          v-model="userInput" 
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入您的回答..."
          :disabled="isThinking || isStreaming"
          rows="1"
          ref="textarea"
        ></textarea>
        <button @click="sendMessage" :disabled="!userInput.trim() || isThinking || isStreaming" class="send-btn">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchStream } from '../utils/stream'
import { clearAskedTopics } from '../api/topics'
import { toast } from '../utils/toast'

interface Message {
  role: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isThinking = ref(false)
const isStreaming = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const textarea = ref<HTMLTextAreaElement | null>(null)
const router = useRouter()

const handleEnd = async () => {
  try {
    // 不在此处清除记录，直接跳转到总结页面，用户在总结页可选择开启新对话（点击后再清除）
    toast.success('对话已结束，跳转到总结页面')
    router.push('/interview-summary')
  } catch (error) {
    console.error('Failed to navigate to summary', error)
    toast.error('结束对话失败')
  }
}

const handleRestart = async () => {
  try {
    await clearAskedTopics()
    messages.value = []
    userInput.value = ''
    toast.success('已清除历史记录，开始新对话')
    await startInterview()
  } catch (error) {
    console.error('Failed to restart conversation', error)
    toast.error('重新开始失败')
  }
}

// Auto-resize textarea
watch(userInput, () => {
  if (textarea.value) {
    textarea.value.style.height = 'auto'
    textarea.value.style.height = textarea.value.scrollHeight + 'px'
  }
})

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatMessage = (content: string) => {
  // Simple formatting, replace newlines with <br>
  return content.replace(/\n/g, '<br>')
}

const startInterview = async () => {
  isThinking.value = true
  
  try {
    await fetchStream('/interview-chat/start', { method: 'POST' }, (chunk) => {
      if (isThinking.value) {
        isThinking.value = false
        isStreaming.value = true
        messages.value.push({ role: 'ai', content: '' })
      }
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.content += chunk
      scrollToBottom()
    })
  } catch (error) {
    console.error('Failed to start interview', error)
    if (isThinking.value) {
       isThinking.value = false
       messages.value.push({ role: 'ai', content: '' })
    }
    const lastMsg = messages.value[messages.value.length - 1]
    lastMsg.content = '面试启动失败，请刷新重试。'
  } finally {
    isThinking.value = false
    isStreaming.value = false
  }
}

const sendMessage = async () => {
  const content = userInput.value.trim()
  if (!content || isThinking.value || isStreaming.value) return

  // Add user message
  messages.value.push({ role: 'user', content })
  userInput.value = ''
  if (textarea.value) textarea.value.style.height = 'auto'
  scrollToBottom()

  isThinking.value = true

  try {
    // Backend expects 'userInput' as a request parameter
    await fetchStream(`/interview-chat/continue?userInput=${encodeURIComponent(content)}`, {
      method: 'POST'
    }, (chunk) => {
      if (isThinking.value) {
        isThinking.value = false
        isStreaming.value = true
        messages.value.push({ role: 'ai', content: '' })
      }
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.content += chunk
      scrollToBottom()
    })
  } catch (error) {
    console.error('Failed to send message', error)
    if (isThinking.value) {
       isThinking.value = false
       messages.value.push({ role: 'ai', content: '' })
    }
    const lastMsg = messages.value[messages.value.length - 1]
    lastMsg.content += '\n[发送失败，请重试]'
  } finally {
    isThinking.value = false
    isStreaming.value = false
  }
}

onMounted(() => {
  startInterview()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-header {
  background: white;
  padding: 15px 20px;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.restart-btn {
  background: var(--primary-color);
  color: white;
}

.restart-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.end-btn {
  background: #f56c6c;
  color: white;
}

.end-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.header-info h2 {
  margin: 0;
  font-size: 18px;
  color: var(--secondary-color);
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #42b983;
  border-radius: 50%;
  margin-right: 5px;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-row {
  display: flex;
  gap: 15px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.ai-avatar {
  background: var(--primary-color);
  color: white;
}

.user-avatar {
  background: #35495e;
  color: white;
}

.message-bubble {
  background: white;
  padding: 15px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  line-height: 1.6;
  position: relative;
  max-width: 80%;
}

.message-row.user .message-bubble {
  background: #e3f2fd; /* Light blue for user */
  border-top-right-radius: 2px;
}

.message-row.ai .message-bubble {
  border-top-left-radius: 2px;
}

.input-area {
  background: white;
  padding: 20px;
  border-top: 1px solid #e2e8f0;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  display: flex;
  gap: 10px;
  background: #f8fafc;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  padding: 10px;
  font-family: inherit;
  font-size: 16px;
  max-height: 150px;
  outline: none;
  margin: 0; /* Override global input margin */
}

textarea:focus {
  box-shadow: none;
}

.send-btn {
  align-self: flex-end;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
}

.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Thinking animation */
.thinking .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #94a3b8;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.thinking .dot:nth-child(1) { animation-delay: -0.32s; }
.thinking .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
