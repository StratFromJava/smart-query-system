import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, fetchMe } from '@/api/auth'

const TOKEN_KEY = 'sqs_token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    profile: null
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    displayName: (state) => state.profile?.nickname || state.profile?.username || '我',
    avatarText: (state) => (state.profile?.nickname || state.profile?.username || 'U').slice(0, 1).toUpperCase()
  },
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.applyAuth(data)
      return data
    },
    async register(payload) {
      const data = await registerApi(payload)
      this.applyAuth(data)
      return data
    },
    async loadProfile() {
      if (!this.token) return null
      this.profile = await fetchMe()
      return this.profile
    },
    applyAuth(data) {
      this.token = data.token
      this.profile = data.user
      localStorage.setItem(TOKEN_KEY, data.token)
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
