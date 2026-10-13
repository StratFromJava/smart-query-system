<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  content: { type: String, default: '' }
})

const html = computed(() => renderMarkdown(props.content))

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(props.content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.warning('当前环境不支持自动复制')
  }
}
</script>

<template>
  <div class="md-render">
    <button class="md-render__copy" type="button" title="复制全文" @click="handleCopy">
      <el-icon><CopyDocument /></el-icon>
    </button>
    <!-- v-html 内容已由 markdown-it(html:false) 转义并经 DOMPurify 净化 -->
    <div class="markdown-body" v-html="html" />
  </div>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.md-render {
  position: relative;

  &__copy {
    position: absolute;
    top: -4px;
    right: -4px;
    width: 28px;
    height: 28px;
    display: grid;
    place-items: center;
    border: none;
    border-radius: v.$radius-sm;
    background: transparent;
    color: var(--sqs-text-muted);
    cursor: pointer;
    opacity: 0;
    transition: all v.$duration-fast v.$ease-standard;

    &:hover {
      background: var(--sqs-bg-hover);
      color: var(--sqs-primary);
    }
  }

  &:hover &__copy {
    opacity: 1;
  }
}
</style>
