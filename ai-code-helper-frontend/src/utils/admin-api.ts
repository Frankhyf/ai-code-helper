import { http } from './request'
import type { App, AppSearchParams, PageResponse, User } from '@/types'

/**
 * 管理员 API 服务
 */

// 管理员应用管理接口
export const adminAppApi = {
  /**
   * 管理员获取应用列表（分页）
   */
  async listApps(params: AppSearchParams): Promise<PageResponse<App> | null> {
    try {
      const payload = {
        pageNum: params.current ?? 1,
        pageSize: params.size ?? 20,
        appName: params.name,
        codeGenType: params.type === 'multiple' ? 'multi_file' : params.type === 'single' ? 'html' : params.type,
        userId: params.userId,
      }
      const response = await http.post<PageResponse<App>>('/app/admin/list/page/vo', payload)
      return response
    } catch (error) {
      console.error('管理员获取应用列表失败:', error)
      return null
    }
  },

  /**
   * 管理员获取应用详情
   */
  async getAppDetail(id: number): Promise<App | null> {
    try {
      const app = await http.get<App>(`/app/admin/get/vo?id=${id}`)
      return app
    } catch (error) {
      console.error('管理员获取应用详情失败:', error)
      return null
    }
  },

  /**
   * 管理员更新应用
   */
  async updateApp(params: {
    id: number
    appName?: string
    initPrompt?: string
    codeGenType?: string
    priority?: number
  }): Promise<boolean> {
    try {
      await http.post<boolean>('/app/admin/update', params)
      return true
    } catch (error) {
      console.error('管理员更新应用失败:', error)
      return false
    }
  },

  /**
   * 管理员删除应用
   */
  async deleteApp(id: number): Promise<boolean> {
    try {
      await http.post<boolean>('/app/admin/delete', { id })
      return true
    } catch (error) {
      console.error('管理员删除应用失败:', error)
      return false
    }
  },
}

// 管理员用户管理接口
export const adminUserApi = {
  /**
   * 获取所有用户列表
   */
  async listUsers(): Promise<User[]> {
    try {
      const users = await http.get<User[]>('/user/list')
      return users
    } catch (error) {
      console.error('获取用户列表失败:', error)
      return []
    }
  },

  /**
   * 获取用户详情
   */
  async getUserInfo(id: number): Promise<User | null> {
    try {
      const user = await http.get<User>(`/user/getInfo/${id}`)
      return user
    } catch (error) {
      console.error('获取用户详情失败:', error)
      return null
    }
  },

  /**
   * 添加用户
   */
  async addUser(params: {
    userAccount: string
    userName?: string
    userRole?: string
  }): Promise<number | null> {
    try {
      const userId = await http.post<number>('/user/add', params)
      return userId
    } catch (error) {
      console.error('添加用户失败:', error)
      return null
    }
  },

  /**
   * 更新用户
   */
  async updateUser(user: Partial<User> & { id: number }): Promise<boolean> {
    try {
      await http.put<boolean>('/user/update', user)
      return true
    } catch (error) {
      console.error('更新用户失败:', error)
      return false
    }
  },

  /**
   * 删除用户
   */
  async deleteUser(id: number): Promise<boolean> {
    try {
      await http.delete<boolean>(`/user/remove/${id}`)
      return true
    } catch (error) {
      console.error('删除用户失败:', error)
      return false
    }
  },
}

