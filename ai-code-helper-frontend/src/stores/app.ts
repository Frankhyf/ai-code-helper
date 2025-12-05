import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import type { App, AppSearchParams, AppCreateForm, AppUpdateForm, ChatMessage, User } from '@/types'
import { http } from '@/utils/request'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

export const useAppStore = defineStore('app', () => {
  // 状态
  const apps = ref<App[]>([])
  const currentApp = ref<App | null>(null)
  const featuredApps = ref<App[]>([])
  const myApps = ref<App[]>([])
  const chatMessages = ref<ChatMessage[]>([])
  const isGenerating = ref(false)
  const generatingProgress = ref(0)
  
  // 计算属性
  const totalApps = computed(() => apps.value.length)
  const hasMoreApps = ref(true)
  
  const mapToUser = (data: any): User => ({
    id: data?.id ?? 0,
    account: data?.userAccount ?? data?.account ?? '',
    nickname: data?.userName ?? data?.nickname ?? data?.userAccount ?? '',
    avatar: data?.userAvatar ?? data?.avatar,
    role: data?.userRole === 'admin' ? 'admin' : 'user',
    createTime: data?.createTime ?? '',
    updateTime: data?.updateTime ?? '',
  })
  
  const transformApp = (data: any): App => {
    const codeGenType = data?.codeGenType ?? data?.type
    const deployKey = data?.deployKey
    const isFeatured = data?.priority === 99
    const status = deployKey ? 'deployed' : isFeatured ? 'featured' : 'draft'
    return {
      id: data?.id ?? 0,
      name: data?.appName ?? data?.name ?? `应用-${data?.id ?? ''}`,
      description: data?.initPrompt ?? data?.description ?? '',
      cover: data?.cover?.startsWith('@') ? data.cover.substring(1) : data?.cover,
      userId: data?.userId ?? data?.user?.id ?? 0,
      userVO: data?.user ? mapToUser(data.user) : undefined,
      type: codeGenType === 'multi_file' ? 'multiple' : 'single',
      status,
      priority: data?.priority ?? 0,
      generateTime: data?.deployedTime ?? data?.editTime ?? '',
      createTime: data?.createTime ?? '',
      updateTime: data?.updateTime ?? data?.editTime ?? '',
      appName: data?.appName ?? data?.name,
      initPrompt: data?.initPrompt,
      codeGenType,
      deployKey,
      deployedTime: data?.deployedTime ?? '',
    }
  }
  
  const transformAppList = (list: any[] = []) => list.map(transformApp)
  
  const buildQueryParams = (params: AppSearchParams = {}) => {
    const payload: Record<string, any> = {
      pageNum: params.current ?? 1,
      pageSize: params.size ?? 10,
      sortField: 'createTime',
      sortOrder: 'descend'
    }
    if (params.name) {
      payload.appName = params.name
    }
    if (params.type) {
      payload.codeGenType = params.type === 'multiple' ? 'multi_file' : params.type === 'single' ? 'html' : params.type
    }
    if (params.userId) {
      payload.userId = params.userId
    }
    return payload
  }
  
  // 获取应用列表
  const fetchApps = async (params: AppSearchParams = {}) => {
    try {
      const payload = buildQueryParams(params)
      const response = await http.post('/app/good/list/page/vo', payload)
      const records = transformAppList(response?.records ?? [])
      if (payload.pageNum === 1) {
        apps.value = records
      } else {
        apps.value.push(...records)
      }
      // 后端分页对象：current(当前页), pages(总页数), records(记录), totalRow(总记录数)
      const current = response?.current ?? response?.pageNum ?? 1
      const pages = response?.pages ?? response?.totalPages ?? 1
      const total = response?.totalRow ?? response?.total ?? 0
      hasMoreApps.value = current < pages
      return {
        records,
        current,
        pages,
        total,
        size: response?.size ?? payload.pageSize,
      }
    } catch (error) {
      console.error('获取应用列表失败:', error)
      return null
    }
  }

  // 获取精选应用
  const fetchFeaturedApps = async (params: AppSearchParams = {}) => {
    try {
      const payload = buildQueryParams(params)
      const response = await http.post('/app/good/list/page/vo', payload)
      const records = transformAppList(response?.records ?? [])
      featuredApps.value = records
      const current = response?.current ?? response?.pageNum ?? 1
      const pages = response?.pages ?? response?.totalPages ?? 1
      const total = response?.totalRow ?? response?.total ?? 0
      return {
        records,
        current,
        pages,
        total,
        size: response?.size ?? payload.pageSize,
      }
    } catch (error) {
      console.warn('获取精选应用失败，后端服务可能未启动')
      featuredApps.value = []
      return { records: [], total: 0, size: 0, current: 0, pages: 0 }
    }
  }

  // 获取我的应用
  const fetchMyApps = async (params: AppSearchParams = {}) => {
    try {
      const payload = buildQueryParams(params)
      const response = await http.post('/app/my/list/page/vo', payload)
      const records = transformAppList(response?.records ?? [])
      
      // 分页模式：直接替换数据，不再追加
      myApps.value = records
      
      const current = response?.current ?? response?.pageNum ?? 1
      const pages = response?.pages ?? response?.totalPages ?? 1
      const total = response?.totalRow ?? response?.total ?? 0
      return {
        records,
        current,
        pages,
        total,
        size: response?.size ?? payload.pageSize,
      }
    } catch (error) {
      console.error('获取我的应用失败:', error)
      return null
    }
  }

  // 获取应用详情（支持字符串ID，避免大整数精度丢失）
  const fetchAppDetail = async (id: number | string) => {
    try {
      console.log('正在获取应用详情，ID:', id, '类型:', typeof id)
      const app = await http.get(`/app/get/vo?id=${id}`)
      const normalized = transformApp(app)
      currentApp.value = normalized
      console.log('应用详情获取成功:', normalized)
      return normalized
    } catch (error) {
      console.error('获取应用详情失败，ID:', id, '错误:', error)
      return null
    }
  }

  // 创建应用
  const createApp = async (form: AppCreateForm) => {
    try {
      const initPrompt = form.prompt
      
      // 优先使用新的 codeGenType 字段，否则根据旧的 type 字段映射
      let codeGenType: string
      if (form.codeGenType) {
        codeGenType = form.codeGenType
      } else if (form.type) {
        // 将前端的 type 映射为后端的 codeGenType（兼容旧代码）
        // single -> html (单文件HTML)
        // multiple -> multi_file (多文件项目)
        codeGenType = form.type === 'single' ? 'html' : 'multi_file'
      } else {
        codeGenType = 'html' // 默认值
      }
      
      console.log('创建应用请求参数:', { initPrompt, codeGenType })
      
      const response = await http.post<number | string>('/app/add', {
        initPrompt,
        codeGenType,  // 添加代码生成类型参数
      })
      
      console.log('=== 创建应用ID追踪 ===')
      console.log('1. 原始响应:', response)
      console.log('2. 响应类型:', typeof response)
      
      // ✅ 保持ID为字符串，避免精度丢失
      let appId: string
      if (typeof response === 'string') {
        console.log('✅ 收到string类型的ID，精度安全')
        appId = response
      } else if (typeof response === 'number') {
        console.warn('⚠️ 警告：收到number类型的ID，转换为字符串')
        appId = String(response)
      } else {
        console.error('应用ID格式错误:', response)
        showToast('应用创建失败：返回数据格式错误')
        return null
      }
      
      console.log('3. 最终appId（字符串）:', appId)
      console.log('===================')
      
      if (appId) {
        showToast('应用创建成功')
        // 不需要立即获取详情，跳转到对话页面后再获取
        // 这样可以避免时序问题（应用刚创建，数据库可能还没完全写入）
        return appId
      }
      
      console.error('应用ID无效:', appId)
      showToast('应用创建失败：应用ID无效')
      return null
    } catch (error) {
      console.error('创建应用失败:', error)
      // 显示更详细的错误信息
      if (error instanceof Error) {
        showToast(error.message || '创建应用失败，请重试')
      } else {
        showToast('创建应用失败，请重试')
      }
      return null
    }
  }

  // 更新应用
  const updateApp = async (form: AppUpdateForm) => {
    try {
      const payload = {
        id: form.id,
        appName: form.name,
      }
      await http.post('/app/update', payload)
      showToast('应用更新成功')
      
      if (currentApp.value && currentApp.value.id === form.id) {
        currentApp.value = {
          ...currentApp.value,
          name: form.name ?? currentApp.value.name,
          appName: form.name ?? currentApp.value.appName,
        }
      }
      const index = apps.value.findIndex(app => app.id === form.id)
      if (index !== -1) {
        apps.value[index] = {
          ...apps.value[index],
          name: form.name ?? apps.value[index].name,
          appName: form.name ?? apps.value[index].appName,
        }
      }
      return true
    } catch (error) {
      console.error('更新应用失败:', error)
      return false
    }
  }

  // 删除应用
  const deleteApp = async (id: number | string) => {
    try {
      await http.post('/app/delete', { id })
      showToast('应用删除成功')
      
      // 从列表中移除
      apps.value = apps.value.filter(app => app.id !== id)
      myApps.value = myApps.value.filter(app => app.id !== id)
      if (currentApp.value?.id === id) {
        currentApp.value = null
      }
      
      return true
    } catch (error) {
      console.error('删除应用失败:', error)
      return false
    }
  }

  // 部署应用
  const deployApp = async (id: number | string) => {
    try {
      // 后端返回完整的部署URL（如 http://175.178.105.217/dist/Kehanf/）
      const deployUrl = await http.post<string>('/app/deploy', { appId: id })
      
      // 从URL中提取deployKey（例如：Kehanf）
      let deployKey: string | undefined
      if (typeof deployUrl === 'string') {
        // 匹配 URL 最后一个路径段作为 deployKey
        const match = deployUrl.match(/\/([^\/]+)\/?$/)
        deployKey = match ? match[1] : undefined
      }
      
      // 更新当前应用状态
      if (currentApp.value && currentApp.value.id === id) {
        currentApp.value = {
          ...currentApp.value,
          status: 'deployed',
          deployKey: deployKey ?? currentApp.value.deployKey,
          deployUrl: typeof deployUrl === 'string' ? deployUrl : currentApp.value.deployUrl,
        }
      }
      
      // 更新应用列表中的状态
      const updateAppInList = (list: App[]) => {
        const index = list.findIndex(app => app.id === id)
        if (index !== -1) {
          list[index] = {
            ...list[index],
            status: 'deployed',
            deployKey: deployKey ?? list[index].deployKey,
            deployUrl: typeof deployUrl === 'string' ? deployUrl : list[index].deployUrl,
          }
        }
      }
      
      updateAppInList(apps.value)
      updateAppInList(myApps.value)
      
      return deployUrl
    } catch (error) {
      console.error('部署应用失败:', error)
      return null
    }
  }

  // AI对话生成
  const chatWithAI = async (appId: number | string, prompt: string) => {
    const userStore = useUserStore()
    const currentUser = userStore.user
    isGenerating.value = true
    generatingProgress.value = 0

    const timestamp = new Date().toISOString()
    const userMessage: ChatMessage = {
      id: Date.now(),
      appId,
      userId: currentUser?.id ?? 0,
      type: 'user',
      content: prompt,
      createTime: timestamp,
    }
    chatMessages.value.push(userMessage)

    let aiMessage: ChatMessage | null = null
    let aiContent = ''
    let totalChars = 0  // 累计接收的字符数
    
    // 根据代码类型估算总长度
    // 后端配置: max_tokens: 4096 (单文件) / 8192 (多文件)
    // 实际生成通常为 max_tokens 的 70-80%
    
    // 获取当前应用的代码类型
    const app = currentApp.value
    const isMultiFile = app?.codeGenType === 'multi_file'
    
    // 估算参数 (可根据实际情况调整)
    const MAX_TOKENS = isMultiFile ? 8192 : 4096  // 后端配置的最大tokens
    const ACTUAL_RATIO = 0.75                      // 实际生成约为max_tokens的75%
    const CHARS_PER_TOKEN = 3.5                    // 代码平均每token的字符数
    
    const estimatedTokens = Math.round(MAX_TOKENS * ACTUAL_RATIO)
    const estimatedTotalChars = estimatedTokens * CHARS_PER_TOKEN
    
    console.log(`📝 进度估算参数: max_tokens=${MAX_TOKENS}, 预计${estimatedTokens}tokens (${estimatedTotalChars}字符)`)

    try {
      const source = http.sse(
        `/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(prompt)}`,
        (payload) => {
          const chunk = typeof payload === 'string' ? payload : payload?.d ?? ''

          if (!chunk) {
            return
          }

          // 累加完整内容（包括AI解释）
          aiContent += chunk
          totalChars = aiContent.length
          
          // 基于实际接收字符数与估算总字符数的比例计算进度
          const rawProgress = (totalChars / estimatedTotalChars) * 100
          
          // 应用压缩函数，让进度在95%处饱和
          const smoothProgress = 95 * (1 - Math.exp(-rawProgress / 50))
          const newProgress = Math.min(95, Math.round(smoothProgress))
          
          // 估算已接收的token数
          const estimatedTokensReceived = Math.round(totalChars / CHARS_PER_TOKEN)
          const tokenProgress = Math.round((estimatedTokensReceived / estimatedTokens) * 100)
          
          // 只在进度变化时输出日志
          if (newProgress > generatingProgress.value) {
            console.log(
              `📊 进度: ${newProgress}% | ` +
              `Token进度: ${tokenProgress}% (${estimatedTokensReceived}/${estimatedTokens}) | ` +
              `字符: ${totalChars}`
            )
          }
          
          generatingProgress.value = newProgress
          
          // 创建或更新AI消息（显示完整内容）
          if (!aiMessage) {
            aiMessage = {
              id: Date.now() + 1,
              appId,
              userId: 0,
              type: 'ai',
              content: aiContent,
              createTime: new Date().toISOString(),
            }
            chatMessages.value.push(aiMessage)
          } else {
            // 更新现有消息内容
            aiMessage.content = aiContent
          }

          // 触发Vue响应式更新
          chatMessages.value = [...chatMessages.value]
        },
        (error) => {
          isGenerating.value = false
          generatingProgress.value = 0
          showToast('对话连接失败')
          console.error('SSE错误:', error)
        },
        async () => {
          isGenerating.value = false
          generatingProgress.value = 100
          
          // 输出最终统计，用于优化估算参数
          const finalTokens = Math.round(totalChars / CHARS_PER_TOKEN)
          const tokenUtilization = Math.round((finalTokens / MAX_TOKENS) * 100)
          console.log(
            `✅ 生成完成! ` +
            `实际tokens: ${finalTokens} (${tokenUtilization}% of max_tokens ${MAX_TOKENS}) | ` +
            `总字符数: ${totalChars} | ` +
            `实际比例: ${(finalTokens / MAX_TOKENS).toFixed(2)}`
          )
          console.log(`💡 建议: 如果实际tokens与估算${estimatedTokens}差异较大，可调整ACTUAL_RATIO参数`)
          
          // 延迟一下，让后端有时间保存到数据库
          setTimeout(async () => {
            console.log('🔄 刷新对话历史，获取数据库中的真实ID...')
            await refreshLatestChatHistory(appId)
          }, 1000)
        },
        // 业务错误处理（限流、权限等）
        (errorData) => {
          isGenerating.value = false
          generatingProgress.value = 0
          
          const errorMessage = errorData.message || '生成过程中出现错误'
          console.error('SSE业务错误:', errorData)
          
          // 如果AI消息已创建，更新为错误消息
          if (aiMessage) {
            aiMessage.content = `❌ ${errorMessage}`
            chatMessages.value = [...chatMessages.value]
          } else {
            // 创建一个错误消息
            const errorAiMessage: ChatMessage = {
              id: Date.now() + 1,
              appId,
              userId: 0,
              type: 'ai',
              content: `❌ ${errorMessage}`,
              createTime: new Date().toISOString(),
            }
            chatMessages.value.push(errorAiMessage)
          }
          
          showToast(errorMessage)
        }
      )

      return source
    } catch (error) {
      isGenerating.value = false
      generatingProgress.value = 0
      console.error('AI对话失败:', error)
      showToast('AI对话失败，请重试')
      return null
    }
  }

  // 获取对话历史（支持游标分页）
  const fetchChatHistory = async (appId: number | string, lastCreateTime?: string, append: boolean = false) => {
    try {
      const params: any = {
        appId,
        pageSize: 10,
      }
      
      // 如果提供了 lastCreateTime，则获取下一页
      if (lastCreateTime) {
        params.lastCreateTime = lastCreateTime
      }
      
      const response = await http.get(`/chatHistory/app/${appId}`, { params })
      
      // 转换对话历史为 ChatMessage 格式
      const records = response?.records ?? []
      let messages: ChatMessage[] = records.map((record: any) => ({
        id: record.id ?? Date.now(),
        appId: record.appId ?? appId,
        userId: record.userId ?? 0,
        type: record.messageType === 'user' ? 'user' : 'ai',
        content: record.message ?? '',
        createTime: record.createTime ?? new Date().toISOString(),
      }))
      
      // 反转消息顺序，确保最旧的消息在前，最新的消息在后
      // 后端返回的是降序（新到旧），需要反转成升序（旧到新）
      messages = messages.reverse()
      
      // 根据 append 参数决定是替换还是追加
      if (append) {
        // 在开头插入旧消息（加载更多）
        chatMessages.value = [...messages, ...chatMessages.value]
      } else {
        // 替换所有消息（首次加载或刷新）
        chatMessages.value = messages
      }
      
      return {
        messages,
        hasMore: messages.length >= 10, // 如果返回了10条，可能还有更多
        lastCreateTime: messages.length > 0 ? messages[0].createTime : undefined,
      }
    } catch (error) {
      console.error('获取对话历史失败:', error)
      return {
        messages: [],
        hasMore: false,
        lastCreateTime: undefined,
      }
    }
  }
  
  // 刷新最新的对话历史（在AI回复完成后调用）
  const refreshLatestChatHistory = async (appId: number | string) => {
    try {
      const params: any = {
        appId,
        pageSize: 10,
      }
      
      const response = await http.get(`/chatHistory/app/${appId}`, { params })
      
      // 转换对话历史为 ChatMessage 格式
      const records = response?.records ?? []
      let messages: ChatMessage[] = records.map((record: any) => ({
        id: record.id ?? Date.now(),
        appId: record.appId ?? appId,
        userId: record.userId ?? 0,
        type: record.messageType === 'user' ? 'user' : 'ai',
        content: record.message ?? '',
        createTime: record.createTime ?? new Date().toISOString(),
      }))
      
      // 反转消息顺序，确保最旧的消息在前，最新的消息在后
      messages = messages.reverse()
      
      // 只保留最新的10条消息（来自数据库）
      // 如果当前消息列表更多，说明用户加载了更多历史，需要合并
      if (chatMessages.value.length > 10) {
        // 保留旧的历史消息，只更新最新的10条
        const oldMessages = chatMessages.value.slice(0, chatMessages.value.length - 10)
        chatMessages.value = [...oldMessages, ...messages]
      } else {
        // 直接替换
        chatMessages.value = messages
      }
      
      console.log('刷新最新对话历史成功，当前共', chatMessages.value.length, '条消息')
      return true
    } catch (error) {
      console.error('刷新对话历史失败:', error)
      return false
    }
  }

  // 设置当前应用
  const setCurrentApp = (app: App | null) => {
    currentApp.value = app ? transformApp(app) : null
  }

  // 清空应用数据
  const clearApps = () => {
    apps.value = []
    currentApp.value = null
    featuredApps.value = []
    myApps.value = []
    chatMessages.value = []
  }

  return {
    apps,
    currentApp,
    featuredApps,
    myApps,
    chatMessages,
    isGenerating,
    generatingProgress,
    totalApps,
    hasMoreApps,
    fetchApps,
    fetchFeaturedApps,
    fetchMyApps,
    fetchAppDetail,
    createApp,
    updateApp,
    deleteApp,
    deployApp,
    chatWithAI,
    fetchChatHistory,
    refreshLatestChatHistory,
    setCurrentApp,
    clearApps,
  }
})