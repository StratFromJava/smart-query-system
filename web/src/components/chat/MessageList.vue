<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  streaming: { type: Boolean, default: false }
})

const emit = defineEmits(['regenerate', 'suggest'])

const scrollRef = ref()
const stickToBottom = ref(true)
const showBackToLatest = ref(false)

const suggestions = [
  { icon: 'EditPen', title: '帮我写一段介绍', desc: '为 SmartQuery 智能问答系统写一段产品简介' },
  { icon: 'DataAnalysis', title: '分析这段代码', desc: '解释 Spring AI Alibaba 中 ChatClient 的作用' },
  { icon: 'Lightbulb', title: '给我一些建议', desc: '如何设计一个高并发的会话存储方案' }
]

function scrollToBottom(smooth = true) {
  const el = scrollRef.value
  if (!el) return
  el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' })
}

function handleScroll() {
  const el = scrollRef.value
  if (!el) return
  const distance = el.scrollHeight - el.scrollTop - el.clientHeight
  stickToBottom.value = distance < 80
  showBackToLatest.value = distance > 240
}

/** 流式内容增长时自动跟随底部（用户手动上滚时暂停） */
watch(
  () => props.messages.map((m) => m.content).join('').length,
  async () => {
    if (!stickToBottom.value) return
    await nextTick()
    scrollToBottom(false)
  }
)

watch(
  () => props.messages.length,
  async () => {
    stickToBottom.value = true
    showBackToLatest.value = false
    await nextTick()
    scrollToBottom()
  }
)

onMounted(() => {
  nextTick(() => scrollToBottom(false))
})

defineExpose({ scrollToBottom })
</script>

<template>
  <div class="message-list" ref="scrollRef" @scroll.passive="handleScroll">
    <!-- 空状态 -->
    <div v-if="!messages.length" class="empty">
      <h1 class="empty__title sqs-gradient-text">今天想聊点什么？</h1>
      <p class="empty__desc">向 SmartQuery 提问，体验流畅的智能问答</p>
      <div class="empty__cards">
        <button
          v-for="item in suggestions"
          :key="item.title"
          class="empty__card"
          type="button"
          @click="emit('suggest', item.desc)"
        >
          <el-icon class="empty__icon"><component :is="item.icon" /></el-icon>
          <span class="empty__card-title">{{ item.title }}</span>
          <span class="empty__card-desc">{{ item.desc }}</span>
        </button>
      </div>
    </div>

    <!-- 消息流 -->
    <div v-else class="message-list__inner">
      <transition-group name="fade-slide">
        <MessageBubble
          v-for="(message, index) in messages"
          :key="index"
          :message="message"
          :is-last="index === messages.length - 1"
          :streaming="streaming"
          @regenerate="emit('regenerate', index)"
        />
      </transition-group>
    </div>

    <transition name="fade-slide">
      <button v-if="showBackToLatest" class="back-latest" type="button" @click="scrollToBottom()">
        <el-icon><ArrowDown /></el-icon>
        回到最新
      </button>
    </transition>
  </div>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.message-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px 20px 32px;
  scroll-behavior: smooth;

  &__inner {
    max-width: v.$content-max-width;
    margin: 0 auto;
  }
}

/* ---------- 空状态 ---------- */
.empty {
  height: 100%;
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  &__title {
    font-size: 30px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  &__desc {
    margin: 0 0 32px;
    font-size: 15px;
    color: var(--sqs-text-secondary);
  }

  &__cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 14px;
    width: 100%;
    max-width: 760px;
  }

  &__card {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 16px;
    text-align: left;
    border: 1px solid var(--sqs-border);
    border-radius: v.$radius-lg;
    background: var(--sqs-bg);
    color: var(--sqs-text);
    cursor: pointer;
    transition: all v.$duration-base v.$ease-standard;

    &:hover {
      transform: translateY(-3px);
      border-color: var(--sqs-primary);
      box-shadow: var(--sqs-shadow-md);
    }
  }

  &__icon {
    font-size: 18px;
    color: var(--sqs-primary);
    margin-bottom: 2px;
  }

  &__card-title {
    font-size: 14px;
    font-weight: 600;
  }

  &__card-desc {
    font-size: 13px;
    color: var(--sqs-text-secondary);
    line-height: 1.6;
  }
}

/* ---------- 回到最新 ---------- */
.back-latest {
  position: sticky;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid var(--sqs-border);
  border-radius: v.$radius-pill;
  background: var(--sqs-glass);
  backdrop-filter: blur(12px);
  color: var(--sqs-text-secondary);
  font-size: 13px;
  cursor: pointer;
  box-shadow: var(--sqs-shadow-md);

  &:hover {
    color: var(--sqs-primary);
    border-color: var(--sqs-primary);
  }
}
</style>
