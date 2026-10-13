<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  streaming: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['send', 'stop'])

const textareaRef = ref()
const text = ref('')
const MAX_HEIGHT = 200

const canSend = computed(() => text.value.trim().length > 0 && !props.streaming && !props.disabled)

function resize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, MAX_HEIGHT)}px`
}

watch(text, () => nextTick(resize))

function handleSend() {
  if (!canSend.value) return
  const content = text.value.trim()
  text.value = ''
  nextTick(resize)
  emit('send', content)
}

function handleKeydown(event) {
  // Enter 发送，Shift+Enter 换行
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
    event.preventDefault()
    handleSend()
  }
}

function fillText(content) {
  text.value = content
  nextTick(() => {
    resize()
    textareaRef.value?.focus()
  })
}

defineExpose({ fillText })
</script>

<template>
  <div class="chat-input">
    <div class="chat-input__box">
      <textarea
        ref="textareaRef"
        v-model="text"
        class="chat-input__area"
        rows="1"
        :placeholder="streaming ? '正在生成中，可点击右侧停止…' : '给 SmartQuery 发送消息，Enter 发送 / Shift+Enter 换行'"
        :disabled="disabled"
        @keydown="handleKeydown"
      />

      <div class="chat-input__toolbar">
        <span class="chat-input__hint">Enter 发送 · Shift+Enter 换行</span>

        <button
          v-if="streaming"
          class="chat-input__btn chat-input__btn--stop"
          type="button"
          title="停止生成"
          @click="emit('stop')"
        >
          <span class="chat-input__stop-icon" />
        </button>
        <button
          v-else
          class="chat-input__btn chat-input__btn--send"
          type="button"
          :disabled="!canSend"
          title="发送"
          @click="handleSend"
        >
          <el-icon><Promotion /></el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.chat-input {
  padding: 8px 20px 20px;
  background: linear-gradient(to top, var(--sqs-bg-subtle) 60%, transparent);

  &__box {
    max-width: v.$content-max-width;
    margin: 0 auto;
    padding: 12px 14px 10px;
    border: 1px solid var(--sqs-border);
    border-radius: v.$radius-xl;
    background: var(--sqs-bg);
    box-shadow: var(--sqs-shadow-md);
    transition: all v.$duration-base v.$ease-standard;

    &:focus-within {
      border-color: var(--sqs-primary);
      box-shadow: 0 0 0 3px rgba(91, 91, 247, 0.12), var(--sqs-shadow-md);
    }
  }

  &__area {
    width: 100%;
    max-height: 200px;
    border: none;
    outline: none;
    resize: none;
    font-family: inherit;
    font-size: 15px;
    line-height: 1.7;
    color: var(--sqs-text);
    background: transparent;
    overflow-y: auto;

    &::placeholder {
      color: var(--sqs-text-muted);
    }
  }

  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 6px;
  }

  &__hint {
    font-size: 12px;
    color: var(--sqs-text-muted);
  }

  &__btn {
    width: 34px;
    height: 34px;
    display: grid;
    place-items: center;
    border: none;
    border-radius: v.$radius-md;
    cursor: pointer;
    transition: all v.$duration-fast v.$ease-standard;

    &--send {
      background: v.$gradient-brand;
      color: #fff;
      font-size: 17px;
      box-shadow: var(--sqs-shadow-brand);

      &:disabled {
        background: var(--sqs-border);
        color: var(--sqs-text-muted);
        box-shadow: none;
        cursor: not-allowed;
      }

      &:not(:disabled):hover {
        transform: translateY(-1px);
      }
    }

    &--stop {
      background: var(--sqs-text);
      color: var(--sqs-bg);
    }
  }

  &__stop-icon {
    width: 12px;
    height: 12px;
    border-radius: 2px;
    background: currentColor;
  }
}
</style>
