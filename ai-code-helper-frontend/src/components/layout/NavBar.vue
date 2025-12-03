<template>
  <nav class="glass-dark sticky top-0 z-50 border-b border-white/10">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- Logo和标题 -->
        <div class="flex items-center space-x-4">
          <router-link to="/" class="flex items-center space-x-2 group">
            <div class="w-8 h-8 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
              <span class="text-white font-bold text-sm">AI</span>
            </div>
            <span class="text-xl font-bold gradient-text">应用生成平台</span>
          </router-link>
        </div>

        <!-- 桌面端导航菜单 -->
        <div class="hidden md:flex items-center space-x-8">
          <router-link 
            to="/" 
            class="nav-link"
            :class="{ active: $route.path === '/' }"
          >
            首页
          </router-link>
          <router-link 
            to="/examples" 
            class="nav-link"
            :class="{ active: $route.path === '/examples' }"
          >
            精选案例
          </router-link>
          <router-link 
            v-if="userStore.isLogin" 
            to="/app/list" 
            class="nav-link"
            :class="{ active: $route.path.startsWith('/app') }"
          >
            我的应用
          </router-link>
          <router-link 
            v-if="userStore.isAdmin" 
            to="/admin/app" 
            class="nav-link"
            :class="{ active: $route.path.startsWith('/admin') }"
          >
            管理后台
          </router-link>
        </div>

        <!-- 用户操作区域 -->
        <div class="flex items-center space-x-4">
          <!-- 用户头像和菜单 -->
          <div v-if="userStore.isLogin" class="flex items-center space-x-3">
            <div class="relative group">
              <button class="flex items-center space-x-2 p-2 rounded-lg hover:bg-white/10 transition-all">
                <div class="w-9 h-9 rounded-full flex items-center justify-center ring-2 ring-white/20 group-hover:ring-white/40 transition-all overflow-hidden"
                  :class="{ 'bg-gradient-to-r from-blue-500 to-purple-500': !userStore.user?.avatar }"
                >
                  <img 
                    v-if="userStore.user?.avatar" 
                    :src="userStore.user.avatar" 
                    alt="头像"
                    class="w-full h-full object-cover"
                  />
                  <span v-else class="text-white font-semibold">
                    {{ getAvatarText(userStore.user?.nickname || userStore.user?.account) }}
                  </span>
                </div>
                <span class="hidden md:block text-sm font-medium">
                  {{ userStore.user?.nickname || userStore.user?.account }}
                </span>
                <svg class="hidden md:block w-4 h-4 transition-transform group-hover:rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </button>
              
              <!-- 用户菜单 -->
              <div class="absolute right-0 mt-3 w-56 glass-dark rounded-xl shadow-2xl border border-white/10 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 overflow-hidden">
                <!-- 用户信息卡片 -->
                <div class="p-4 bg-gradient-to-br from-blue-500/20 to-purple-500/20 border-b border-white/10">
                  <div class="flex items-center space-x-3">
                    <div class="w-12 h-12 rounded-full flex items-center justify-center ring-2 ring-white/30 overflow-hidden"
                      :class="{ 'bg-gradient-to-r from-blue-500 to-purple-500': !userStore.user?.avatar }"
                    >
                      <img 
                        v-if="userStore.user?.avatar" 
                        :src="userStore.user.avatar" 
                        alt="头像"
                        class="w-full h-full object-cover"
                      />
                      <span v-else class="text-white font-bold text-lg">
                        {{ getAvatarText(userStore.user?.nickname || userStore.user?.account) }}
                      </span>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-semibold truncate">{{ userStore.user?.nickname }}</p>
                      <p class="text-xs text-gray-400 truncate">{{ userStore.user?.account }}</p>
                      <span v-if="userStore.isAdmin" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gradient-to-r from-yellow-500/20 to-orange-500/20 text-yellow-400 border border-yellow-500/30 mt-1">
                        管理员
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 菜单项 -->
                <div class="py-2">
                  <router-link 
                    to="/profile" 
                    class="flex items-center px-4 py-2.5 text-sm hover:bg-white/10 transition-colors group/item"
                  >
                    <svg class="w-5 h-5 mr-3 text-cyan-400 group-hover/item:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    <span>个人信息</span>
                  </router-link>

                  <router-link 
                    to="/app/list" 
                    class="flex items-center px-4 py-2.5 text-sm hover:bg-white/10 transition-colors group/item"
                  >
                    <svg class="w-5 h-5 mr-3 text-blue-400 group-hover/item:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                    </svg>
                    <span>我的应用</span>
                  </router-link>
                  
                  <router-link 
                    v-if="userStore.isAdmin" 
                    to="/admin/app" 
                    class="flex items-center px-4 py-2.5 text-sm hover:bg-white/10 transition-colors group/item"
                  >
                    <svg class="w-5 h-5 mr-3 text-purple-400 group-hover/item:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    </svg>
                    <span>管理后台</span>
                  </router-link>
                </div>

                <!-- 退出登录按钮 -->
                <div class="p-2 border-t border-white/10">
                  <button 
                    @click="handleLogout" 
                    class="flex items-center justify-center w-full px-4 py-2.5 text-sm font-medium rounded-lg bg-gradient-to-r from-red-500/20 to-pink-500/20 border border-red-500/30 text-red-400 hover:from-red-500/30 hover:to-pink-500/30 hover:border-red-500/50 transition-all group/logout"
                  >
                    <svg class="w-5 h-5 mr-2 group-hover/logout:rotate-12 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                    </svg>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 登录注册按钮 -->
          <div v-else class="flex items-center space-x-3">
            <router-link 
              to="/login" 
              class="px-4 py-2 text-sm font-medium rounded-lg border border-blue-500 text-blue-500 hover:bg-blue-500 hover:text-white transition-colors"
            >
              登录
            </router-link>
            <router-link 
              to="/register" 
              class="px-4 py-2 text-sm font-medium rounded-lg bg-gradient-to-r from-blue-500 to-cyan-500 text-white hover:from-blue-600 hover:to-cyan-600 transition-all"
            >
              注册
            </router-link>
          </div>

          <!-- 移动端菜单按钮 -->
          <button 
            @click="showMobileMenu = !showMobileMenu" 
            class="md:hidden p-2 rounded-lg hover:bg-white/10 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- 移动端菜单 -->
      <transition name="slide-down">
        <div v-if="showMobileMenu" class="md:hidden glass-dark border-t border-white/10">
          <div class="px-2 pt-2 pb-3 space-y-1">
            <router-link 
              to="/" 
              class="mobile-nav-link"
              :class="{ active: $route.path === '/' }"
            >
              首页
            </router-link>
            <router-link 
              to="/examples" 
              class="mobile-nav-link"
              :class="{ active: $route.path === '/examples' }"
            >
              精选案例
            </router-link>
            <router-link 
              v-if="userStore.isLogin" 
              to="/app/list" 
              class="mobile-nav-link"
              :class="{ active: $route.path.startsWith('/app') }"
            >
              我的应用
            </router-link>
            <router-link 
              v-if="userStore.isAdmin" 
              to="/admin/app" 
              class="mobile-nav-link"
              :class="{ active: $route.path.startsWith('/admin') }"
            >
              管理后台
            </router-link>
          </div>
        </div>
      </transition>
    </div>

    <!-- 自定义退出确认对话框 -->
    <transition name="fade">
      <div
        v-if="showLogoutConfirm"
        class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
        @click.self="cancelLogout"
      >
        <div class="glass-dark max-w-md w-full rounded-2xl shadow-2xl border border-white/20 transform transition-all animate-scale-in">
          <!-- 头部 -->
          <div class="p-6 border-b border-white/10">
            <div class="flex items-center space-x-3">
              <div class="w-12 h-12 bg-gradient-to-r from-red-500 to-pink-500 rounded-full flex items-center justify-center">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-bold text-white">确认退出</h3>
                <p class="text-sm text-gray-400">您确定要退出登录吗？</p>
              </div>
            </div>
          </div>

          <!-- 内容 -->
          <div class="p-6">
            <p class="text-gray-300 leading-relaxed">
              退出登录后，您需要重新登录才能使用应用生成、管理等功能。
            </p>
          </div>

          <!-- 底部按钮 -->
          <div class="p-6 border-t border-white/10 flex gap-3">
            <button
              @click="cancelLogout"
              class="flex-1 px-4 py-3 rounded-lg border border-white/20 hover:bg-white/10 transition-all font-medium"
            >
              取消
            </button>
            <button
              @click="confirmLogout"
              class="flex-1 px-4 py-3 rounded-lg bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600 transition-all font-medium text-white shadow-lg"
            >
              确定退出
            </button>
          </div>
        </div>
      </div>
    </transition>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const showMobileMenu = ref(false)
const showLogoutConfirm = ref(false)

// 获取头像文本（用于无头像时显示）
// 中文显示第一个字，英文显示首字母大写
const getAvatarText = (name?: string) => {
  if (!name) return 'U'
  const firstChar = name.charAt(0)
  // 判断是否为中文字符
  if (/[\u4e00-\u9fa5]/.test(firstChar)) {
    return firstChar
  }
  // 英文或其他字符，返回大写首字母
  return firstChar.toUpperCase()
}

// 退出登录
const handleLogout = () => {
  showLogoutConfirm.value = true
}

// 确认退出
const confirmLogout = async () => {
  showLogoutConfirm.value = false
  await userStore.logout()
  router.push('/')
}

// 取消退出
const cancelLogout = () => {
  showLogoutConfirm.value = false
}
</script>

<style scoped>
.nav-link {
  @apply px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:bg-white/10;
}

.nav-link.active {
  @apply bg-gradient-to-r from-blue-500 to-cyan-500 text-white;
}

.mobile-nav-link {
  @apply block px-3 py-2 text-base font-medium rounded-lg transition-colors hover:bg-white/10;
}

.mobile-nav-link.active {
  @apply bg-gradient-to-r from-blue-500 to-cyan-500 text-white;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 对话框淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 对话框缩放动画 */
@keyframes scale-in {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-scale-in {
  animation: scale-in 0.3s ease-out;
}
</style>