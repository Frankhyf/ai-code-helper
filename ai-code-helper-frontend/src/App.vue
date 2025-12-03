<template>
  <div id="app" class="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900">
    <!-- 导航栏 -->
    <NavBar v-if="showNavBar" />
    
    <!-- 主内容区域 -->
    <main :class="{ 'pb-16': showTabBar }">
      <router-view v-slot="{ Component }">
        <transition name="page-transition" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <!-- 底部导航 -->
    <TabBar v-if="showTabBar" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/layout/NavBar.vue'
import TabBar from '@/components/layout/TabBar.vue'

const route = useRoute()
const userStore = useUserStore()

// 计算是否显示导航栏
const showNavBar = computed(() => {
  const hiddenRoutes = ['Login', 'Register']
  return !hiddenRoutes.includes(route.name as string)
})

// 计算是否显示底部导航
const showTabBar = computed(() => {
  const showRoutes = ['Home', 'AppList', 'Examples']
  return showRoutes.includes(route.name as string)
})

// 初始化用户状态
onMounted(() => {
  userStore.initUser()
})
</script>

<style scoped>
.page-transition-enter-active,
.page-transition-leave-active {
  transition: all 0.3s ease;
}

.page-transition-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.page-transition-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>