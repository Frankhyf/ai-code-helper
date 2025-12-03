<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12">
    <div class="max-w-lg w-full">
      <!-- 页面标题 -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold gradient-text mb-2">个人信息</h1>
        <p class="text-gray-400">{{ isEditing ? '编辑您的个人资料' : '查看您的个人资料' }}</p>
      </div>

      <!-- 个人信息卡片 -->
      <div class="glass-dark rounded-2xl shadow-2xl p-8">
        <!-- 浏览模式 -->
        <div v-if="!isEditing" class="space-y-6">
          <!-- 头像展示 -->
          <div class="flex flex-col items-center mb-6">
            <div 
              class="w-24 h-24 rounded-full overflow-hidden bg-gradient-to-br from-blue-500/30 to-purple-500/30 flex items-center justify-center border-2 border-white/20"
            >
              <img 
                v-if="userStore.user?.avatar" 
                :src="userStore.user.avatar" 
                alt="头像"
                class="w-full h-full object-cover"
              />
              <svg v-else class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
            <h2 class="text-xl font-semibold text-white mt-4">{{ userStore.user?.nickname || '未设置昵称' }}</h2>
            <span 
              class="mt-2 px-3 py-1 text-xs rounded-full"
              :class="userStore.isAdmin ? 'bg-purple-500/20 text-purple-400' : 'bg-blue-500/20 text-blue-400'"
            >
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </span>
          </div>

          <!-- 个人简介 -->
          <div class="glass rounded-lg p-4">
            <h3 class="text-sm font-medium text-gray-400 mb-2">个人简介</h3>
            <p class="text-white">{{ userStore.user?.profile || '这个人很懒，什么都没写~' }}</p>
          </div>

          <!-- 账号信息 -->
          <div class="space-y-3">
            <div class="flex justify-between items-center py-3 border-b border-white/10">
              <span class="text-gray-400">账号</span>
              <span class="text-white font-medium">{{ userStore.user?.account || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-3 border-b border-white/10">
              <span class="text-gray-400">注册时间</span>
              <span class="text-white">{{ formatDate(userStore.user?.createTime) }}</span>
            </div>
            <div class="flex justify-between items-center py-3">
              <span class="text-gray-400">更新时间</span>
              <span class="text-white">{{ formatDate(userStore.user?.updateTime) }}</span>
            </div>
          </div>

          <!-- 编辑按钮 -->
          <button
            @click="startEditing"
            class="w-full bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-3 px-4 rounded-lg transition-all glow-button flex items-center justify-center"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
            </svg>
            编辑资料
          </button>
        </div>

        <!-- 编辑模式 -->
        <form v-else @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 头像编辑 -->
          <div class="flex flex-col items-center mb-6">
            <div class="relative group">
              <div 
                class="w-24 h-24 rounded-full overflow-hidden bg-gradient-to-br from-blue-500/30 to-purple-500/30 flex items-center justify-center border-2 border-white/20"
                :class="{ 'animate-pulse': uploadingAvatar }"
              >
                <img 
                  v-if="form.userAvatar" 
                  :src="form.userAvatar" 
                  alt="头像"
                  class="w-full h-full object-cover"
                />
                <svg v-else class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
              <!-- 编辑遮罩 -->
              <div 
                class="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center cursor-pointer"
                @click="triggerAvatarUpload"
              >
                <svg v-if="!uploadingAvatar" class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                <div v-else class="loading-spinner !w-6 !h-6"></div>
              </div>
              <!-- 隐藏的文件输入 -->
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                class="hidden"
                @change="handleAvatarUpload"
              />
            </div>
            <p class="text-sm text-gray-400 mt-2">{{ uploadingAvatar ? '上传中...' : '点击头像上传图片' }}</p>
            <p class="text-xs text-gray-500 mt-1">支持 jpg、png、gif、webp 格式，最大 5MB</p>
          </div>

          <!-- 用户名 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              用户名
            </label>
            <div class="relative">
              <input
                v-model="form.userName"
                type="text"
                placeholder="请输入用户名"
                maxlength="20"
                class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-500': errors.userName }"
              />
              <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
              </div>
            </div>
            <div class="flex justify-between mt-1">
              <p v-if="errors.userName" class="text-sm text-red-400">{{ errors.userName }}</p>
              <p class="text-xs text-gray-500 ml-auto">{{ form.userName?.length || 0 }}/20</p>
            </div>
          </div>

          <!-- 个人简介 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              个人简介
            </label>
            <textarea
              v-model="form.userProfile"
              placeholder="介绍一下自己吧..."
              maxlength="200"
              rows="4"
              class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            ></textarea>
            <div class="flex justify-end mt-1">
              <p class="text-xs text-gray-500">{{ form.userProfile?.length || 0 }}/200</p>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex space-x-4 pt-4">
            <button
              type="button"
              @click="cancelEditing"
              class="flex-1 px-4 py-3 bg-white/10 hover:bg-white/20 text-gray-300 rounded-lg transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="loading || !hasChanges"
              class="flex-1 bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-4 rounded-lg transition-all glow-button"
            >
              <span v-if="!loading">保存修改</span>
              <span v-else class="flex items-center justify-center">
                <div class="loading-spinner mr-2 !w-5 !h-5"></div>
                保存中...
              </span>
            </button>
          </div>
        </form>
      </div>

      <!-- 返回链接 -->
      <div class="mt-6 text-center">
        <router-link 
          to="/" 
          class="text-sm text-gray-400 hover:text-gray-300"
        >
          ← 返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { UserUpdateMyForm } from '@/types'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 编辑模式状态
const isEditing = ref(false)

// 表单数据
const form = reactive<UserUpdateMyForm>({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 原始数据（用于对比是否有修改）
const originalForm = reactive<UserUpdateMyForm>({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 表单验证错误
const errors = reactive({
  userName: ''
})

// 状态
const loading = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref<HTMLInputElement | null>(null)

// 是否有修改
const hasChanges = computed(() => {
  return form.userName !== originalForm.userName ||
         form.userAvatar !== originalForm.userAvatar ||
         form.userProfile !== originalForm.userProfile
})

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 开始编辑
const startEditing = () => {
  const user = userStore.user
  if (user) {
    form.userName = user.nickname || ''
    form.userAvatar = user.avatar || ''
    form.userProfile = user.profile || ''
    
    // 保存原始数据
    originalForm.userName = form.userName
    originalForm.userAvatar = form.userAvatar
    originalForm.userProfile = form.userProfile
  }
  isEditing.value = true
}

// 触发头像上传
const triggerAvatarUpload = () => {
  if (uploadingAvatar.value) return
  avatarInputRef.value?.click()
}

// 处理头像上传
const handleAvatarUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  
  if (!file) return
  
  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    showToast('仅支持 jpg、png、gif、webp 格式的图片')
    input.value = '' // 清空选择
    return
  }
  
  // 校验文件大小（最大 5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    showToast('图片大小不能超过 5MB')
    input.value = '' // 清空选择
    return
  }
  
  uploadingAvatar.value = true
  
  try {
    const avatarUrl = await userStore.uploadAvatar(file)
    
    if (avatarUrl) {
      // 更新表单中的头像 URL
      form.userAvatar = avatarUrl
      originalForm.userAvatar = avatarUrl // 头像已上传保存，更新原始值
    }
  } finally {
    uploadingAvatar.value = false
    input.value = '' // 清空选择，允许重复选择同一文件
  }
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
  errors.userName = ''
}

// 表单验证
const validateForm = () => {
  let isValid = true
  
  if (form.userName && form.userName.length > 20) {
    errors.userName = '用户名不能超过20个字符'
    isValid = false
  } else {
    errors.userName = ''
  }
  
  return isValid
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  if (!hasChanges.value) {
    showToast('没有需要保存的修改')
    return
  }

  loading.value = true

  try {
    const success = await userStore.updateMyInfo(form)
    
    if (success) {
      isEditing.value = false
    }
  } catch (error) {
    console.error('保存失败:', error)
    showToast('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  if (!userStore.isLogin) {
    router.push('/login')
    return
  }
})
</script>
