<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  streaming: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  /** 可用能力清单（FeatureType），用于渲染开关 */
  features: { type: Array, default: () => [] },
  /** 已开启的能力 code 集合 */
  activeFeatures: { type: Array, default: () => [] },
  /** 当前模型是否支持图片理解 */
  multimodal: { type: Boolean, default: false }
})

const emit = defineEmits(['send', 'stop', 'toggle-feature'])

const textareaRef = ref()
const text = ref('')
const attachments = ref([])
const uploading = ref(false)
const MAX_HEIGHT = 200

const canSend = computed(() => text.value.trim().length > 0 && !props.streaming && !props.disabled)

/** 待发送图片缩略图列表 */
const attachmentPreviews = computed(() =>
  attachments.value.map((url) => ({ url, preview: `${url}` }))
)

function resize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, MAX_HEIGHT)}px`
}

watch(text, () => nextTick(resize))

async function handleImageUpload(options) {
  uploading.value = true
  try {
    const { url } = await uploadImage(options.file)
    attachments.value.push(url)
    ElMessage.success('图片已上传')
  } catch {
    // 错误由响应拦截器提示
  } finally {
    uploading.value = false
  }
}

function removeAttachment(url) {
  attachments.value = attachments.value.filter((item) => item !== url)
}

function handleSend() {
  if (!canSend.value) return
  const content = text.value.trim()
  text.value = ''
  nextTick(resize)
  emit('send', content, {
    features: props.activeFeatures,
    attachments: attachments.value
  })
  attachments.value = []
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
      <div v-if="attachmentPreviews.length" class="chat-input__attachments">
        <div v-for="item in attachmentPreviews" :key="item.url" class="chat-input__attachment">
          <img :src="item.preview" alt="待发送图片" />
          <button
            class="chat-input__attachment-remove"
            type="button"
            title="移除图片"
            @click="removeAttachment(item.url)"
          >
            ×
          </button>
        </div>
      </div>

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
        <div class="chat-input__tools">
          <el-upload
            class="chat-input__upload"
            accept="image/png,image/jpeg,image/gif,image/webp,image/bmp"
            :show-file-list="false"
            :http-request="handleImageUpload"
            :disabled="!multimodal || streaming || disabled"
          >
            <button
              class="chat-input__tool"
              type="button"
              :class="{ 'chat-input__tool--disabled': !multimodal }"
              :title="multimodal ? '上传图片' : '切换到 qwen-vl 模型后可上传图片'"
              :disabled="!multimodal || streaming || disabled"
            >
              <el-icon><Picture /></el-icon>
            </button>
          </el-upload>

          <button
            v-for="feature in features"
            :key="feature.code"
            class="chat-input__tool"
            :class="{ 'chat-input__tool--active': activeFeatures.includes(feature.code) }"
            type="button"
            :title="feature.name"
            @click="emit('toggle-feature', feature.code)"
          >
            <el-icon><component :is="feature.code === 'rag' ? 'Document' : 'Link'" /></el-icon>
            <span>{{ feature.name }}</span>
          </button>

          <span v-if="uploading" class="chat-input__uploading">图片上传中…</span>
        </div>

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

  &__tools {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
    flex-wrap: wrap;
  }

  &__tool {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 28px;
    padding: 0 8px;
    border: 1px solid var(--sqs-border);
    border-radius: v.$radius-md;
    background: transparent;
    color: var(--sqs-text-secondary);
    font-size: 12px;
    cursor: pointer;
    transition: all v.$duration-fast v.$ease-standard;

    &:hover:not(&--disabled) {
      border-color: var(--sqs-primary);
      color: var(--sqs-primary);
    }

    &--active {
      border-color: var(--sqs-primary);
      color: var(--sqs-primary);
      background: var(--sqs-gradient-soft);
    }

    &--disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }
  }

  &__uploading {
    font-size: 12px;
    color: var(--sqs-text-muted);
  }

  &__attachments {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__attachment {
    position: relative;
    width: 72px;
    height: 72px;
    border-radius: v.$radius-md;
    overflow: hidden;
    border: 1px solid var(--sqs-border);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__attachment-remove {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    display: grid;
    place-items: center;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 12px;
    line-height: 1;
    cursor: pointer;

    &:hover {
      background: rgba(0, 0, 0, 0.75);
    }
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
