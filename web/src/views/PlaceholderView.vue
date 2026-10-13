<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: { type: String, default: '功能建设中' },
  desc: { type: String, default: '该能力正在开发中，敬请期待。' }
})

const router = useRouter()
</script>

<template>
  <div class="placeholder-view">
    <header class="placeholder-view__header">
      <button class="back-btn" type="button" @click="router.push('/chat')">
        <el-icon><ArrowLeft /></el-icon>
        返回对话
      </button>
    </header>

    <div class="placeholder-view__body">
      <h1 class="placeholder-view__title sqs-gradient-text">{{ title }}</h1>
      <div class="placeholder-card sqs-glass">
        <div class="placeholder-card__icon">
          <el-icon><Tools /></el-icon>
        </div>
        <p class="placeholder-card__desc">{{ desc }}</p>
        <el-tag type="info" size="small" round effect="plain">敬请期待</el-tag>
        <el-button class="placeholder-card__btn" type="primary" round @click="router.push('/chat')">
          返回对话
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '../styles/variables.scss' as v;

.placeholder-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--sqs-bg-subtle);

  &__header {
    flex: 0 0 auto;
    height: v.$header-height;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-bottom: 1px solid var(--sqs-border);
    background: var(--sqs-bg);
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 20px;
    padding: 24px;
  }

  &__title {
    font-size: 26px;
    font-weight: 600;
  }
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--sqs-border);
  border-radius: v.$radius-pill;
  background: transparent;
  color: var(--sqs-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all v.$duration-fast v.$ease-standard;

  &:hover {
    color: var(--sqs-primary);
    border-color: var(--sqs-primary);
  }
}

.placeholder-card {
  width: 100%;
  max-width: 420px;
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  text-align: center;
  border: 1px solid var(--sqs-border);
  border-radius: v.$radius-xl;
  box-shadow: var(--sqs-shadow-lg);

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: v.$radius-lg;
    background: var(--sqs-gradient-soft);
    color: var(--sqs-primary);
    display: grid;
    place-items: center;
    font-size: 26px;
  }

  &__desc {
    margin: 0;
    font-size: 14px;
    line-height: 1.8;
    color: var(--sqs-text-secondary);
  }

  &__btn {
    margin-top: 6px;
    padding: 0 28px;
    box-shadow: var(--sqs-shadow-brand);
  }
}
</style>
