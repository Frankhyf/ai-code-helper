// 用户类型定义
export interface User {
  id: number
  account: string
  nickname: string
  avatar?: string
  profile?: string
  role: 'user' | 'admin'
  createTime: string
  updateTime: string
}

// 应用类型定义
export interface App {
  id: number
  name: string
  description?: string
  cover?: string
  userId: number
  userVO?: User
  type: 'single' | 'multiple'
  status: 'draft' | 'deployed' | 'featured'
  priority: number
  generateTime?: string
  createTime: string
  updateTime: string
  // 后端原始字段
  appName?: string
  initPrompt?: string
  codeGenType?: string
  deployKey?: string
  deployUrl?: string  // 完整的部署 URL（由后端返回）
  deployedTime?: string
}

// 对话消息类型
export interface ChatMessage {
  id: number
  appId: number | string
  userId: number
  type: 'user' | 'ai' | 'system'
  content: string
  createTime: string
}

// 对话历史类型
export interface ChatHistory {
  id: number
  appId: number
  userId: number
  userVO?: User
  messages: ChatMessage[]
  createTime: string
  updateTime: string
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页响应类型
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 分页请求参数
export interface PageParams {
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 应用搜索参数
export interface AppSearchParams extends PageParams {
  name?: string
  type?: string
  status?: string
  userId?: number
}

// 用户搜索参数
export interface UserSearchParams extends PageParams {
  account?: string
  nickname?: string
  role?: string
}

// 对话搜索参数
export interface ChatSearchParams extends PageParams {
  appId?: number
  userId?: number
  content?: string
}

// 登录表单
export interface LoginForm {
  account: string
  password: string
}

// 注册表单
export interface RegisterForm {
  account: string
  password: string
  confirmPassword: string
  nickname?: string
}

// 用户信息更新表单
export interface UserUpdateMyForm {
  userName?: string
  userAvatar?: string
  userProfile?: string
}

// 应用创建表单
export interface AppCreateForm {
  name: string
  description?: string
  type?: 'single' | 'multiple' // 保留以兼容旧代码
  codeGenType?: string // 新的代码生成类型字段
  prompt: string
}

// 应用更新表单
export interface AppUpdateForm {
  id: number
  name?: string
  description?: string
  cover?: string
  priority?: number
  status?: string
}

// SSE消息类型
export interface SSEMessage {
  type: 'start' | 'progress' | 'complete' | 'error'
  content: string
  data?: any
}