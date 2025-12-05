import { createApp, type App as AppInstance } from 'vue'
import Toast from '../components/Toast.vue'

interface ToastOptions {
  message: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

let toastInstance: AppInstance | null = null

export function showToast(options: ToastOptions | string) {
  // Remove existing toast if any
  if (toastInstance) {
    toastInstance.unmount()
    const existingEl = document.getElementById('toast-mount')
    if (existingEl) existingEl.remove()
  }

  const opts = typeof options === 'string' ? { message: options } : options

  const mountNode = document.createElement('div')
  mountNode.id = 'toast-mount'
  document.body.appendChild(mountNode)

  toastInstance = createApp(Toast, {
    ...opts,
    onClose: () => {
      if (toastInstance) {
        toastInstance.unmount()
        toastInstance = null
        mountNode.remove()
      }
    }
  })

  toastInstance.mount(mountNode)
}

// Convenient shortcuts
export const toast = {
  success: (message: string, duration?: number) => showToast({ message, type: 'success', duration }),
  error: (message: string, duration?: number) => showToast({ message, type: 'error', duration }),
  warning: (message: string, duration?: number) => showToast({ message, type: 'warning', duration }),
  info: (message: string, duration?: number) => showToast({ message, type: 'info', duration })
}
