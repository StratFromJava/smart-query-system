import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/chat/:sessionId?',
    name: 'chat',
    component: () => import('@/views/ChatView.vue')
  },
  {
    path: '/knowledge',
    name: 'knowledge',
    component: () => import('@/views/KnowledgeView.vue')
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/PlaceholderView.vue'),
    props: { title: '联网搜索', desc: '联网检索能力已上线：在对话输入区的工具栏开启「联网搜索」开关即可。' }
  },
  {
    path: '/files',
    name: 'files',
    component: () => import('@/views/PlaceholderView.vue'),
    props: { title: '图片理解', desc: '图片理解已上线：切换到「通义千问 VL」模型，在输入区上传图片即可。' }
  },
  { path: '/', redirect: '/chat' },
  { path: '/:pathMatch(.*)*', redirect: '/chat' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.isLogin) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.public && userStore.isLogin && to.name !== 'register') {
    return { name: 'chat' }
  }
  return true
})

export default router
