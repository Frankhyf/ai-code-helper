<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12">
    <div class="max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-8">
        <router-link to="/" class="flex items-center justify-center space-x-2">
          <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center">
            <span class="text-white font-bold text-xl">AI</span>
          </div>
          <span class="text-2xl font-bold gradient-text">应用生成平台</span>
        </router-link>
      </div>

      <!-- 注册表单卡片 -->
      <div class="glass-dark rounded-2xl shadow-2xl p-8">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold gradient-text mb-2">创建账号</h2>
          <p class="text-gray-400">开始您的AI应用生成之旅</p>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-6">
          <!-- 账号输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              账号
            </label>
            <div class="relative">
              <input
                v-model="form.account"
                type="text"
                placeholder="请输入账号（3-20位字符）"
                class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-500': errors.account }"
              />
              <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
            </div>
            <p v-if="errors.account" class="mt-1 text-sm text-red-400">{{ errors.account }}</p>
          </div>

          <!-- 昵称输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              昵称
            </label>
            <div class="relative">
              <input
                v-model="form.nickname"
                type="text"
                placeholder="请输入昵称（可选）"
                class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-500': errors.nickname }"
              />
              <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
                </svg>
              </div>
            </div>
            <p v-if="errors.nickname" class="mt-1 text-sm text-red-400">{{ errors.nickname }}</p>
          </div>

          <!-- 密码输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              密码
            </label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码（至少8位）"
                class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-500': errors.password }"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="!showPassword" class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"></path>
                </svg>
                <svg v-else class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-400">{{ errors.password }}</p>
          </div>

          <!-- 确认密码输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              确认密码
            </label>
            <div class="relative">
              <input
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-500': errors.confirmPassword }"
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="!showConfirmPassword" class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"></path>
                </svg>
                <svg v-else class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-400">{{ errors.confirmPassword }}</p>
          </div>

          <!-- 服务条款 -->
          <div class="flex items-start">
            <input
              v-model="agreeTerms"
              type="checkbox"
              class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label class="ml-2 text-sm text-gray-300">
              我已阅读并同意
              <a href="#" class="text-blue-400 hover:text-blue-300">服务条款</a>
              和
              <a href="#" class="text-blue-400 hover:text-blue-300">隐私政策</a>
            </label>
          </div>

          <!-- 注册按钮 -->
          <button
            type="submit"
            :disabled="loading || !form.account || !form.password || !form.confirmPassword || !agreeTerms"
            class="w-full bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-4 rounded-lg transition-all glow-button"
          >
            <span v-if="!loading">注 册</span>
            <span v-else class="flex items-center justify-center">
              <div class="loading-spinner mr-2 !w-5 !h-5"></div>
              注册中...
            </span>
          </button>
        </form>

        <!-- 分割线 -->
        <div class="mt-8 flex items-center">
          <div class="flex-1 border-t border-gray-600"></div>
          <span class="px-3 text-sm text-gray-400">或者</span>
          <div class="flex-1 border-t border-gray-600"></div>
        </div>

        <!-- 登录链接 -->
        <div class="mt-6 text-center">
          <p class="text-gray-400">
            已有账号？
            <router-link 
              to="/login" 
              class="text-blue-400 hover:text-blue-300 font-medium"
            >
              立即登录
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RegisterForm } from '@/types'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const form = reactive<RegisterForm>({
  account: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 表单验证错误
const errors = reactive({
  account: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 其他状态
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreeTerms = ref(false)

// 表单验证
const validateForm = () => {
  let isValid = true
  
  // 重置错误
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })
  
  // 验证账号
  if (!form.account.trim()) {
    errors.account = '请输入账号'
    isValid = false
  } else if (form.account.length < 3 || form.account.length > 20) {
    errors.account = '账号长度应在3-20位之间'
    isValid = false
  } else if (!/^[a-zA-Z0-9_]+$/.test(form.account)) {
    errors.account = '账号只能包含字母、数字和下划线'
    isValid = false
  }
  
  // 验证昵称
  if (form.nickname && form.nickname.length > 20) {
    errors.nickname = '昵称长度不能超过20位'
    isValid = false
  }
  
  // 验证密码
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 8) {
    errors.password = '密码长度至少8位'
    isValid = false
  } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/.test(form.password)) {
    errors.password = '密码必须包含大小写字母和数字'
    isValid = false
  }
  
  // 验证确认密码
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }
  
  return isValid
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) {
    return
  }

  if (!agreeTerms.value) {
    showToast('请同意服务条款和隐私政策')
    return
  }

  loading.value = true

  try {
    const success = await userStore.register(form)
    
    if (success) {
      showToast('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败:', error)
    showToast('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>