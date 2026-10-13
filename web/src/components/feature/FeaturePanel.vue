<script setup>
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

/** 未实现的能力跳转到占位页 */
const routeMap = {
  rag: '/knowledge',
  web_search: '/search',
  multimodal: '/files'
}

function handleOpen(code) {
  const path = routeMap[code]
  if (!path) return
  appStore.setFeaturePanel(false)
  router.push(path)
}
</script>

<template>
  <el-drawer v-model="appStore.featurePanelVisible" title="扩展能力" size="380px">
    <div class="feature-panel">
      <p class="feature-panel__intro">以下为规划中的扩展能力，已预留后端接入点与前端入口。</p>

      <div
        v-for="feature in appStore.features"
        :key="feature.code"
        class="feature-card"
        :class="{ 'feature-card--ready': feature.implemented }"
        @click="handleOpen(feature.code)"
      >
        <div class="feature-card__icon">
          <el-icon><component :is="feature.code === 'rag' ? 'Document' : feature.code === 'web_search' ? 'Link' : 'Picture'" /></el-icon>
        </div>
        <div class="feature-card__body">
          <div class="feature-card__head">
            <span class="feature-card__name">{{ feature.name }}</span>
            <el-tag :type="feature.implemented ? 'success' : 'info'" size="small" round effect="plain">
              {{ feature.implemented ? '已上线' : '即将开放' }}
            </el-tag>
          </div>
          <p class="feature-card__desc">{{ feature.desc }}</p>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.feature-panel {
  &__intro {
    margin: 0 0 16px;
    font-size: 13px;
    color: var(--sqs-text-secondary);
    line-height: 1.7;
  }
}

.feature-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid var(--sqs-border);
  border-radius: v.$radius-lg;
  cursor: pointer;
  transition: all v.$duration-base v.$ease-standard;

  &:hover {
    transform: translateY(-2px);
    border-color: var(--sqs-primary);
    box-shadow: var(--sqs-shadow-md);
  }

  &__icon {
    flex: 0 0 auto;
    width: 38px;
    height: 38px;
    border-radius: v.$radius-md;
    background: var(--sqs-gradient-soft);
    color: var(--sqs-primary);
    display: grid;
    place-items: center;
    font-size: 18px;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: var(--sqs-text);
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
    color: var(--sqs-text-secondary);
  }
}
</style>
