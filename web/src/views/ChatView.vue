<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChatHeader from '@/components/layout/ChatHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import MessageList from '@/components/chat/MessageList.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import FeaturePanel from '@/components/feature/FeaturePanel.vue'
import { useAppStore } from '@/stores/app'
import { useSessionStore } from '@/stores/session'
import { useChatStore } from '@/stores/chat'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const sessionStore = useSessionStore()
const chatStore = useChatStore()

const inputRef = ref()
const messageListRef = ref()

/** 路由中的会话 ID 变化 → 重新加载历史消息 */
watch(
  () => route.params.sessionId,
  async (value) => {
    const sessionId = value ? Number(value) : null
    sessionStore.select(sessionId)
    await chatStore.load(sessionId)
  },
  { immediate: true }
)

onMounted(async () => {
  await appStore.loadMeta()
  await sessionStore.refresh()
})

async function handleSend(content) {
  await chatStore.send(content, appStore.currentModel)
  // 首次发言会自动创建会话，同步到地址栏
  if (sessionStore.currentId && Number(route.params.sessionId) !== sessionStore.currentId) {
    router.replace(`/chat/${sessionStore.currentId}`)
  }
}

function handleNewChat() {
  chatStore.reset()
  sessionStore.select(null)
  if (route.params.sessionId) {
    router.push('/chat')
  }
}

function handleSelect(sessionId) {
  router.push(`/chat/${sessionId}`)
}

function handleSuggest(text) {
  inputRef.value?.fillText(text)
}

/**
 * 重新生成：截断本地尾部消息后重发最近一条提问。
 * 注意这是 UI 层行为，服务端仍会追加一条新的回答记录。
 */
function handleRegenerate(index) {
  const messages = chatStore.messages
  let question = ''
  for (let i = index - 1; i >= 0; i--) {
    if (messages[i].role === 'user') {
      question = messages[i].content
      break
    }
  }
  if (!question) return
  messages.splice(index)
  chatStore.send(question, appStore.currentModel)
}
</script>

<template>
  <div class="chat-view">
    <ChatHeader />

    <div class="chat-view__body">
      <AppSidebar
        :sessions="sessionStore.sessions"
        :current-id="sessionStore.currentId"
        :collapsed="appStore.sidebarCollapsed"
        @new-chat="handleNewChat"
        @select="handleSelect"
        @rename="sessionStore.rename"
        @remove="sessionStore.remove"
        @search="sessionStore.search"
      />

      <div
        v-if="!appStore.sidebarCollapsed"
        class="chat-view__mask"
        @click="appStore.toggleSidebar()"
      />

      <main class="chat-view__main">
        <MessageList
          ref="messageListRef"
          :messages="chatStore.messages"
          :streaming="chatStore.streaming"
          @regenerate="handleRegenerate"
          @suggest="handleSuggest"
        />
        <ChatInput
          ref="inputRef"
          :streaming="chatStore.streaming"
          @send="handleSend"
          @stop="chatStore.stop"
        />
      </main>
    </div>

    <FeaturePanel />
  </div>
</template>

<style scoped lang="scss">
@use '../styles/variables.scss' as v;

.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--sqs-bg-subtle);

  &__body {
    flex: 1;
    display: flex;
    min-height: 0;
    position: relative;
  }

  &__mask {
    display: none;
  }

  &__main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    background: var(--sqs-bg-subtle);
  }
}

@media (max-width: 900px) {
  .chat-view__mask {
    display: block;
    position: absolute;
    inset: 0;
    z-index: 15;
    background: rgba(15, 16, 20, 0.35);
    backdrop-filter: blur(2px);
  }
}
</style>
