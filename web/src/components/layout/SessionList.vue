<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  sessions: { type: Array, default: () => [] },
  currentId: { type: [Number, String], default: null }
})

const emit = defineEmits(['select', 'rename', 'remove'])

/** 按「今天 / 更早」分组 */
const groups = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const todayList = []
  const earlierList = []

  for (const session of props.sessions) {
    const time = new Date(session.updateTime || session.createTime)
    if (!isNaN(time) && time >= today) {
      todayList.push(session)
    } else {
      earlierList.push(session)
    }
  }
  return [
    { key: 'today', label: '今天', items: todayList },
    { key: 'earlier', label: '更早', items: earlierList }
  ].filter((group) => group.items.length)
})

async function handleRename(session) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的会话标题', '重命名', {
      inputValue: session.title,
      inputValidator: (v) => (v && v.trim().length > 0 ? true : '标题不能为空')
    })
    emit('rename', session.id, value.trim())
  } catch {
    // 取消
  }
}

async function handleRemove(session) {
  try {
    await ElMessageBox.confirm(`删除后该会话的消息将不可恢复，确认删除「${session.title}」？`, '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  emit('remove', session.id)
  ElMessage.success('会话已删除')
}
</script>

<template>
  <div class="session-list">
    <el-empty v-if="!sessions.length" description="暂无会话" :image-size="70" />

    <div v-for="group in groups" :key="group.key" class="session-group">
      <p class="session-group__label">{{ group.label }}</p>
      <button
        v-for="session in group.items"
        :key="session.id"
        class="session-item"
        :class="{ 'session-item--active': session.id === currentId }"
        type="button"
        @click="emit('select', session.id)"
      >
        <el-icon class="session-item__icon"><ChatDotSquare /></el-icon>
        <span class="session-item__title">{{ session.title }}</span>
        <span class="session-item__actions">
          <el-icon title="重命名" @click.stop="handleRename(session)"><EditPen /></el-icon>
          <el-icon title="删除" @click.stop="handleRemove(session)"><Delete /></el-icon>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.session-group {
  margin-bottom: 10px;

  &__label {
    margin: 12px 8px 6px;
    font-size: 12px;
    color: var(--sqs-text-muted);
  }
}

.session-item {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border: none;
  border-radius: v.$radius-md;
  background: transparent;
  color: var(--sqs-text-secondary);
  cursor: pointer;
  text-align: left;
  transition: all v.$duration-fast v.$ease-standard;

  &:hover {
    background: var(--sqs-bg-hover);
    color: var(--sqs-text);
  }

  &--active {
    background: var(--sqs-bg-active);
    color: var(--sqs-text);
    font-weight: 500;
  }

  &--active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 18px;
    border-radius: v.$radius-pill;
    background: v.$gradient-brand;
  }

  &__icon {
    flex: 0 0 auto;
    font-size: 15px;
  }

  &__title {
    flex: 1;
    min-width: 0;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity v.$duration-fast v.$ease-standard;
    color: var(--sqs-text-muted);

    .el-icon:hover {
      color: var(--sqs-primary);
    }
  }

  &:hover &__actions {
    opacity: 1;
  }
}
</style>
