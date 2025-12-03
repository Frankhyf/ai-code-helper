import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: 'AI应用生成平台',
      requiresAuth: false
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/auth/Profile.vue'),
    meta: {
      title: '个人信息',
      requiresAuth: true
    }
  },
  {
    path: '/app',
    name: 'App',
    redirect: '/app/list',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'list',
        name: 'AppList',
        component: () => import('@/views/app/List.vue'),
        meta: {
          title: '我的应用',
          requiresAuth: true
        }
      },
      {
        path: 'chat/:id',
        name: 'AppChat',
        component: () => import('@/views/app/Chat.vue'),
        meta: {
          title: '应用对话',
          requiresAuth: true
        }
      },
      {
        path: 'edit/:id',
        name: 'AppEdit',
        component: () => import('@/views/app/Edit.vue'),
        meta: {
          title: '编辑应用',
          requiresAuth: true
        }
      },
      {
        path: 'create',
        name: 'AppCreate',
        component: () => import('@/views/app/Create.vue'),
        meta: {
          title: '创建应用',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    redirect: '/admin/app',
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'app',
        name: 'AdminApp',
        component: () => import('@/views/admin/AppManage.vue'),
        meta: {
          title: '应用管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'chat',
        name: 'AdminChat',
        component: () => import('@/views/admin/ChatManage.vue'),
        meta: {
          title: '对话管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/examples',
    name: 'Examples',
    component: () => import('@/views/Examples.vue'),
    meta: {
      title: '精选案例',
      requiresAuth: false
    }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: {
      title: '关于我们',
      requiresAuth: false
    }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  }
]

// 创建路由器
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - AI应用生成平台`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLogin) {
    // 尝试从本地存储恢复登录状态
    userStore.initUser()
    
    if (!userStore.isLogin) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
    return
  }
  
  next()
})

export default router