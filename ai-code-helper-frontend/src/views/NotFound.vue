<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <div class="text-center">
      <div class="text-8xl mb-8">🚀</div>
      <h1 class="text-6xl font-bold gradient-text mb-4">404</h1>
      <h2 class="text-2xl font-semibold text-gray-300 mb-4">页面不存在</h2>
      <p class="text-gray-400 mb-8 max-w-md mx-auto">
        抱歉，您访问的页面不存在或已被移除。
      </p>
      
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <router-link 
          to="/" 
          class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-3 px-6 rounded-lg transition-all glow-button"
        >
          返回首页
        </router-link>
        
        <button
          @click="goBack"
          class="bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-6 rounded-lg transition-colors"
        >
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}
</script>