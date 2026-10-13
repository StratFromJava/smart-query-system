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
    component: () => import('@/views/PlaceholderView.vue'),
    props: { title: '知识库', desc: '上传文档构建专属知识库，让回答基于你的私有资料。' }
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/PlaceholderView.vue'),
    props: { title: '联网搜索', desc: '实时检索互联网信息，让回答具备最新时效性。' }
  },
  {
    path: '/files',
    name: 'files',
    component: () => import('@/views/PlaceholderView.vue'),
    props: { title: '文件与图片理解', desc: '上传图片或文档，让模型看懂内容并给出分析。' }
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
