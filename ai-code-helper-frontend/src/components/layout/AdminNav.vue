<template>
  <div class="glass-dark rounded-xl p-4 mb-8">
    <div class="flex space-x-2 overflow-x-auto">
      <router-link
        to="/admin/app"
        class="admin-nav-item"
        :class="{ active: $route.path === '/admin/app' }"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
        </svg>
        应用管理
      </router-link>
      
      <router-link
        to="/admin/user"
        class="admin-nav-item"
        :class="{ active: $route.path === '/admin/user' }"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
        </svg>
        用户管理
      </router-link>
      
      <router-link
        to="/admin/chat"
        class="admin-nav-item"
        :class="{ active: $route.path === '/admin/chat' }"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
        </svg>
        对话管理
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.admin-nav-item {
  @apply flex items-center px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-all;
  @apply text-gray-300 bg-white/5 hover:bg-white/10;
}

.admin-nav-item.active {
  @apply bg-gradient-to-r from-blue-500 to-cyan-500 text-white;
}
</style>

