<template>
  <div class="min-h-screen">
    <!-- Hero区域 -->
    <section class="relative min-h-screen flex items-center justify-center overflow-hidden">
      <!-- 动态背景 -->
      <div id="hero-background" class="absolute inset-0 z-0"></div>
      
      <!-- 主要内容 -->
      <div class="relative z-10 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <h1 class="text-4xl md:text-6xl font-bold mb-6 animate-fade-in">
            <span class="gradient-text">AI应用生成平台</span>
          </h1>
          <p class="text-xl md:text-2xl text-gray-300 mb-8 max-w-3xl mx-auto animate-slide-up">
            用自然语言描述你的需求，AI自动生成完整的网站应用
            <br class="hidden md:block" />
            <span class="text-cyan-400">零代码、智能化、一键部署</span>
          </p>
          
          <!-- 创建应用输入框 -->
          <div class="max-w-2xl mx-auto mb-8 animate-slide-up" style="animation-delay: 0.2s">
            <div class="glass rounded-2xl p-6 shadow-2xl">
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  描述你想要创建的应用
                </label>
                <textarea
                  v-model="appDescription"
                  placeholder="例如：创建一个现代化的个人博客网站，要有文章列表、分类标签、搜索功能..."
                  class="w-full h-24 px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  @keydown.enter.ctrl="createApp"
                ></textarea>
              </div>
              
              <!-- 快捷模板按钮 -->
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
                <button
                  v-for="template in quickTemplates"
                  :key="template.key"
                  @click="selectTemplate(template)"
                  class="glass-dark hover:bg-white/20 rounded-lg p-3 text-sm font-medium transition-all hover:scale-105"
                >
                  <div class="text-2xl mb-1">{{ template.icon }}</div>
                  <div>{{ template.name }}</div>
                </button>
              </div>
              
              <!-- 创建按钮 -->
              <button
                v-if="userStore.isLogin"
                @click="createApp"
                :disabled="!appDescription.trim() || isCreating"
                class="w-full glow-button bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all"
              >
                <span v-if="!isCreating">🚀 立即生成应用</span>
                <span v-else class="flex items-center justify-center">
                  <div class="loading-spinner mr-2 !w-5 !h-5"></div>
                  生成中...
                </span>
              </button>
              <!-- 未登录提示 -->
              <div v-else class="space-y-3">
                <router-link
                  to="/login"
                  class="w-full block text-center glow-button bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-3 px-6 rounded-lg transition-all"
                >
                  🔐 登录后开始创建
                </router-link>
                <p class="text-center text-sm text-gray-400">
                  还没有账号？
                  <router-link to="/register" class="text-blue-400 hover:text-blue-300">立即注册</router-link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 滚动指示器 -->
      <div class="absolute bottom-8 left-1/2 transform -translate-x-1/2 animate-bounce">
        <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"></path>
        </svg>
      </div>
    </section>

    <!-- 功能特性区域 -->
    <section class="py-20 px-4 sm:px-6 lg:px-8">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold mb-4 gradient-text">核心功能特性</h2>
          <p class="text-xl text-gray-300">让每个人都能轻松创建专业的网站应用</p>
        </div>
        
        <div class="grid md:grid-cols-3 gap-8">
          <div class="card-hover glass-dark rounded-2xl p-8 text-center">
            <div class="w-16 h-16 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-4">智能对话</h3>
            <p class="text-gray-300">通过自然语言与AI对话，描述你的需求，AI理解并生成相应的应用代码</p>
          </div>
          
          <div class="card-hover glass-dark rounded-2xl p-8 text-center">
            <div class="w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-4">实时预览</h3>
            <p class="text-gray-300">AI生成过程中实时预览效果，所见即所得，支持多轮对话优化</p>
          </div>
          
          <div class="card-hover glass-dark rounded-2xl p-8 text-center">
            <div class="w-16 h-16 bg-gradient-to-r from-green-500 to-teal-500 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-4">一键部署</h3>
            <p class="text-gray-300">生成完成后一键部署到云端，获得永久访问链接，支持自定义域名</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 精选案例预览 -->
    <section class="py-20 px-4 sm:px-6 lg:px-8 bg-gradient-to-r from-slate-800/50 to-blue-800/50">
      <div class="max-w-6xl mx-auto">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
          <div>
            <h2 class="text-3xl font-bold gradient-text">精选案例</h2>
            <p class="text-gray-400 mt-1">看看其他用户创建的精彩应用</p>
          </div>
          <router-link 
            to="/examples" 
            class="mt-4 sm:mt-0 inline-flex items-center px-4 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-cyan-600 transition-all text-sm"
          >
            查看更多
            <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"></path>
            </svg>
          </router-link>
        </div>
        
        <div v-if="featuredApps.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="app in featuredApps.slice(0, 6)" 
            :key="app.id"
            class="card-hover glass-dark rounded-xl overflow-hidden group"
          >
            <!-- 应用预览图 -->
            <div class="aspect-[16/10] bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center relative overflow-hidden">
              <img 
                v-if="app.cover" 
                :src="app.cover" 
                :alt="app.name"
                class="w-full h-full object-cover"
              />
              <div v-else class="text-center">
                <div class="text-3xl mb-1">🎨</div>
                <div class="text-xs text-gray-400">精选应用</div>
              </div>
              
              <!-- 精选标识 -->
              <div class="absolute top-2 right-2">
                <span class="px-1.5 py-0.5 bg-yellow-500/20 text-yellow-400 text-xs rounded">
                  ⭐ 精选
                </span>
              </div>
              
              <!-- 悬停操作按钮 -->
              <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <router-link
                  to="/examples"
                  class="p-1.5 bg-green-500 hover:bg-green-600 rounded-lg transition-colors"
                  title="查看详情"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                  </svg>
                </router-link>
              </div>
            </div>

            <!-- 应用信息 -->
            <div class="p-3">
              <div class="flex items-center justify-between mb-1">
                <h3 class="text-sm font-semibold text-white group-hover:text-blue-400 transition-colors truncate flex-1 mr-2">
                  {{ app.name }}
                </h3>
                <div class="flex items-center space-x-1 flex-shrink-0">
                  <span 
                    v-if="app.codeGenType"
                    class="px-1.5 py-0.5 text-xs rounded bg-cyan-500/20 text-cyan-400"
                  >
                    {{ getCodeGenTypeLabel(app.codeGenType) }}
                  </span>
                </div>
              </div>
              
              <div class="flex items-center justify-between text-xs text-gray-500">
                <span class="flex items-center truncate mr-2">
                  <svg class="w-3 h-3 mr-1 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                  </svg>
                  <span class="truncate">{{ app.userVO?.nickname || '匿名用户' }}</span>
                </span>
                <span class="flex-shrink-0">{{ new Date(app.createTime).toLocaleDateString() }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="text-center py-12">
          <div class="text-6xl mb-4">🎨</div>
          <p class="text-gray-400">暂无精选案例，快去创建第一个应用吧！</p>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="py-20 px-4 sm:px-6 lg:px-8">
      <div class="max-w-6xl mx-auto">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div class="animate-fade-in">
            <div class="text-3xl md:text-4xl font-bold gradient-text mb-2">10K+</div>
            <div class="text-gray-400">已创建应用</div>
          </div>
          <div class="animate-fade-in" style="animation-delay: 0.1s">
            <div class="text-3xl md:text-4xl font-bold gradient-text mb-2">5K+</div>
            <div class="text-gray-400">活跃用户</div>
          </div>
          <div class="animate-fade-in" style="animation-delay: 0.2s">
            <div class="text-3xl md:text-4xl font-bold gradient-text mb-2">99%</div>
            <div class="text-gray-400">生成成功率</div>
          </div>
          <div class="animate-fade-in" style="animation-delay: 0.3s">
            <div class="text-3xl md:text-4xl font-bold gradient-text mb-2">24/7</div>
            <div class="text-gray-400">在线服务</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import type { AppCreateForm } from '@/types'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

// 初始化p5.js背景
import { initHeroBackground } from '@/utils/heroBackground'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 表单数据
const appDescription = ref('')
const isCreating = ref(false)

// p5 实例引用
let p5Instance: any = null

// 快捷模板
const quickTemplates = [
  {
    key: 'blog',
    name: '个人博客',
    icon: '📝',
    description: '创建一个现代化的个人博客网站，包含文章列表、分类、标签等功能'
  },
  {
    key: 'company',
    name: '企业官网',
    icon: '🏢',
    description: '创建一个专业的企业官网，展示公司信息、产品服务、联系方式等'
  },
  {
    key: 'shop',
    name: '在线商城',
    icon: '🛒',
    description: '创建一个功能完整的在线商城，包含商品展示、购物车、订单管理等'
  },
  {
    key: 'portfolio',
    name: '作品展示',
    icon: '🎨',
    description: '创建一个精美的作品展示网站，适合设计师、摄影师等展示作品集'
  }
]

// 选择模板
const selectTemplate = (template: any) => {
  appDescription.value = template.description
}

// 创建应用
const createApp = async () => {
  if (!appDescription.value.trim()) {
    showToast('请先描述你想要创建的应用')
    return
  }

  if (!userStore.isLogin) {
    router.push('/login')
    return
  }

  isCreating.value = true

  try {
    const form: AppCreateForm = {
      name: `AI生成应用 - ${new Date().toLocaleString()}`,
      description: appDescription.value.slice(0, 100) + '...',
      prompt: appDescription.value
    }

    const appId = await appStore.createApp(form)
    if (typeof appId === 'string') {
      console.log('=== 路由跳转ID追踪 ===')
      console.log('跳转前appId:', appId, '类型:', typeof appId)
      console.log('跳转URL:', `/app/chat/${appId}`)
      console.log('==================')
      router.push(`/app/chat/${appId}`)
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    showToast('创建应用失败，请重试')
  } finally {
    isCreating.value = false
  }
}

// 精选应用
const featuredApps = ref<any[]>([])

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const response = await appStore.fetchFeaturedApps({
      current: 1,
      size: 6
    })
    if (response) {
      featuredApps.value = response.records
    }
  } catch (error) {
    // 后端服务不可用时，使用空数组，不显示错误提示
    console.warn('后端服务未启动，使用默认数据')
    featuredApps.value = []
  }
}

onMounted(() => {
  // 初始化背景动画
  p5Instance = initHeroBackground('hero-background')
  
  // 加载精选应用
  loadFeaturedApps()
})

// 组件卸载时清理 p5 实例
onUnmounted(() => {
  if (p5Instance) {
    p5Instance.remove()
    p5Instance = null
  }
})
</script>