import { defineStore } from 'pinia'
import { streamChat } from '@/api/chat'
import { listMessages } from '@/api/session'
import { useSessionStore } from '@/stores/session'

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [],
    streaming: false,
    error: '',
    controller: null
  }),
  actions: {
    /**
     * 加载指定会话的历史消息。
     * @param {number|null} sessionId 为空则清空消息区（新对话）
     */
    async load(sessionId) {
      this.error = ''
      this.messages = sessionId ? await listMessages(sessionId) : []
    },
    /**
     * 发送消息并流式接收回复。
     */
    async send(content, model) {
      const sessionStore = useSessionStore()
      if (!sessionStore.currentId) {
        await sessionStore.create({ model })
      }
      const sessionId = sessionStore.currentId

      this.error = ''
      this.messages.push({ role: 'user', content })
      const reply = { role: 'assistant', content: '', streaming: true, failed: false }
      this.messages.push(reply)

      this.streaming = true
      this.controller = new AbortController()

      await streamChat(
        { sessionId, content, model },
        {
          onDelta: (delta) => {
            reply.content += delta
          },
          onDone: async () => {
            reply.streaming = false
            this.streaming = false
            await sessionStore.refresh()
          },
          onError: (error) => {
            this.error = error.message || 'AI 服务调用失败'
            reply.streaming = false
            reply.failed = true
            this.streaming = false
            if (!reply.content) {
              reply.content = `⚠️ ${this.error}`
            }
          }
        },
        this.controller.signal
      )
    },
    stop() {
      this.controller?.abort()
      this.streaming = false
      const last = this.messages[this.messages.length - 1]
      if (last && last.streaming) {
        last.streaming = false
      }
    },
    reset() {
      this.messages = []
      this.error = ''
      this.streaming = false
    }
  }
})
