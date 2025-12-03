<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <div class="text-6xl mb-4">🚧</div>
      <h1 class="text-2xl font-bold text-gray-300 mb-2">功能开发中</h1>
      <p class="text-gray-400 mb-6">应用编辑功能正在开发中</p>
      <router-link 
        :to="`/app/chat/${$route.params.id}`" 
        class="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-semibold py-2 px-4 rounded-lg transition-all"
      >
        返回对话
      </router-link>
    </div>
  </div>
</template>