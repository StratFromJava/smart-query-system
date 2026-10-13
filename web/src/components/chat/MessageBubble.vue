<script setup>
import { computed } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'

const props = defineProps({
  message: { type: Object, required: true },
  isLast: { type: Boolean, default: false },
  streaming: { type: Boolean, default: false }
})

const emit = defineEmits(['regenerate'])

const isUser = computed(() => props.message.role === 'user')
/** 等待首个 token 时的呼吸动画 */
const waiting = computed(() => props.isLast && props.streaming && !props.message.content)
</script>

<template>
  <div class="bubble-wrap" :class="isUser ? 'bubble-wrap--user' : 'bubble-wrap--ai'">
    <!-- AI 头像 -->
    <div v-if="!isUser" class="avatar avatar--ai">SQ</div>

    <div class="bubble-col">
      <div class="bubble" :class="[isUser ? 'bubble--user' : 'bubble--ai', { 'bubble--failed': message.failed }]">
        <div v-if="waiting" class="sqs-typing">
          <span /><span /><span />
        </div>

        <MarkdownRenderer v-else-if="!isUser" :content="message.content" />

        <span v-else-if="message.streaming && !message.content" class="sqs-caret" />
        <span v-else class="bubble__plain">{{ message.content }}</span>
      </div>

      <div v-if="!isUser && !streaming && message.content" class="bubble-actions">
        <el-tooltip content="重新生成" placement="bottom">
          <button type="button" @click="emit('regenerate')">
            <el-icon><RefreshRight /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- 用户头像 -->
    <div v-if="isUser" class="avatar avatar--user">我</div>
  </div>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.bubble-wrap {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 6px 0;

  &--user {
    flex-direction: row-reverse;
  }
}

.avatar {
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  user-select: none;

  &--ai {
    background: v.$gradient-brand;
    box-shadow: var(--sqs-shadow-brand);
    letter-spacing: 0.5px;
  }

  &--user {
    background: var(--sqs-text-secondary);
  }
}

.bubble-col {
  max-width: calc(100% - 90px);
  min-width: 0;
}

.bubble {
  position: relative;
  padding: 12px 16px;
  border-radius: v.$radius-lg;
  font-size: 15px;
  line-height: 1.7;
  word-break: break-word;

  &--ai {
    background: var(--sqs-bg);
    border: 1px solid var(--sqs-border);
    border-top-left-radius: v.$radius-sm;
    box-shadow: var(--sqs-shadow-sm);
    color: var(--sqs-text);
  }

  &--user {
    background: v.$gradient-brand;
    color: #fff;
    border-top-right-radius: v.$radius-sm;
    box-shadow: var(--sqs-shadow-brand);
  }

  &--failed {
    border-color: rgba(229, 69, 69, 0.4);
    background: rgba(229, 69, 69, 0.06);
  }

  &__plain {
    white-space: pre-wrap;
  }
}

.bubble-actions {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  opacity: 0;
  transition: opacity v.$duration-fast v.$ease-standard;

  button {
    width: 26px;
    height: 26px;
    display: grid;
    place-items: center;
    border: none;
    border-radius: v.$radius-sm;
    background: transparent;
    color: var(--sqs-text-muted);
    cursor: pointer;
    transition: all v.$duration-fast v.$ease-standard;

    &:hover {
      background: var(--sqs-bg-hover);
      color: var(--sqs-primary);
    }
  }
}

.bubble-wrap:hover .bubble-actions {
  opacity: 1;
}

/* 流式输出时保持光标可见 */
.sqs-caret {
  display: inline-block;
}
</style>
