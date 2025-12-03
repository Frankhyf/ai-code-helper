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

      <!-- 登录表单卡片 -->
      <div class="glass-dark rounded-2xl shadow-2xl p-8">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold gradient-text mb-2">欢迎回来</h2>
          <p class="text-gray-400">登录您的账号开始使用</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- 账号输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              账号
            </label>
            <div class="relative">
              <input
                v-model="form.account"
                type="text"
                placeholder="请输入账号"
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

          <!-- 密码输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              密码
            </label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
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

          <!-- 记住我选项 -->
          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input
                v-model="rememberMe"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-300">记住我</span>
            </label>
            
            <a href="#" class="text-sm text-blue-400 hover:text-blue-300">
              忘记密码？
            </a>
          </div>

          <!-- 登录按钮 -->
          <button
            type="submit"
            :disabled="loading || !form.account || !form.password"
            class="w-full bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-4 rounded-lg transition-all glow-button"
          >
            <span v-if="!loading">登 录</span>
            <span v-else class="flex items-center justify-center">
              <div class="loading-spinner mr-2 !w-5 !h-5"></div>
              登录中...
            </span>
          </button>
        </form>

        <!-- 分割线 -->
        <div class="mt-8 flex items-center">
          <div class="flex-1 border-t border-gray-600"></div>
          <span class="px-3 text-sm text-gray-400">或者</span>
          <div class="flex-1 border-t border-gray-600"></div>
        </div>

        <!-- 注册链接 -->
        <div class="mt-6 text-center">
          <p class="text-gray-400">
            还没有账号？
            <router-link 
              to="/register" 
              class="text-blue-400 hover:text-blue-300 font-medium"
            >
              立即注册
            </router-link>
          </p>
        </div>

        <!-- 游客访问 -->
        <div class="mt-4 text-center">
          <router-link 
            to="/" 
            class="text-sm text-gray-400 hover:text-gray-300"
          >
            游客访问 →
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { LoginForm } from '@/types'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const form = reactive<LoginForm>({
  account: '',
  password: ''
})

// 表单验证错误
const errors = reactive({
  account: '',
  password: ''
})

// 其他状态
const loading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)

// 表单验证
const validateForm = () => {
  let isValid = true
  
  // 验证账号
  if (!form.account.trim()) {
    errors.account = '请输入账号'
    isValid = false
  } else {
    errors.account = ''
  }
  
  // 验证密码
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度至少6位'
    isValid = false
  } else {
    errors.password = ''
  }
  
  return isValid
}

// 登录处理
const handleLogin = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const success = await userStore.login(form)
    
    if (success) {
      showToast('登录成功')
      
      // 跳转到之前访问的页面或首页
      const redirect = route.query.redirect as string
      if (redirect) {
        router.push(redirect)
      } else {
        router.push('/')
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
    showToast('登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>