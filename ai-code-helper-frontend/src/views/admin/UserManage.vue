<template>
  <div class="min-h-screen pt-6 pb-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 管理后台子导航 -->
      <AdminNav />
      
      <!-- 页面标题 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold gradient-text">用户管理</h1>
          <p class="text-gray-400 mt-2">管理平台所有用户信息</p>
        </div>
      </div>

      <!-- 功能开发中提示 -->
      <div class="glass-dark rounded-2xl p-12 text-center">
        <div class="text-6xl mb-6">👥</div>
        <h2 class="text-2xl font-bold text-gray-300 mb-4">用户管理功能</h2>
        <p class="text-gray-400 mb-8 max-w-md mx-auto">
          用户管理功能正在开发中，敬请期待...
        </p>
        
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link 
            to="/admin/app" 
            class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-2 px-4 rounded-lg transition-all"
          >
            返回应用管理
          </router-link>
          
          <router-link 
            to="/" 
            class="bg-white/10 hover:bg-white/20 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
          >
            返回首页
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AdminNav from '@/components/layout/AdminNav.vue'
</script>