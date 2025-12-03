<template>
  <div class="min-h-screen pt-6 pb-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 管理后台子导航 -->
      <AdminNav />
      
      <!-- 页面标题 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold gradient-text">应用管理</h1>
          <p class="text-gray-400 mt-2">管理平台所有用户的应用</p>
        </div>
        
        <div class="mt-4 sm:mt-0">
          <div class="text-sm text-gray-400">
            总计: <span class="text-blue-400 font-semibold">{{ totalApps }}</span> 个应用
          </div>
        </div>
      </div>

      <!-- 搜索和筛选工具栏 -->
      <div class="glass-dark rounded-xl p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- 应用名称搜索 -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-300 mb-2">
              应用名称
            </label>
            <input
              v-model="searchParams.name"
              type="text"
              placeholder="搜索应用名称..."
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <!-- 创建者搜索 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              创建者
            </label>
            <input
              v-model="searchParams.creator"
              type="text"
              placeholder="搜索创建者..."
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <!-- 应用类型筛选 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              应用类型
            </label>
            <select
              v-model="searchParams.type"
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">所有类型</option>
              <option value="single">单文件</option>
              <option value="multiple">多文件</option>
            </select>
          </div>
          
          <!-- 状态筛选 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              状态
            </label>
            <select
              v-model="searchParams.status"
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">所有状态</option>
              <option value="draft">草稿</option>
              <option value="deployed">已部署</option>
              <option value="featured">精选</option>
            </select>
          </div>
          
          <!-- 操作按钮 -->
          <div class="md:col-span-4 flex justify-end space-x-3">
            <button
              @click="resetSearch"
              class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors"
            >
              重置
            </button>
            <button
              @click="searchApps"
              class="px-4 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white rounded-lg transition-all"
            >
              搜索
            </button>
          </div>
        </div>
      </div>

      <!-- 应用列表表格 -->
      <div class="glass-dark rounded-xl overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-white/5">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  应用信息
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  创建者
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  类型
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  状态
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  创建时间
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/10">
              <tr 
                v-for="app in apps" 
                :key="app.id"
                class="hover:bg-white/5 transition-colors"
              >
                <!-- 应用信息 -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-500 rounded-lg flex items-center justify-center mr-3">
                      <span class="text-white font-bold text-sm">{{ app.name.charAt(0) }}</span>
                    </div>
                    <div>
                      <div class="text-sm font-medium text-white">{{ app.name }}</div>
                      <div class="text-sm text-gray-400 line-clamp-1">
                        {{ app.description || '暂无描述' }}
                      </div>
                    </div>
                  </div>
                </td>
                
                <!-- 创建者 -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-300">
                    {{ app.userVO?.nickname || app.userVO?.account || '未知用户' }}
                  </div>
                  <div class="text-sm text-gray-500">
                    {{ app.userVO?.account }}
                  </div>
                </td>
                
                <!-- 类型 -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="px-2 py-1 text-xs rounded-full"
                    :class="getTypeClass(app.type)"
                  >
                    {{ app.type === 'single' ? '单文件' : '多文件' }}
                  </span>
                </td>
                
                <!-- 状态 -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="px-2 py-1 text-xs rounded-full"
                    :class="getStatusClass(app.status)"
                  >
                    {{ getStatusText(app.status) }}
                  </span>
                </td>
                
                <!-- 创建时间 -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                  {{ new Date(app.createTime).toLocaleDateString() }}
                  <div class="text-xs text-gray-500">
                    {{ new Date(app.createTime).toLocaleTimeString() }}
                  </div>
                </td>
                
                <!-- 操作 -->
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex space-x-2">
                    <router-link
                      :to="`/app/chat/${app.id}`"
                      class="text-blue-400 hover:text-blue-300 transition-colors"
                      title="查看对话"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                      </svg>
                    </router-link>
                    
                    <button
                      @click="toggleFeatured(app)"
                      class="text-yellow-400 hover:text-yellow-300 transition-colors"
                      :title="app.status === 'featured' ? '取消精选' : '设为精选'"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"></path>
                      </svg>
                    </button>
                    
                    <button
                      @click="editApp(app)"
                      class="text-green-400 hover:text-green-300 transition-colors"
                      title="编辑"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                      </svg>
                    </button>
                    
                    <button
                      @click="deleteApp(app)"
                      class="text-red-400 hover:text-red-300 transition-colors"
                      title="删除"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && apps.length === 0" class="text-center py-16">
          <div class="text-6xl mb-4">📱</div>
          <h3 class="text-xl font-semibold text-gray-300 mb-2">暂无应用</h3>
          <p class="text-gray-400">没有找到符合条件的应用</p>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-16">
          <div class="loading-spinner mx-auto mb-4"></div>
          <p class="text-gray-400">加载中...</p>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && totalPages > 1" class="mt-8 flex justify-center">
        <div class="flex space-x-2">
          <button
            @click="changePage(currentPage - 1)"
            :disabled="currentPage <= 1"
            class="px-3 py-2 bg-white/10 hover:bg-white/20 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg transition-colors"
          >
            上一页
          </button>
          
          <span 
            v-for="page in displayPages" 
            :key="page"
            class="px-3 py-2 text-white"
            :class="{ 
              'bg-blue-500': page === currentPage,
              'bg-white/10 hover:bg-white/20 cursor-pointer': page !== currentPage && page !== '...'
            }"
            @click="typeof page === 'number' ? changePage(page) : null"
          >
            {{ page }}
          </span>
          
          <button
            @click="changePage(currentPage + 1)"
            :disabled="currentPage >= totalPages"
            class="px-3 py-2 bg-white/10 hover:bg-white/20 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg transition-colors"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import type { App } from '@/types'
import { showToast, showDialog } from 'vant'
import AdminNav from '@/components/layout/AdminNav.vue'

const appStore = useAppStore()

// 搜索参数
const searchParams = ref({
  name: '',
  creator: '',
  type: '',
  status: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)

// 状态
const loading = ref(false)

// 计算属性
const apps = computed(() => appStore.apps)
const totalApps = computed(() => appStore.totalApps)
const totalPages = computed(() => Math.ceil(totalApps.value / pageSize.value))

// 分页显示
const displayPages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  if (start > 1) {
    pages.push(1)
    if (start > 2) pages.push('...')
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  if (end < totalPages.value) {
    if (end < totalPages.value - 1) pages.push('...')
    pages.push(totalPages.value)
  }
  
  return pages
})

// 获取类型样式类
const getTypeClass = (type: string) => {
  return type === 'single' 
    ? 'bg-blue-500/20 text-blue-400' 
    : 'bg-purple-500/20 text-purple-400'
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  switch (status) {
    case 'draft':
      return 'bg-gray-500/20 text-gray-400'
    case 'deployed':
      return 'bg-green-500/20 text-green-400'
    case 'featured':
      return 'bg-yellow-500/20 text-yellow-400'
    default:
      return 'bg-gray-500/20 text-gray-400'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'draft':
      return '草稿'
    case 'deployed':
      return '已部署'
    case 'featured':
      return '精选'
    default:
      return '未知'
  }
}

// 加载应用列表
const loadApps = async () => {
  loading.value = true
  
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...searchParams.value
    }
    
    await appStore.fetchApps(params)
  } catch (error) {
    console.error('加载应用列表失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索应用
const searchApps = () => {
  currentPage.value = 1
  loadApps()
}

// 重置搜索
const resetSearch = () => {
  searchParams.value = {
    name: '',
    creator: '',
    type: '',
    status: ''
  }
  currentPage.value = 1
  loadApps()
}

// 切换分页
const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadApps()
  }
}

// 切换精选状态
const toggleFeatured = async (app: App) => {
  try {
    const newStatus = app.status === 'featured' ? 'deployed' : 'featured'
    const newPriority = app.status === 'featured' ? 0 : 99
    
    const success = await appStore.updateApp({
      id: app.id,
      status: newStatus,
      priority: newPriority
    })
    
    if (success) {
      showToast(app.status === 'featured' ? '已取消精选' : '已设为精选')
      loadApps()
    }
  } catch (error) {
    console.error('切换精选状态失败:', error)
    showToast('操作失败，请重试')
  }
}

// 编辑应用
const editApp = (app: App) => {
  // 这里可以实现编辑功能
  showToast('编辑功能开发中...')
}

// 删除应用
const deleteApp = (app: App) => {
  showDialog({
    title: '确认删除',
    message: `确定要删除应用"${app.name}"吗？此操作不可撤销。`,
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonColor: '#ef4444'
  }).then(async () => {
    try {
      const success = await appStore.deleteApp(app.id)
      if (success) {
        showToast('删除成功')
        loadApps()
      }
    } catch (error) {
      console.error('删除失败:', error)
      showToast('删除失败，请重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>