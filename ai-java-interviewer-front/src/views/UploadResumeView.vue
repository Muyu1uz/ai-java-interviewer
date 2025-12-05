<template>
  <div class="container">
    <div class="upload-card">
      <div class="icon-header">📄</div>
      <h2 class="form-title">上传您的简历</h2>
      <p class="subtitle">支持 PDF, DOC, DOCX 格式，文件大小不超过 10MB</p>
      
      <div 
        class="upload-area" 
        :class="{ 'is-dragover': isDragOver, 'has-file': !!file }"
        @dragover.prevent="isDragOver = true"
        @dragleave.prevent="isDragOver = false"
        @drop.prevent="handleDrop"
        @click="triggerFileInput"
      >
        <input 
          type="file" 
          ref="fileInput"
          @change="handleFileChange" 
          accept=".pdf,.doc,.docx" 
          style="display: none"
        />
        
        <div v-if="!file" class="upload-placeholder">
          <div class="upload-icon">☁️</div>
          <p>点击或拖拽文件到此处上传</p>
        </div>
        
        <div v-else class="file-info">
          <div class="file-icon">📎</div>
          <div class="file-details">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatSize(file.size) }}</span>
          </div>
          <button class="remove-btn" @click.stop="file = null">✕</button>
        </div>
      </div>

      <button @click="handleUpload" :disabled="!file || isUploading" class="upload-btn">
        {{ isUploading ? '上传分析中...' : '开始智能分析' }}
      </button>
    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="modal-overlay">
      <div class="modal-content">
        <div class="success-icon">🎉</div>
        <h3>简历分析完毕</h3>
        <p>你可以开始面试了！</p>
        <button @click="startInterview" class="start-btn">开始面试</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { uploadResume } from '../api/resume'
import { toast } from '../utils/toast'

const file = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const isDragOver = ref(false)
const isUploading = ref(false)
const showSuccessModal = ref(false)
const router = useRouter()

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    file.value = target.files[0]
  }
}

const handleDrop = (event: DragEvent) => {
  isDragOver.value = false
  if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
    file.value = event.dataTransfer.files[0]
  }
}

const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleUpload = async () => {
  if (!file.value) return
  
  isUploading.value = true
  const formData = new FormData()
  formData.append('file', file.value)

  try {
    await uploadResume(formData)
    showSuccessModal.value = true
  } catch (error) {
    console.error('Upload failed', error)
    toast.error('上传失败,请重试')
  } finally {
    isUploading.value = false
  }
}

const startInterview = () => {
  router.push('/chat')
}
</script>

<style scoped>
/* ... existing styles ... */
.upload-card {
  background: white;
  padding: 50px;
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
  text-align: center;
  margin-top: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  animation: slideUp 0.6s ease-out;
}

.icon-header {
  font-size: 48px;
  margin-bottom: 20px;
}

.subtitle {
  color: #64748b;
  margin-bottom: 30px;
}

.upload-area {
  border: 2px dashed #cbd5e1;
  padding: 40px;
  margin: 20px 0 30px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f8fafc;
  position: relative;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover, .upload-area.is-dragover {
  border-color: var(--primary-color);
  background-color: #f0fdf4;
  transform: scale(1.01);
}

.upload-area.has-file {
  border-style: solid;
  border-color: var(--primary-color);
  background-color: #fff;
}

.upload-icon {
  font-size: 40px;
  margin-bottom: 10px;
  color: var(--primary-color);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  padding: 10px;
}

.file-icon {
  font-size: 32px;
}

.file-details {
  text-align: left;
  flex: 1;
}

.file-name {
  display: block;
  font-weight: 600;
  color: var(--text-color);
}

.file-size {
  font-size: 12px;
  color: #94a3b8;
}

.remove-btn {
  background: none;
  color: #94a3b8;
  padding: 5px 10px;
  font-size: 18px;
  box-shadow: none;
}

.remove-btn:hover {
  color: #ef4444;
  background: none;
  transform: none;
  box-shadow: none;
}

.upload-btn {
  width: 100%;
  max-width: 300px;
  padding: 15px;
  font-size: 18px;
}

.upload-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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
  animation: fadeIn 0.3s ease-out;
}

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: slideUp 0.3s ease-out;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.modal-content h3 {
  color: var(--secondary-color);
  margin-bottom: 10px;
  font-size: 24px;
}

.modal-content p {
  color: #64748b;
  margin-bottom: 30px;
}

.start-btn {
  width: 100%;
  padding: 15px;
  font-size: 18px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  box-shadow: 0 4px 15px rgba(66, 185, 131, 0.4);
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(66, 185, 131, 0.5);
}
</style>
