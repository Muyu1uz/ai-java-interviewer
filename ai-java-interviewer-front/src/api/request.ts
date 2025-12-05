import axios from 'axios'

const request = axios.create({
  baseURL: '/api', // Use relative path to trigger Vite proxy
  timeout: 5000
})

// Request interceptor to add token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      const authValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`
      
      // Ensure headers object exists
      if (!config.headers) {
        config.headers = new axios.AxiosHeaders()
      }

      // Use set method if available (Axios 1.x), otherwise direct assignment
      if (typeof config.headers.set === 'function') {
        config.headers.set('Authorization', authValue)
      } else {
        (config.headers as any)['Authorization'] = authValue
      }

      console.log('Request Interceptor - URL:', config.url)
      console.log('Request Interceptor - Authorization Header Set:', authValue.substring(0, 20) + '...')
    } else {
      console.warn('Request Interceptor - No token found in localStorage for URL:', config.url)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor — handle unified backend format { code, message, data }
function notifyError(msg: string) {
  const anyWin = window as any
  if (anyWin.ElMessage && typeof anyWin.ElMessage.error === 'function') {
    anyWin.ElMessage.error(msg)
  } else if (anyWin.$message && typeof anyWin.$message.error === 'function') {
    anyWin.$message.error(msg)
  } else {
    // fallback to console.warn (avoid alert to keep UI clean)
    console.warn('UI Message:', msg)
  }
}

request.interceptors.response.use(
  (response) => {
    const result = response.data

    // If backend uses unified response with code/message/data
    if (result && typeof result === 'object' && Object.prototype.hasOwnProperty.call(result, 'code')) {
      if (result.code !== 200) {
        const msg = result.message || '服务器返回错误'
        notifyError(msg)
        return Promise.reject(result)
      }
      return result.data
    }

    // Fallback: return raw response data
    return result
  },
  (error) => {
    // HTTP error handling
    try {
      if (error.response && error.response.status === 401) {
        const requestUrl = (error.config && error.config.url) || ''
        // Keep previous special-case behavior for resume check
        if (requestUrl.includes('/resume/check')) {
          console.warn('401 on resume check, ignoring global logout to allow retry or progression.')
          notifyError('简历检查服务需要登录或不可用')
          return Promise.reject(error)
        }

        console.warn('401 Unauthorized detected, clearing token and redirecting to login.')
        localStorage.removeItem('token')
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    } catch (e) {
      console.error('Error in response error handler', e)
    }

    notifyError('网络错误')
    return Promise.reject(error)
  }
)

export default request
