import { defineStore } from 'pinia'
import { listModels, listFeatures } from '@/api/meta'

const THEME_KEY = 'sqs_theme'

export const useAppStore = defineStore('app', {
  state: () => ({
    theme: localStorage.getItem(THEME_KEY) || 'light',
    sidebarCollapsed: false,
    featurePanelVisible: false,
    models: [],
    features: [],
    defaultModel: 'qwen-plus',
    currentModel: 'qwen-plus'
  }),
  getters: {
    isDark: (state) => state.theme === 'dark',
    modelOptions: (state) => state.models.map((m) => ({ value: m.code, label: m.label }))
  },
  actions: {
    initTheme() {
      document.documentElement.classList.toggle('dark', this.isDark)
    },
    toggleTheme() {
      this.theme = this.isDark ? 'light' : 'dark'
      localStorage.setItem(THEME_KEY, this.theme)
      document.documentElement.classList.toggle('dark', this.isDark)
    },
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setFeaturePanel(visible) {
      this.featurePanelVisible = visible
    },
    async loadMeta() {
      try {
        const [models, features] = await Promise.all([listModels(), listFeatures()])
        this.models = models || []
        this.features = features || []
        if (this.models.length) {
          this.defaultModel = this.models[0].code
          if (!this.models.some((m) => m.code === this.currentModel)) {
            this.currentModel = this.defaultModel
          }
        }
      } catch {
        // 元数据加载失败不阻塞主流程
      }
    }
  }
})
