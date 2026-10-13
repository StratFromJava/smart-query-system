<script setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<template>
  <header class="chat-header sqs-glass">
    <div class="chat-header__left">
      <button class="icon-btn" type="button" :title="appStore.sidebarCollapsed ? '展开侧栏' : '收起侧栏'" @click="appStore.toggleSidebar()">
        <el-icon><component :is="appStore.sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
      </button>
      <div class="logo">
        <span class="logo__mark">SQ</span>
        <span class="logo__name">SmartQuery</span>
      </div>
    </div>

    <div class="chat-header__center">
      <el-select
        v-model="appStore.currentModel"
        class="model-select"
        size="default"
        placeholder="选择模型"
      >
        <el-option
          v-for="model in appStore.models"
          :key="model.code"
          :label="model.label"
          :value="model.code"
        />
      </el-select>
    </div>

    <div class="chat-header__right">
      <el-tooltip content="预留能力" placement="bottom">
        <button class="icon-btn" type="button" @click="appStore.setFeaturePanel(true)">
          <el-icon><MagicStick /></el-icon>
        </button>
      </el-tooltip>

      <el-tooltip :content="appStore.isDark ? '切换到浅色' : '切换到深色'" placement="bottom">
        <button class="icon-btn" type="button" @click="appStore.toggleTheme()">
          <el-icon><component :is="appStore.isDark ? 'Sunny' : 'Moon'" /></el-icon>
        </button>
      </el-tooltip>

      <el-dropdown trigger="click" @command="handleLogout">
        <button class="user-chip" type="button">
          <span class="user-chip__avatar">{{ userStore.avatarText }}</span>
          <span class="user-chip__name">{{ userStore.displayName }}</span>
          <el-icon class="user-chip__arrow"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.chat-header {
  position: relative;
  z-index: 10;
  height: v.$header-height;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid var(--sqs-border);

  &__left,
  &__right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__center {
    flex: 1;
    display: flex;
    justify-content: center;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 4px;

  &__mark {
    width: 26px;
    height: 26px;
    border-radius: 8px;
    background: v.$gradient-brand;
    color: #fff;
    display: grid;
    place-items: center;
    font-size: 12px;
    font-weight: 700;
    box-shadow: var(--sqs-shadow-brand);
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: var(--sqs-text);
    letter-spacing: 0.3px;
  }
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: v.$radius-sm;
  background: transparent;
  color: var(--sqs-text-secondary);
  cursor: pointer;
  transition: all v.$duration-fast v.$ease-standard;

  &:hover {
    background: var(--sqs-bg-hover);
    color: var(--sqs-primary);
  }
}

.model-select {
  width: 168px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border: 1px solid var(--sqs-border);
  border-radius: v.$radius-pill;
  background: transparent;
  cursor: pointer;
  transition: all v.$duration-fast v.$ease-standard;

  &:hover {
    border-color: var(--sqs-primary);
    background: var(--sqs-bg-hover);
  }

  &__avatar {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    background: v.$gradient-brand;
    color: #fff;
    display: grid;
    place-items: center;
    font-size: 12px;
    font-weight: 600;
  }

  &__name {
    font-size: 13px;
    color: var(--sqs-text);
    max-width: 90px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__arrow {
    font-size: 12px;
    color: var(--sqs-text-muted);
  }
}

@media (max-width: 900px) {
  .logo__name,
  .user-chip__name {
    display: none;
  }
}
</style>
