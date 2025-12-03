<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col pt-4 pb-4 overflow-hidden">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 w-full flex flex-col h-full">
      <!-- 页面标题 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 flex-shrink-0">
        <div>
          <h1 class="text-3xl font-bold gradient-text">我的应用</h1>
          <p class="text-gray-400 mt-1">管理您创建的所有应用</p>
        </div>
        
        <div class="mt-4 sm:mt-0 flex space-x-3">
          <router-link 
            to="/app/create" 
            class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-2 px-4 rounded-lg transition-all glow-button flex items-center"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            创建应用
          </router-link>
        </div>
      </div>

      <!-- 搜索 -->
      <div class="mb-4 flex-shrink-0">
        <div class="relative">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索应用名称..."
            class="w-full pl-10 pr-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            @keydown.enter="handleSearch"
          />
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </div>
          <button
            v-if="searchKeyword"
            @click="clearSearch"
            class="absolute inset-y-0 right-0 pr-3 flex items-center"
          >
            <svg class="h-4 w-4 text-gray-400 hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- 应用列表容器 -->
      <div class="flex-1 min-h-0 relative">
        <!-- 加载状态 -->
        <div v-if="loading" class="absolute inset-0 flex flex-col items-center justify-center">
          <div class="loading-spinner mx-auto mb-4"></div>
          <p class="text-gray-400">加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredApps.length === 0" class="absolute inset-0 flex flex-col items-center justify-center">
          <div class="text-6xl mb-4">🎨</div>
          <h3 class="text-xl font-semibold text-gray-300 mb-2">还没有应用</h3>
          <p class="text-gray-400 mb-6">开始创建您的第一个AI应用吧！</p>
          <router-link 
            to="/app/create" 
            class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-3 px-6 rounded-lg transition-all"
          >
            创建第一个应用
          </router-link>
        </div>

        <!-- 应用网格 -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 h-full content-start">
          <div
            v-for="app in filteredApps"
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
                <div class="text-3xl mb-1">🚀</div>
                <div class="text-xs text-gray-400">应用预览</div>
              </div>
              
              <!-- 悬停操作按钮 -->
              <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center space-x-2">
                <router-link
                  :to="`/app/chat/${app.id}`"
                  class="p-1.5 bg-blue-500 hover:bg-blue-600 rounded-lg transition-colors"
                  title="编辑对话"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                  </svg>
                </router-link>
                
                <button
                  @click="deployApp(app)"
                  v-if="app.status !== 'deployed'"
                  class="p-1.5 bg-purple-500 hover:bg-purple-600 rounded-lg transition-colors"
                  title="部署"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                  </svg>
                </button>
                
                <button
                  @click="deleteApp(app)"
                  class="p-1.5 bg-red-500 hover:bg-red-600 rounded-lg transition-colors"
                  title="删除"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 应用信息 -->
            <div class="p-3">
              <div class="flex items-center justify-between mb-1">
                <!-- 应用名称（可编辑） -->
                <div class="flex-1 mr-2 min-w-0">
                  <input
                    v-if="editingAppId === app.id"
                    v-model="editingName"
                    :data-app-id="app.id"
                    type="text"
                    maxlength="50"
                    class="w-full text-sm font-semibold text-white bg-white/10 border border-blue-500 rounded px-1.5 py-0.5 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    @blur="saveAppName(app)"
                    @keydown.enter="saveAppName(app)"
                    @keydown.escape="cancelEditName"
                    @click.stop
                  />
                  <h3 
                    v-else
                    class="text-sm font-semibold text-white hover:text-blue-400 transition-colors truncate cursor-pointer flex items-center group/name"
                    @click="startEditName(app, $event)"
                    title="点击编辑应用名称"
                  >
                    {{ app.name }}
                    <svg class="w-3 h-3 ml-1 opacity-0 group-hover/name:opacity-100 transition-opacity text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                    </svg>
                  </h3>
                </div>
                <div class="flex items-center space-x-1 flex-shrink-0">
                  <span 
                    v-if="app.codeGenType"
                    class="px-1.5 py-0.5 text-xs rounded bg-cyan-500/20 text-cyan-400"
                  >
                    {{ getCodeGenTypeLabel(app.codeGenType) }}
                  </span>
                  <span 
                    class="px-1.5 py-0.5 text-xs rounded"
                    :class="getStatusClass(app.status)"
                  >
                    {{ getStatusText(app.status) }}
                  </span>
                </div>
              </div>
              
              <div class="text-xs text-gray-500">
                {{ new Date(app.createTime).toLocaleDateString() }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div v-if="!loading && totalApps > 0" class="flex justify-center mt-3 flex-shrink-0">
        <div class="pagination-wrapper glass-dark rounded-xl p-1">
          <van-pagination 
            v-model="currentPage" 
            :total-items="totalApps" 
            :items-per-page="pageSize"
            force-ellipses
            @change="handlePageChange"
            class="glass-pagination"
          >
            <template #prev-text>
              <div class="flex items-center space-x-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
                <span class="hidden sm:inline">上一页</span>
              </div>
            </template>
            <template #next-text>
              <div class="flex items-center space-x-1">
                <span class="hidden sm:inline">下一页</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
              </div>
            </template>
          </van-pagination>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { App } from '@/types'
import { showToast, showDialog, Pagination as VanPagination } from 'vant'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

const appStore = useAppStore()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(6)
const totalApps = ref(0)

// 应用名称编辑状态
const editingAppId = ref<number | string | null>(null)
const editingName = ref('')

// 计算属性 - 直接使用 store 中的数据（搜索由后端处理）
const filteredApps = computed(() => appStore.myApps)

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
    const response = await appStore.fetchMyApps({
      current: currentPage.value,
      size: pageSize.value,
      userId: userStore.user?.id,
      name: searchKeyword.value.trim() || undefined
    })

    if (response) {
      totalApps.value = response.total
    }
  } catch (error) {
    console.error('加载应用列表失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadApps()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadApps()
}

// 页面切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadApps()
}

// 部署应用
const deployApp = async (app: App) => {
  try {
    showToast('正在部署...')
    const deployUrl = await appStore.deployApp(app.id)
    if (deployUrl) {
      showToast('部署成功')
    }
  } catch (error) {
    console.error('部署失败:', error)
    showToast('部署失败，请重试')
  }
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
      }
    } catch (error) {
      console.error('删除失败:', error)
      showToast('删除失败，请重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 开始编辑应用名称
const startEditName = (app: App, event: Event) => {
  event.stopPropagation()
  editingAppId.value = app.id
  editingName.value = app.name || ''
  nextTick(() => {
    const input = document.querySelector(`input[data-app-id="${app.id}"]`) as HTMLInputElement
    if (input) {
      input.focus()
      input.select()
    }
  })
}

// 保存应用名称
const saveAppName = async (app: App) => {
  if (editingAppId.value !== app.id) return
  
  const newName = editingName.value.trim()
  
  // 如果名称没有变化或为空，取消编辑
  if (!newName || newName === app.name) {
    cancelEditName()
    return
  }
  
  try {
    const success = await appStore.updateApp({
      id: app.id,
      name: newName
    })
    
    if (success) {
      // 更新本地列表中的应用名称
      const appIndex = appStore.myApps.findIndex(a => a.id === app.id)
      if (appIndex !== -1) {
        appStore.myApps[appIndex].name = newName
        appStore.myApps[appIndex].appName = newName
      }
      showToast('应用名称已更新')
    }
  } catch (error) {
    console.error('更新应用名称失败:', error)
    showToast('更新失败，请重试')
  } finally {
    editingAppId.value = null
    editingName.value = ''
  }
}

// 取消编辑应用名称
const cancelEditName = () => {
  editingAppId.value = null
  editingName.value = ''
}

// 页面加载时获取数据
onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 隐藏全局滚动条（针对此页面布局） */
:global(html), :global(body) {
  overflow: hidden;
}

/* 自定义 Vant 分页样式 */
:deep(.glass-pagination) {
  --van-pagination-item-default-color: #94a3b8;
  --van-pagination-background-color: transparent;
  --van-pagination-item-active-background-color: #3b82f6;
  --van-pagination-item-active-color: #ffffff;
  --van-pagination-desc-color: #64748b;
  --van-pagination-disabled-opacity: 0.3;
  --van-pagination-height: 40px;
  --van-pagination-font-size: 14px;
  width: auto;
  min-width: 300px;
}

:deep(.glass-pagination ul) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.van-pagination__item) {
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  margin: 0;
  flex: none;
  min-width: 36px;
  padding: 0 8px;
  transition: all 0.3s ease;
}

:deep(.van-pagination__item:hover:not(.van-pagination__item--disabled):not(.van-pagination__item--active)) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

:deep(.van-pagination__item--active) {
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  border: none;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4);
  font-weight: 600;
}

:deep(.van-pagination__item--disabled) {
  opacity: 0.3;
  cursor: not-allowed;
}

:deep(.van-pagination__prev),
:deep(.van-pagination__next) {
  padding: 0 16px;
  font-weight: 500;
}
</style>