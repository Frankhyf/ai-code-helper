import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import type { User, LoginForm, RegisterForm, UserUpdateMyForm } from '@/types'
import { http } from '@/utils/request'
import { showToast } from 'vant'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref<User | null>(null)
  const isLogin = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  const setUser = (userData: User | null) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
  }

  const transformToUser = (data: any): User => ({
    id: data?.id ?? 0,
    account: data?.userAccount ?? data?.account ?? '',
    nickname: data?.userName ?? data?.nickname ?? data?.userAccount ?? '',
    avatar: data?.userAvatar ?? data?.avatar,
    profile: data?.userProfile ?? data?.profile ?? '',
    role: data?.userRole === 'admin' ? 'admin' : 'user',
    createTime: data?.createTime ?? '',
    updateTime: data?.updateTime ?? '',
  })

  // 登录
  const login = async (form: LoginForm) => {
    try {
      // 后端需要的字段名：userAccount, userPassword
      const loginPayload = {
        userAccount: form.account,
        userPassword: form.password,
      }
      const userData = await http.post('/user/login', loginPayload)
      setUser(transformToUser(userData))
      showToast('登录成功')
      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  // 注册
  const register = async (form: RegisterForm) => {
    try {
      // 后端需要的字段名：userAccount, userPassword, checkPassword
      const registerPayload = {
        userAccount: form.account,
        userPassword: form.password,
        checkPassword: form.confirmPassword,
      }
      await http.post('/user/register', registerPayload)
      showToast('注册成功，请登录')
      return true
    } catch (error) {
      console.error('注册失败:', error)
      return false
    }
  }

  // 登出
  const logout = async (showMessage = true) => {
    try {
      await http.post('/user/logout')
    } catch (error) {
      console.warn('退出登录失败:', error)
    } finally {
      setUser(null)
      if (showMessage) {
        showToast('已退出登录')
      }
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const userData = await http.get('/user/get/login')
      const normalized = transformToUser(userData)
      setUser(normalized)
      return normalized
    } catch (error) {
      console.error('获取用户信息失败:', error)
      setUser(null)
      return null
    }
  }

  // 初始化用户状态
  const initUser = () => {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      user.value = JSON.parse(savedUser)
    }
    // 再从服务端验证一次会话状态
    getUserInfo()
  }

  // 更新用户自己的信息
  const updateMyInfo = async (form: UserUpdateMyForm) => {
    try {
      await http.post('/user/update/my', form)
      // 更新成功后刷新用户信息
      await getUserInfo()
      showToast('更新成功')
      return true
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return false
    }
  }

  // 上传头像
  const uploadAvatar = async (file: File) => {
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      // 使用 fetch 发送 multipart/form-data 请求
      const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
      const response = await fetch(`${baseUrl}/user/upload/avatar`, {
        method: 'POST',
        body: formData,
        credentials: 'include' // 携带 cookie
      })
      
      const data = await response.json()
      
      if (data.code === 0 || data.code === 200) {
        // 上传成功，刷新用户信息（后端已自动更新头像字段）
        await getUserInfo()
        showToast('头像上传成功')
        return data.data as string // 返回头像 URL
      } else {
        showToast(data.message || '头像上传失败')
        return null
      }
    } catch (error) {
      console.error('上传头像失败:', error)
      showToast('头像上传失败，请重试')
      return null
    }
  }

  return {
    user,
    isLogin,
    isAdmin,
    login,
    register,
    logout,
    getUserInfo,
    initUser,
    updateMyInfo,
    uploadAvatar,
  }
})