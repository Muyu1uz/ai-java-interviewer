<template>
  <Transition name="toast">
    <div v-if="visible" class="toast-container" :class="type">
      <div class="toast-content">
        <span class="toast-icon">{{ iconMap[type] }}</span>
        <span class="toast-message">{{ message }}</span>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Props {
  message: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  duration: 3000
})

const emit = defineEmits<{
  close: []
}>()

const visible = ref(true)

const iconMap = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ'
}

onMounted(() => {
  setTimeout(() => {
    visible.value = false
    setTimeout(() => {
      emit('close')
    }, 300) // Wait for animation to finish
  }, props.duration)
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  padding: 12px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  font-size: 14px;
  font-weight: 500;
  max-width: 90%;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast-icon {
  font-size: 16px;
  font-weight: bold;
}

.toast-container.success {
  background: linear-gradient(135deg, #42b983 0%, #35a372 100%);
  color: white;
}

.toast-container.error {
  background: linear-gradient(135deg, #f56c6c 0%, #e85a5a 100%);
  color: white;
}

.toast-container.warning {
  background: linear-gradient(135deg, #e6a23c 0%, #d89b38 100%);
  color: white;
}

.toast-container.info {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  color: white;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}
</style>
