<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import SessionList from './SessionList.vue'

const props = defineProps({
  sessions: { type: Array, default: () => [] },
  currentId: { type: [Number, String], default: null },
  collapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['new-chat', 'select', 'rename', 'remove', 'search'])

const keyword = ref('')

function handleSearch() {
  emit('search', keyword.value)
}
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
    <div class="sidebar__top">
      <button class="new-chat" type="button" @click="emit('new-chat')">
        <el-icon><Plus /></el-icon>
        <span v-if="!collapsed">新建对话</span>
      </button>
    </div>

    <div v-if="!collapsed" class="sidebar__search">
      <el-input
        v-model="keyword"
        placeholder="搜索会话"
        :prefix-icon="Search"
        clearable
        size="default"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
    </div>

    <SessionList
      v-if="!collapsed"
      :sessions="sessions"
      :current-id="currentId"
      @select="emit('select', $event)"
      @rename="(id, title) => emit('rename', id, title)"
      @remove="emit('remove', $event)"
    />

    <div v-if="!collapsed" class="sidebar__footer">
      <span>SmartQuery · v1.0</span>
    </div>
  </aside>
</template>

<style scoped lang="scss">
@use '../../styles/variables.scss' as v;

.sidebar {
  width: v.$sidebar-width;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  background: var(--sqs-bg);
  border-right: 1px solid var(--sqs-border);
  transition: width v.$duration-base v.$ease-standard;

  &--collapsed {
    width: v.$sidebar-width-collapsed;
    align-items: center;
  }

  &__top {
    display: flex;
    justify-content: center;
    padding: 0 4px 12px;
  }

  &__search {
    padding: 0 4px 10px;
  }

  &__footer {
    padding: 10px 12px 0;
    font-size: 12px;
    color: var(--sqs-text-muted);
    border-top: 1px solid var(--sqs-border);
    padding-top: 12px;
    margin-top: auto;
  }
}

.new-chat {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: v.$radius-md;
  background: v.$gradient-brand;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: var(--sqs-shadow-brand);
  transition: all v.$duration-base v.$ease-standard;

  &:hover {
    transform: translateY(-1px);
    filter: brightness(1.06);
  }
}

.sidebar--collapsed .new-chat {
  width: 40px;
  height: 40px;
  border-radius: v.$radius-md;
}

@media (max-width: 900px) {
  .sidebar {
    position: absolute;
    z-index: 20;
    height: 100%;
    box-shadow: var(--sqs-shadow-lg);
  }
}
</style>
