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
     * @param {object} extras 可选项 { features: string[], attachments: string[] }
     */
    async send(content, model, extras = {}) {
      const sessionStore = useSessionStore()
      if (!sessionStore.currentId) {
        await sessionStore.create({ model })
      }
      const sessionId = sessionStore.currentId

      // 乐观渲染：图片以 Markdown 语法拼接（与服务端持久化格式一致）
      const imageMarkdown = (extras.attachments || [])
        .map((url) => `\n\n![image](${url})`)
        .join('')
      this.error = ''
      this.messages.push({ role: 'user', content: content + imageMarkdown })
      const reply = { role: 'assistant', content: '', streaming: true, failed: false }
      this.messages.push(reply)

      this.streaming = true
      this.controller = new AbortController()

      await streamChat(
        {
          sessionId,
          content,
          model,
          features: extras.features && extras.features.length ? extras.features : undefined,
          attachments: extras.attachments && extras.attachments.length ? extras.attachments : undefined
        },
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
