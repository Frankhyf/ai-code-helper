<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col pt-4 pb-4 overflow-hidden">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 w-full flex flex-col h-full">
      <!-- 页面标题 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 flex-shrink-0">
        <div>
          <h1 class="text-3xl font-bold gradient-text">精选案例</h1>
          <p class="text-gray-400 mt-1">探索由AI生成的精彩应用案例，获取创作灵感</p>
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
          <h3 class="text-xl font-semibold text-gray-300 mb-2">暂无精选案例</h3>
          <p class="text-gray-400 mb-6">快去创建您的第一个应用吧！</p>
          <router-link 
            to="/" 
            class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-3 px-6 rounded-lg transition-all"
          >
            开始创建
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
                <button
                  @click="previewApp(app)"
                  class="p-2 bg-green-500 hover:bg-green-600 rounded-lg transition-colors"
                  title="查看详情"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                  </svg>
                </button>
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

    <!-- 应用详情弹窗 -->
    <Teleport to="body">
      <div 
        v-if="showDetailDialog" 
        class="fixed inset-0 z-50 flex items-center justify-center"
        @click.self="closeDetailDialog"
      >
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm"></div>
        
        <!-- 弹窗内容 -->
        <div class="relative glass-dark rounded-2xl w-full max-w-2xl mx-4 overflow-hidden animate-fade-in">
          <!-- 关闭按钮 -->
          <button
            @click="closeDetailDialog"
            class="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition-colors z-10"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
          
          <!-- 应用预览图 -->
          <div class="aspect-video bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center relative">
            <img 
              v-if="selectedApp?.cover" 
              :src="selectedApp.cover" 
              :alt="selectedApp.name"
              class="w-full h-full object-cover"
            />
            <div v-else class="text-center">
              <div class="text-6xl mb-2">🎨</div>
              <div class="text-gray-400">精选应用</div>
            </div>
            <!-- 精选标识 -->
            <div class="absolute top-4 right-4">
              <span class="px-2 py-1 bg-yellow-500/20 text-yellow-400 text-sm rounded-full">
                ⭐ 精选
              </span>
            </div>
          </div>
          
          <!-- 应用详情 -->
          <div class="p-6">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center space-x-3">
                <h2 class="text-2xl font-bold text-white">
                  {{ selectedApp?.name }}
                </h2>
                <!-- 代码生成类型标签 -->
                <span 
                  v-if="selectedApp?.codeGenType"
                  class="px-3 py-1 text-sm rounded-full bg-cyan-500/20 text-cyan-400"
                >
                  {{ getCodeGenTypeLabel(selectedApp.codeGenType) }}
                </span>
              </div>
            </div>
            
            <p class="text-gray-400 mb-6">
              {{ selectedApp?.description || '这是一个由AI生成的精彩应用' }}
            </p>
            
            <!-- 详细信息 -->
            <div class="grid grid-cols-2 gap-4 mb-6">
              <div class="glass rounded-lg p-4">
                <div class="text-sm text-gray-400 mb-1">创建者</div>
                <div class="text-white font-medium">
                  {{ selectedApp?.userVO?.nickname || '匿名用户' }}
                </div>
              </div>
              <div class="glass rounded-lg p-4">
                <div class="text-sm text-gray-400 mb-1">创建时间</div>
                <div class="text-white font-medium">
                  {{ selectedApp?.createTime ? new Date(selectedApp.createTime).toLocaleString() : '-' }}
                </div>
              </div>
              <div class="glass rounded-lg p-4">
                <div class="text-sm text-gray-400 mb-1">代码类型</div>
                <div class="text-white font-medium">
                  {{ selectedApp?.codeGenType ? getCodeGenTypeLabel(selectedApp.codeGenType) : '未知' }}
                </div>
              </div>
              <div class="glass rounded-lg p-4">
                <div class="text-sm text-gray-400 mb-1">部署状态</div>
                <div class="text-white font-medium">
                  {{ selectedApp?.deployedTime ? '已部署' : '未部署' }}
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex space-x-3">
              <button
                @click="previewDeployedApp(selectedApp!)"
                v-if="selectedApp?.deployedTime"
                class="flex-1 bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 text-white font-semibold py-3 px-4 rounded-lg transition-all text-center"
              >
                预览应用
              </button>
              <button
                v-else
                @click="closeDetailDialog"
                class="flex-1 bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-4 rounded-lg transition-all text-center"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import type { App } from '@/types'
import { showToast, Pagination as VanPagination } from 'vant'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

const appStore = useAppStore()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(6)
const totalApps = ref(0)

// 详情弹窗状态
const showDetailDialog = ref(false)
const selectedApp = ref<App | null>(null)

// 计算属性 - 直接使用 store 中的数据（搜索由后端处理）
const filteredApps = computed(() => appStore.featuredApps)

// 加载精选应用
const loadFeaturedApps = async () => {
  loading.value = true

  try {
    const response = await appStore.fetchFeaturedApps({
      current: currentPage.value,
      size: pageSize.value,
      name: searchKeyword.value.trim() || undefined
    })

    if (response) {
      totalApps.value = response.total
    }
  } catch (error) {
    console.error('加载精选应用失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadFeaturedApps()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadFeaturedApps()
}

// 页面切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadFeaturedApps()
}

// 预览应用 / 查看详情
const previewApp = (app: App) => {
  selectedApp.value = app
  showDetailDialog.value = true
}

// 关闭详情弹窗
const closeDetailDialog = () => {
  showDetailDialog.value = false
  selectedApp.value = null
}

// 预览已部署的应用
const previewDeployedApp = (app: App) => {
  if (app.deployUrl) {
    // 直接使用后端返回的完整部署 URL
    window.open(app.deployUrl, '_blank')
  } else {
    showToast('该应用尚未部署')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadFeaturedApps()
})
</script>

<style scoped>
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
