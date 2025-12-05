<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col overflow-hidden">
    <!-- 顶部工具栏 -->
    <div class="glass-dark border-b border-white/10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center space-x-4">
            <router-link 
              to="/app/list" 
              class="p-2 hover:bg-white/10 rounded-lg transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
              </svg>
            </router-link>
            
            <div>
              <!-- 应用名称（可编辑） -->
              <div class="flex items-center space-x-2">
                <input
                  v-if="isEditingName"
                  ref="nameInputRef"
                  v-model="editingName"
                  type="text"
                  maxlength="50"
                  class="text-lg font-semibold text-white bg-white/10 border border-blue-500 rounded px-2 py-0.5 focus:outline-none focus:ring-2 focus:ring-blue-500 w-48"
                  @blur="saveAppName"
                  @keydown.enter="saveAppName"
                  @keydown.escape="cancelEditName"
                />
                <h1 
                  v-else
                  class="text-lg font-semibold text-white cursor-pointer hover:text-blue-400 transition-colors group flex items-center"
                  @click="startEditName"
                  title="点击编辑应用名称"
                >
                  {{ currentApp?.name || '应用对话' }}
                  <svg class="w-4 h-4 ml-1 opacity-0 group-hover:opacity-100 transition-opacity text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                  </svg>
                </h1>
              </div>
              <p class="text-sm text-gray-400">
                与AI对话优化您的应用
              </p>
            </div>
          </div>
          
          <div class="flex items-center space-x-3">
            <!-- 下载代码按钮 -->
            <button
              @click="downloadCode"
              :disabled="isDownloading"
              class="bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-2 px-4 rounded-lg transition-all glow-button"
            >
              <svg v-if="!isDownloading" class="w-4 h-4 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
              </svg>
              <svg v-else class="w-4 h-4 mr-2 inline animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              {{ isDownloading ? '下载中...' : '下载代码' }}
            </button>
            
            <!-- 部署按钮 -->
            <button
              @click="deployCurrentApp"
              class="bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 text-white font-semibold py-2 px-4 rounded-lg transition-all glow-button"
            >
              <svg class="w-4 h-4 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
              </svg>
              {{ currentApp?.deployedTime ? '访问部署' : '部署应用' }}
            </button>
            
            <!-- 设置按钮 -->
            <router-link
              :to="`/app/edit/${appId}`"
              class="p-2 hover:bg-white/10 rounded-lg transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
              </svg>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="flex-1 flex overflow-hidden">
      <!-- 左侧对话面板 -->
      <div class="w-1/3 border-r border-white/10 flex flex-col">
        <!-- 对话历史 -->
        <div ref="chatContainer" class="flex-1 overflow-y-auto p-4 space-y-4" @scroll="handleScroll">
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory && !loadingHistory" class="text-center">
            <button
              @click="loadMoreHistory"
              class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors text-sm"
            >
              加载更多历史消息
            </button>
          </div>
          
          <!-- 加载中状态 -->
          <div v-if="loadingHistory" class="text-center">
            <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
            <p class="text-sm text-gray-400 mt-2">加载中...</p>
          </div>
          
          <div
            v-for="message in chatMessages"
            :key="message.id"
            :class="['flex', message.type === 'user' ? 'justify-end' : 'justify-start']"
          >
            <div
              :class="[
                'rounded-lg',
                message.type === 'user' 
                  ? 'max-w-xs lg:max-w-sm px-4 py-2 bg-blue-500 text-white' 
                  : 'max-w-full px-4 py-3 bg-white/5 text-gray-300 border border-white/10'
              ]"
            >
              <!-- 用户消息：普通文本 -->
              <div v-if="message.type === 'user'" class="text-sm">
                {{ message.content }}
              </div>
              <!-- AI消息：使用 Markdown 渲染器 -->
              <div v-else class="ai-message-content">
                <AiMessageRenderer :content="message.content" />
              </div>
              <div class="text-xs opacity-60 mt-2">
                {{ new Date(message.createTime).toLocaleTimeString() }}
              </div>
            </div>
          </div>
          
          <!-- AI生成中的简洁提示 -->
          <div v-if="isGenerating" class="flex justify-start">
            <div class="inline-flex items-center space-x-2 px-3 py-2 bg-white/5 border border-white/10 rounded-lg">
              <div class="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>
              <span class="text-sm text-gray-400">AI 正在思考...</span>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="border-t border-white/10 p-4">
          <!-- 选中元素信息展示 -->
          <div v-if="selectedElement" class="mb-3">
            <div class="flex items-start justify-between p-3 bg-blue-500/10 border border-blue-500/30 rounded-lg">
              <div class="flex-1 min-w-0">
                <div class="flex items-center space-x-2 mb-1">
                  <svg class="w-4 h-4 text-blue-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span class="text-sm font-medium text-blue-400">已选中元素</span>
                </div>
                <div class="text-xs text-gray-300 space-y-0.5">
                  <div class="flex items-center space-x-2">
                    <span class="text-gray-500">标签:</span>
                    <code class="px-1.5 py-0.5 bg-gray-800 rounded text-cyan-400">&lt;{{ selectedElement.tagName }}&gt;</code>
                  </div>
                  <div v-if="selectedElement.id" class="flex items-center space-x-2">
                    <span class="text-gray-500">ID:</span>
                    <code class="px-1.5 py-0.5 bg-gray-800 rounded text-green-400">#{{ selectedElement.id }}</code>
                  </div>
                  <div v-if="selectedElement.className" class="flex items-center space-x-2 overflow-hidden">
                    <span class="text-gray-500 flex-shrink-0">Class:</span>
                    <code class="px-1.5 py-0.5 bg-gray-800 rounded text-yellow-400 truncate">.{{ selectedElement.className.split(' ').slice(0, 2).join('.') }}</code>
                  </div>
                  <div v-if="selectedElement.textContent" class="flex items-start space-x-2">
                    <span class="text-gray-500 flex-shrink-0">文本:</span>
                    <span class="text-gray-300 truncate">"{{ selectedElement.textContent.slice(0, 50) }}{{ selectedElement.textContent.length > 50 ? '...' : '' }}"</span>
                  </div>
                </div>
              </div>
              <button
                @click="removeSelectedElement"
                class="ml-2 p-1 hover:bg-white/10 rounded transition-colors flex-shrink-0"
                title="移除选中"
              >
                <svg class="w-4 h-4 text-gray-400 hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>
          
          <form @submit.prevent="sendMessage" class="flex space-x-3">
            <div class="flex-1">
              <textarea
                v-model="inputMessage"
                :placeholder="selectedElement ? '描述您想对选中元素进行的修改...' : '描述您想要修改或添加的功能...'"
                class="w-full px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                rows="3"
                :disabled="isGenerating"
                @keydown.enter.ctrl="sendMessage"
              ></textarea>
            </div>
            
            <div class="flex flex-col space-y-2">
              <!-- 编辑模式按钮 -->
              <button
                type="button"
                @click="toggleEditMode"
                :class="[
                  'p-2 rounded-lg transition-all',
                  isEditMode 
                    ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/30' 
                    : 'bg-white/10 hover:bg-white/20 text-gray-300'
                ]"
                :title="isEditMode ? '退出编辑模式' : '进入编辑模式'"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path>
                </svg>
              </button>
              
              <!-- 发送按钮 -->
              <button
                type="submit"
                :disabled="!inputMessage.trim() || isGenerating"
                class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold p-2 rounded-lg transition-all glow-button"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                </svg>
              </button>
            </div>
          </form>
          
          <div class="mt-2 flex items-center justify-between text-xs text-gray-400">
            <span>按 Ctrl+Enter 发送消息</span>
            <span v-if="isEditMode" class="text-orange-400">
              📝 编辑模式：点击预览中的元素进行选择
            </span>
          </div>
        </div>
      </div>

      <!-- 右侧预览面板 -->
      <div class="flex-1 flex flex-col">
        <div class="p-4 border-b border-white/10">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <h3 class="text-lg font-semibold">实时预览</h3>
              <!-- 编辑模式指示器 -->
              <span 
                v-if="isEditMode" 
                class="px-2 py-1 text-xs font-medium bg-orange-500/20 text-orange-400 rounded-full border border-orange-500/30 animate-pulse"
              >
                ✏️ 编辑模式
              </span>
            </div>
            <div class="flex space-x-2">
              <button
                @click="refreshPreview"
                class="p-2 hover:bg-white/10 rounded-lg transition-colors"
                title="刷新预览"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                </svg>
              </button>
              
              <button
                @click="openInNewWindow"
                class="p-2 hover:bg-white/10 rounded-lg transition-colors"
                title="新窗口打开"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 预览iframe -->
        <div class="flex-1 bg-gray-900 relative">
          <iframe
            ref="previewFrame"
            :src="previewUrl"
            class="w-full h-full border-0"
            @load="onPreviewLoad"
          ></iframe>
          
          <!-- 编辑模式遮罩提示 -->
          <div 
            v-if="isEditMode && previewUrl === 'about:blank'"
            class="absolute inset-0 flex items-center justify-center bg-black/50"
          >
            <div class="text-center p-6">
              <div class="text-4xl mb-3">🎨</div>
              <p class="text-gray-300">请先生成应用内容</p>
              <p class="text-sm text-gray-500 mt-1">生成内容后即可进入编辑模式</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { App, ChatMessage } from '@/types'
import { showToast } from 'vant'
import { getStaticPreviewUrl } from '@/constants/codeGenType'
import axios from 'axios'
import { 
  type SelectedElementInfo, 
  injectEditorScript, 
  exitEditorMode, 
  clearSelection,
  createMessageListener,
  formatElementInfoForPrompt
} from '@/utils/visualEditor'
import AiMessageRenderer from '@/components/AiMessageRenderer.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 保持ID为字符串，避免精度丢失
const appId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string') {
    return id  // ✅ 直接返回字符串
  }
  return String(id)  // ✅ 转为字符串
})

// 添加日志确认ID正确性
console.log('Chat页面 - 路由参数ID:', route.params.id, '转换后ID:', appId.value)
// 状态
const inputMessage = ref('')
const previewUrl = ref('about:blank')
const eventSource = ref<EventSource | null>(null)
const previewFrame = ref<HTMLIFrameElement | null>(null)
const chatContainer = ref<HTMLDivElement | null>(null)
const hasMoreHistory = ref(false)
const loadingHistory = ref(false)
const lastCreateTime = ref<string | undefined>(undefined)
const isDownloading = ref(false)

// 可视化编辑模式状态
const isEditMode = ref(false)
const selectedElement = ref<SelectedElementInfo | null>(null)
let messageListenerCleanup: (() => void) | null = null

// 应用名称编辑状态
const isEditingName = ref(false)
const editingName = ref('')
const nameInputRef = ref<HTMLInputElement | null>(null)

// 用户滚动状态：用于判断用户是否正在查看历史消息
const userScrolledUp = ref(false)
let scrollTimeout: ReturnType<typeof setTimeout> | null = null

// 检测用户是否滚动到底部附近（允许一定误差）
const isNearBottom = () => {
  if (!chatContainer.value) return true
  const { scrollTop, scrollHeight, clientHeight } = chatContainer.value
  // 距离底部 100px 以内认为是在底部
  return scrollHeight - scrollTop - clientHeight < 100
}

// 处理用户滚动事件
const handleScroll = () => {
  if (!chatContainer.value) return
  
  // 清除之前的定时器
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
  }
  
  // 检测用户是否向上滚动（不在底部附近）
  if (!isNearBottom()) {
    userScrolledUp.value = true
  } else {
    // 用户滚动到底部，重置状态
    userScrolledUp.value = false
  }
}

// 滚动到聊天容器底部
const scrollToBottom = (smooth: boolean = true) => {
  if (chatContainer.value) {
    const behavior = smooth ? 'smooth' : 'auto'
    chatContainer.value.scrollTo({
      top: chatContainer.value.scrollHeight,
      behavior: behavior as ScrollBehavior
    })
    // 滚动到底部后重置用户滚动状态
    userScrolledUp.value = false
  }
}

const updatePreviewUrl = (app?: App | null) => {
  if (!app) {
    previewUrl.value = 'about:blank'
    return
  }

  // 判断是否应该显示预览
  // 条件：至少有2条对话记录
  const shouldShowPreview = chatMessages.value.length >= 2
  
  if (!shouldShowPreview) {
    previewUrl.value = 'about:blank'
    console.log('对话记录不足2条，不显示预览')
    return
  }

  // 使用统一的预览 URL 生成方法
  // 自动识别 Vue 项目并添加 dist 后缀
  const codeGenType = app.codeGenType || 'html'
  previewUrl.value = getStaticPreviewUrl(codeGenType, String(app.id))
  console.log('实时预览URL:', previewUrl.value, '代码类型:', codeGenType, '已部署:', !!app.deployedTime)
}

// 计算属性
const currentApp = computed(() => appStore.currentApp)
const chatMessages = computed(() => appStore.chatMessages)
const isGenerating = computed(() => appStore.isGenerating)
const generatingProgress = computed(() => appStore.generatingProgress)

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isGenerating.value) {
    return
  }

  let message = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 发送消息时重置用户滚动状态，确保自动跟随新消息
  userScrolledUp.value = false
  
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElement.value) {
    const elementInfo = formatElementInfoForPrompt(selectedElement.value)
    message = `${elementInfo}\n\n【用户需求】\n${message}`
    console.log('[Chat] 添加元素信息到提示词:', message)
  }
  
  // 发送后清除选中元素并退出编辑模式
  if (isEditMode.value) {
    exitEditModeHandler()
  }

  try {
    const source = await appStore.chatWithAI(appId.value, message)
    if (source) {
      eventSource.value = source
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    showToast('发送失败，请重试')
  }
}

// 部署当前应用
const deployCurrentApp = async () => {
  if (!currentApp.value) return

  // 如果已部署，直接跳转到部署的网站
  if (currentApp.value.deployedTime && currentApp.value.deployUrl) {
    console.log('跳转到已部署网站:', currentApp.value.deployUrl)
    window.open(currentApp.value.deployUrl, '_blank')
    return
  }

  // 未部署，执行部署操作
  try {
    showToast('正在部署...')
    const deployUrl = await appStore.deployApp(currentApp.value.id)
    if (deployUrl) {
      showToast('部署成功，正在跳转...')
      
      // 延迟跳转，确保部署完成
      setTimeout(() => {
        // 直接使用后端返回的完整 URL
        console.log('跳转到新部署网站:', deployUrl)
        window.open(deployUrl, '_blank')
      }, 500)
    }
  } catch (error) {
    console.error('部署失败:', error)
    showToast('部署失败，请重试')
  }
}

// 下载应用代码
const downloadCode = async () => {
  if (!currentApp.value || isDownloading.value) return

  isDownloading.value = true
  try {
    showToast('正在准备下载...')
    
    // 构建请求 URL
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const downloadUrl = `${baseURL}/app/download/${appId.value}`
    
    // 使用原始 axios 发送请求，设置 responseType 为 blob 以接收二进制数据
    // 使用原始 axios 而非封装的 request，避免响应拦截器干扰 blob 响应
    const response = await axios.get(downloadUrl, {
      responseType: 'blob',
      withCredentials: true,  // 携带 cookie 凭证
    })
    
    // 从响应头中提取文件名
    // 响应头格式: Content-Disposition: attachment; filename="xxx.zip"
    const contentDisposition = response.headers['content-disposition'] || ''
    let fileName = `app-${appId.value}.zip`  // 默认文件名
    
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=["']?([^"';\n]*)["']?/)
    if (filenameMatch && filenameMatch[1]) {
      fileName = decodeURIComponent(filenameMatch[1])
    }
    
    // 创建 Blob 对象并触发下载
    const blob = new Blob([response.data], { type: 'application/zip' })
    const blobUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(blobUrl)
    
    showToast('下载成功')
  } catch (error: any) {
    console.error('下载代码失败:', error)
    // 如果响应是 Blob 类型的错误信息，尝试解析
    if (error.response?.data instanceof Blob) {
      try {
        const text = await error.response.data.text()
        const errorData = JSON.parse(text)
        showToast(errorData.message || '下载失败，请重试')
      } catch {
        showToast('下载失败，请重试')
      }
    } else {
      showToast(error.message || '下载失败，请重试')
    }
  } finally {
    isDownloading.value = false
  }
}

// 刷新预览
const refreshPreview = () => {
  const frame = previewFrame.value
  if (frame) {
    frame.src = frame.src
  }
}

// 在新窗口打开
const openInNewWindow = () => {
  if (previewUrl.value === 'about:blank') {
    showToast('应用尚未部署')
    return
  }
  window.open(previewUrl.value, '_blank')
}

// 预览加载完成
const onPreviewLoad = () => {
  // 预览加载完成后的处理
  // 如果当前处于编辑模式，重新注入脚本
  if (isEditMode.value && previewFrame.value) {
    setTimeout(() => {
      injectEditorScript(previewFrame.value!)
    }, 500)
  }
}

// 切换编辑模式
const toggleEditMode = () => {
  if (!previewFrame.value) {
    showToast('预览窗口未加载')
    return
  }
  
  if (previewUrl.value === 'about:blank') {
    showToast('请先生成应用内容')
    return
  }
  
  isEditMode.value = !isEditMode.value
  
  if (isEditMode.value) {
    // 进入编辑模式
    showToast('编辑模式已启用，点击元素进行选择')
    
    // 注入编辑脚本
    setTimeout(() => {
      if (previewFrame.value) {
        injectEditorScript(previewFrame.value)
      }
    }, 100)
    
    // 设置消息监听器
    messageListenerCleanup = createMessageListener((info) => {
      console.log('[Chat] 收到选中元素:', info)
      selectedElement.value = info
    })
  } else {
    // 退出编辑模式
    exitEditModeHandler()
  }
}

// 退出编辑模式
const exitEditModeHandler = () => {
  isEditMode.value = false
  selectedElement.value = null
  
  if (previewFrame.value) {
    exitEditorMode(previewFrame.value)
  }
  
  if (messageListenerCleanup) {
    messageListenerCleanup()
    messageListenerCleanup = null
  }
}

// 移除选中的元素
const removeSelectedElement = () => {
  selectedElement.value = null
  if (previewFrame.value) {
    clearSelection(previewFrame.value)
  }
}

// 开始编辑应用名称
const startEditName = () => {
  if (!currentApp.value) return
  editingName.value = currentApp.value.name || ''
  isEditingName.value = true
  nextTick(() => {
    nameInputRef.value?.focus()
    nameInputRef.value?.select()
  })
}

// 保存应用名称
const saveAppName = async () => {
  if (!currentApp.value || !isEditingName.value) return
  
  const newName = editingName.value.trim()
  
  // 如果名称没有变化或为空，取消编辑
  if (!newName || newName === currentApp.value.name) {
    cancelEditName()
    return
  }
  
  try {
    const success = await appStore.updateApp({
      id: currentApp.value.id,
      name: newName
    })
    
    if (success) {
      showToast('应用名称已更新')
    }
  } catch (error) {
    console.error('更新应用名称失败:', error)
    showToast('更新失败，请重试')
  } finally {
    isEditingName.value = false
  }
}

// 取消编辑应用名称
const cancelEditName = () => {
  isEditingName.value = false
  editingName.value = ''
}

// 标志：是否已自动发送初始消息
const hasAutoSentInitialMessage = ref(false)

// 加载对话历史
const loadChatHistory = async () => {
  // 如果正在生成中，不要加载历史记录，避免覆盖正在生成的消息
  if (isGenerating.value) {
    console.log('正在生成中，跳过加载历史记录')
    return
  }
  
  loadingHistory.value = true
  try {
    const result = await appStore.fetchChatHistory(appId.value)
    hasMoreHistory.value = result.hasMore
    lastCreateTime.value = result.lastCreateTime
    console.log('对话历史加载成功，共', chatMessages.value.length, '条消息')
    
    // 首次加载完成后，滚动到底部
    setTimeout(() => scrollToBottom(false), 100)
  } catch (error) {
    console.error('加载对话历史失败:', error)
    showToast('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  if (!hasMoreHistory.value || loadingHistory.value) {
    return
  }
  
  // 记录当前滚动位置和内容高度
  const container = chatContainer.value
  const oldScrollHeight = container?.scrollHeight ?? 0
  const oldScrollTop = container?.scrollTop ?? 0
  
  loadingHistory.value = true
  try {
    const result = await appStore.fetchChatHistory(appId.value, lastCreateTime.value, true) // append=true
    hasMoreHistory.value = result.hasMore
    lastCreateTime.value = result.lastCreateTime
    console.log('加载更多历史消息成功，当前共', chatMessages.value.length, '条消息')
    
    // 加载完成后，调整滚动位置，保持用户当前查看的内容不变
    setTimeout(() => {
      if (container) {
        const newScrollHeight = container.scrollHeight
        const heightDiff = newScrollHeight - oldScrollHeight
        container.scrollTop = oldScrollTop + heightDiff
      }
    }, 50)
  } catch (error) {
    console.error('加载更多历史消息失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loadingHistory.value = false
  }
}

// 加载应用详情和对话历史
const loadAppData = async (retryCount = 0) => {
  try {
    const detail = await appStore.fetchAppDetail(appId.value)
    
    // 如果获取失败且重试次数少于3次，延迟后重试
    if (!detail && retryCount < 3) {
      console.log(`应用详情获取失败，${1000 * (retryCount + 1)}ms后重试...`)
      setTimeout(() => {
        loadAppData(retryCount + 1)
      }, 1000 * (retryCount + 1)) // 递增延迟：1s, 2s, 3s
      return
    }
    
    // 设置预览URL
    updatePreviewUrl(detail ?? null)

    // 加载对话历史
    await loadChatHistory()
    
    // 自动发送初始消息
    await autoSendInitialMessage(detail)
    
  } catch (error) {
    console.error('加载应用数据失败:', error)
    
    // 如果是刚创建的应用，可能还没完全写入数据库，尝试重试
    if (retryCount < 3) {
      console.log(`加载失败，${1000 * (retryCount + 1)}ms后重试...`)
      setTimeout(() => {
        loadAppData(retryCount + 1)
      }, 1000 * (retryCount + 1))
    } else {
      showToast('加载应用失败，请刷新页面重试')
    }
  }
}

// 自动发送初始消息
const autoSendInitialMessage = async (app?: App | null) => {
  // 避免重复发送
  if (hasAutoSentInitialMessage.value) {
    console.log('已发送过初始消息，跳过')
    return
  }
  
  // 检查是否有对话历史
  if (chatMessages.value.length > 0) {
    console.log('已有对话历史，不自动发送')
    hasAutoSentInitialMessage.value = true
    return
  }
  
  // 检查是否有初始提示词
  if (!app?.initPrompt) {
    console.log('没有初始提示词，不自动发送')
    return
  }
  
  // 检查是否是自己的应用
  const userStore = useUserStore()
  const currentUserId = userStore.user?.id
  if (currentUserId !== app.userId) {
    console.log('不是自己的应用，不自动发送')
    return
  }
  
  console.log('🚀 自动发送初始消息:', app.initPrompt)
  hasAutoSentInitialMessage.value = true
  
  // 延迟一点发送，确保页面已完全加载
  setTimeout(async () => {
    try {
      showToast('正在根据您的描述生成代码...')
      await appStore.chatWithAI(appId.value, app.initPrompt!)
    } catch (error) {
      console.error('自动发送初始消息失败:', error)
      showToast('自动生成代码失败，请手动发送消息')
    }
  }, 500)
}

// 页面加载时获取数据
onMounted(() => {
  loadAppData()
})

// 监听AI生成状态，生成完成后自动刷新预览
watch(isGenerating, (newVal, oldVal) => {
  // 从生成中变为完成状态
  if (oldVal === true && newVal === false && currentApp.value) {
    console.log('AI生成完成，刷新预览...')
    // 延迟刷新，确保文件已保存
    setTimeout(() => {
      refreshPreview()
      showToast('代码生成完成，预览已更新')
      
      // 如果对话记录达到2条或以上，更新预览URL
      if (chatMessages.value.length >= 2) {
        updatePreviewUrl(currentApp.value)
      }
    }, 1000)
  }
})

// 监听对话消息数量变化，更新预览URL和滚动位置
watch(() => chatMessages.value.length, (newLength, oldLength) => {
  if (newLength >= 2 && currentApp.value) {
    updatePreviewUrl(currentApp.value)
  }
  
  // 如果消息增加了（不是加载历史记录），滚动到底部（用户发送消息或AI回复）
  // 加载历史记录时会在专门的逻辑中处理滚动
  if (newLength > oldLength && !loadingHistory.value) {
    setTimeout(() => scrollToBottom(), 100)
  }
})

// 监听消息内容变化（AI流式输出时），平滑滚动到底部
// 但如果用户正在查看历史消息，则不强制滚动
watch(() => chatMessages.value.map(m => m.content).join(''), () => {
  if (isGenerating.value && !userScrolledUp.value) {
    scrollToBottom(true)
  }
})

// 页面卸载时清理资源
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close()
  }
  
  // 清理可视化编辑相关资源
  if (messageListenerCleanup) {
    messageListenerCleanup()
    messageListenerCleanup = null
  }
  
  // 清空当前应用
  appStore.setCurrentApp(null)
})
</script>

<style scoped>
/* 保留空样式块以备后续扩展 */
</style>