<template>
  <div class="min-h-screen pt-6 pb-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 管理后台子导航 -->
      <AdminNav />
      
      <!-- 页面标题 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold gradient-text">对话管理</h1>
          <p class="text-gray-400 mt-2">管理平台所有用户的对话历史</p>
        </div>
        
        <div class="mt-4 sm:mt-0">
          <div class="text-sm text-gray-400">
            总计: <span class="text-blue-400 font-semibold">{{ totalChats }}</span> 条对话
          </div>
        </div>
      </div>

      <!-- 搜索和筛选工具栏 -->
      <div class="glass-dark rounded-xl p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <!-- 应用ID搜索 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              应用ID
            </label>
            <input
              v-model="searchParams.appId"
              type="text"
              placeholder="搜索应用ID..."
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <!-- 用户ID搜索 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              用户ID
            </label>
            <input
              v-model="searchParams.userId"
              type="text"
              placeholder="搜索用户ID..."
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <!-- 消息类型筛选 -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              消息类型
            </label>
            <select
              v-model="searchParams.messageType"
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">所有类型</option>
              <option value="user">用户消息</option>
              <option value="ai">AI消息</option>
            </select>
          </div>
          
          <!-- 消息内容搜索 -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-300 mb-2">
              消息内容
            </label>
            <input
              v-model="searchParams.message"
              type="text"
              placeholder="搜索消息内容..."
              class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-3 items-end">
            <button
              @click="resetSearch"
              class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors"
            >
              重置
            </button>
            <button
              @click="searchChats"
              class="px-4 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white rounded-lg transition-all"
            >
              搜索
            </button>
          </div>
        </div>
      </div>

      <!-- 对话列表表格 -->
      <div class="glass-dark rounded-xl overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-white/5">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  ID
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  应用ID
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  用户ID
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  消息类型
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  消息内容
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                  创建时间
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/10">
              <tr 
                v-for="chat in chats" 
                :key="chat.id"
                class="hover:bg-white/5 transition-colors"
              >
                <!-- ID -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-300">
                    {{ chat.id }}
                  </div>
                </td>
                
                <!-- 应用ID -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-300">
                    {{ chat.appId }}
                  </div>
                </td>
                
                <!-- 用户ID -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-300">
                    {{ chat.userId }}
                  </div>
                </td>
                
                <!-- 消息类型 -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="px-2 py-1 text-xs rounded-full"
                    :class="getMessageTypeClass(chat.messageType || '')"
                  >
                    {{ chat.messageType === 'user' ? '用户消息' : 'AI消息' }}
                  </span>
                </td>
                
                <!-- 消息内容 -->
                <td class="px-6 py-4">
                  <div class="text-sm text-gray-300 max-w-md">
                    <div class="line-clamp-2">
                      {{ chat.message }}
                    </div>
                  </div>
                </td>
                
                <!-- 创建时间 -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                  {{ chat.createTime ? new Date(chat.createTime).toLocaleDateString() : '-' }}
                  <div class="text-xs text-gray-500">
                    {{ chat.createTime ? new Date(chat.createTime).toLocaleTimeString() : '' }}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && chats.length === 0" class="text-center py-16">
          <div class="text-6xl mb-4">💬</div>
          <h3 class="text-xl font-semibold text-gray-300 mb-2">暂无对话记录</h3>
          <p class="text-gray-400">没有找到符合条件的对话记录</p>
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
            class="px-3 py-2 text-white rounded-lg"
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
import { showToast } from 'vant'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
// API 类型定义在 typings.d.ts 中作为全局命名空间声明
import AdminNav from '@/components/layout/AdminNav.vue'

// 搜索参数
const searchParams = ref({
  appId: '',
  userId: '',
  messageType: '',
  message: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)

// 状态
const loading = ref(false)
const chats = ref<API.ChatHistory[]>([])
const totalChats = ref(0)

// 计算属性
const totalPages = computed(() => Math.ceil(totalChats.value / pageSize.value))

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

// 获取消息类型样式类
const getMessageTypeClass = (messageType: string) => {
  return messageType === 'user' 
    ? 'bg-blue-500/20 text-blue-400' 
    : 'bg-purple-500/20 text-purple-400'
}

// 加载对话列表
const loadChats = async () => {
  loading.value = true
  
  try {
    const params: API.ChatHistoryQueryRequest = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    }
    
    // 添加搜索条件
    if (searchParams.value.appId) {
      params.appId = parseInt(searchParams.value.appId)
    }
    
    if (searchParams.value.userId) {
      params.userId = parseInt(searchParams.value.userId)
    }
    
    if (searchParams.value.messageType) {
      params.messageType = searchParams.value.messageType
    }
    
    if (searchParams.value.message) {
      params.message = searchParams.value.message
    }
    
    // request 拦截器已处理响应，直接返回 data 部分（PageChatHistory 类型）
    const data = await listAllChatHistoryByPageForAdmin(params) as unknown as API.PageChatHistory
    
    if (data) {
      chats.value = data.records || []
      totalChats.value = data.totalRow || 0
      console.log('对话列表加载成功，共', totalChats.value, '条记录')
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索对话
const searchChats = () => {
  currentPage.value = 1
  loadChats()
}

// 重置搜索
const resetSearch = () => {
  searchParams.value = {
    appId: '',
    userId: '',
    messageType: '',
    message: ''
  }
  currentPage.value = 1
  loadChats()
}

// 切换分页
const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadChats()
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadChats()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
