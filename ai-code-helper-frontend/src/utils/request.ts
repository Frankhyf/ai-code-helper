import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取用户信息，添加认证token（如果后端需要）
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        // 如果后端使用 token 认证，可以在这里添加
        // config.headers['Authorization'] = `Bearer ${user.token}`
      } catch (e) {
        console.warn('解析用户信息失败:', e)
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 记录原始响应（仅在开发环境）
    if (import.meta.env.DEV) {
      console.log('API响应:', response.config.url, response.data)
    }
    
    const { code, message, data } = response.data
    
    // 成功响应（后端成功返回 code: 200）
    if (code === 200 || code === 0) {
      return data
    }
    
    // 业务错误
    console.error('业务错误:', { code, message, data })
    showToast(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const userStore = useUserStore()
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          showToast('请先登录')
          userStore.logout(false)
          break
        case 403:
          showToast('没有权限')
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      showToast('请求超时')
    } else if (error.code === 'ERR_NETWORK') {
      // 后端服务未启动时不显示错误提示，仅在控制台输出
      console.warn('后端服务未连接:', error.message)
    } else {
      // 其他网络错误也只在控制台输出
      console.warn('网络请求错误:', error.message)
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
export const http = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => 
    request.get<T, T>(url, config),
  
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => 
    request.post<T, T>(url, data, config),
  
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => 
    request.put<T, T>(url, data, config),
  
  delete: <T = any>(url: string, config?: AxiosRequestConfig) => 
    request.delete<T, T>(url, config),
  
  // SSE请求
  sse: (
    url: string,
    onMessage: (data: any, event?: MessageEvent) => void,
    onError?: (error: any) => void,
    onComplete?: (data: any) => void,
    onBusinessError?: (errorData: { error: boolean; code: number; message: string }) => void
  ) => {
    const eventSource = new EventSource(`${request.defaults.baseURL}${url}`, {
      withCredentials: true,
    })
    
    let streamCompleted = false
    
    eventSource.onmessage = (event) => {
      if (streamCompleted) return
      
      let payload: any = event.data
      try {
        payload = JSON.parse(event.data)
      } catch (error) {
        // 保留原始字符串
      }
      onMessage(payload, event)
    }
    
    // 监听业务错误事件（后端限流、权限等错误）
    eventSource.addEventListener('business-error', (event: MessageEvent) => {
      if (streamCompleted) return
      
      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)
        
        streamCompleted = true
        eventSource.close()
        
        // 如果提供了业务错误处理器，使用它
        if (onBusinessError) {
          onBusinessError(errorData)
        } else {
          // 默认处理：显示错误提示并调用onError
          const errorMessage = errorData.message || '请求失败'
          showToast(errorMessage)
          if (onError) {
            onError(new Error(errorMessage))
          }
        }
      } catch (parseError) {
        console.error('解析业务错误事件失败:', parseError, '原始数据:', event.data)
        if (onError) {
          onError(new Error('服务器返回错误'))
        }
        eventSource.close()
      }
    })
    
    eventSource.addEventListener('done', (event) => {
      if (streamCompleted) return
      
      streamCompleted = true
      let payload: any = event.data
      try {
        payload = JSON.parse(event.data)
      } catch (error) {
        // ignore
      }
      onComplete?.(payload)
      eventSource.close()
    })
    
    eventSource.onerror = (error) => {
      if (streamCompleted) return
      
      streamCompleted = true
      if (onError) onError(error)
      eventSource.close()
    }
    
    return eventSource
  }
}

export default request