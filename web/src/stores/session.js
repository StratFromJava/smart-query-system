import { defineStore } from 'pinia'
import { listSessions, createSession, renameSession, removeSession } from '@/api/session'

export const useSessionStore = defineStore('session', {
  state: () => ({
    sessions: [],
    currentId: null,
    keyword: ''
  }),
  actions: {
    async refresh() {
      this.sessions = await listSessions(this.keyword ? this.keyword.trim() : undefined)
    },
    async create(payload) {
      const session = await createSession(payload)
      this.currentId = session.id
      await this.refresh()
      return session
    },
    select(sessionId) {
      this.currentId = sessionId
    },
    async rename(sessionId, title) {
      await renameSession(sessionId, title)
      await this.refresh()
    },
    async remove(sessionId) {
      await removeSession(sessionId)
      if (this.currentId === sessionId) {
        this.currentId = null
      }
      await this.refresh()
    },
    async search(keyword) {
      this.keyword = keyword
      await this.refresh()
    }
  }
})
