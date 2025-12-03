<template>
  <div class="fixed bottom-0 left-0 right-0 glass-dark border-t border-white/10 z-40 md:hidden">
    <div class="grid grid-cols-4 h-16">
      <router-link 
        to="/" 
        class="tab-item"
        :class="{ active: $route.path === '/' }"
      >
        <div class="tab-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
          </svg>
        </div>
        <span class="tab-text">首页</span>
      </router-link>

      <router-link 
        to="/examples" 
        class="tab-item"
        :class="{ active: $route.path === '/examples' }"
      >
        <div class="tab-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
          </svg>
        </div>
        <span class="tab-text">案例</span>
      </router-link>

      <router-link 
        v-if="userStore.isLogin" 
        to="/app/list" 
        class="tab-item"
        :class="{ active: $route.path.startsWith('/app') }"
      >
        <div class="tab-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
          </svg>
        </div>
        <span class="tab-text">应用</span>
      </router-link>

      <router-link 
        v-if="userStore.isLogin && userStore.isAdmin" 
        to="/admin/app" 
        class="tab-item"
        :class="{ active: $route.path.startsWith('/admin') }"
      >
        <div class="tab-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
        </div>
        <span class="tab-text">管理</span>
      </router-link>

      <!-- 未登录时的占位 -->
      <div 
        v-else-if="!userStore.isLogin" 
        class="tab-item"
        @click="showLoginTip"
      >
        <div class="tab-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
          </svg>
        </div>
        <span class="tab-text">我的</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 显示登录提示
const showLoginTip = () => {
  showToast('请先登录')
  router.push('/login')
}
</script>

<style scoped>
.tab-item {
  @apply flex flex-col items-center justify-center space-y-1 text-gray-400 transition-colors;
}

.tab-item.active {
  @apply text-blue-500;
}

.tab-icon {
  @apply w-6 h-6 flex items-center justify-center;
}

.tab-text {
  @apply text-xs font-medium;
}
</style>