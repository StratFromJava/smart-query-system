<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDocuments, uploadDocument, removeDocument } from '@/api/knowledge'

const documents = ref([])
const loading = ref(false)
const uploading = ref(false)
const uploadRef = ref()

function formatSize(size) {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

async function load() {
  loading.value = true
  try {
    documents.value = await listDocuments()
  } catch {
    // 错误由响应拦截器提示
  } finally {
    loading.value = false
  }
}

async function handleUpload(options) {
  uploading.value = true
  try {
    const doc = await uploadDocument(options.file)
    ElMessage.success(`《${doc.title}》入库成功，共 ${doc.chunkCount} 个知识分块`)
    await load()
  } catch {
    // 错误由响应拦截器提示
  } finally {
    uploading.value = false
    uploadRef.value?.clearFiles()
  }
}

function handleExceed() {
  ElMessage.warning('每次仅上传一个文档')
}

async function handleRemove(doc) {
  try {
    await ElMessageBox.confirm(`确定删除《${doc.title}》吗？其知识分块将一并移除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await removeDocument(doc.id)
    ElMessage.success('文档已删除')
    await load()
  } catch {
    // 错误由响应拦截器提示
  }
}

onMounted(load)
</script>

<template>
  <div class="knowledge-view">
    <header class="knowledge-view__header">
      <h2>知识库</h2>
      <p>上传 txt / md / pdf 文档，对话时开启「知识库问答」即可基于私有资料精准作答。</p>
    </header>

    <div class="knowledge-view__body">
      <el-upload
        ref="uploadRef"
        class="knowledge-view__upload"
        drag
        accept=".txt,.md,.pdf"
        :limit="1"
        :http-request="handleUpload"
        :show-file-list="false"
        :on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文档到此处，或 <em>点击选择文件</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .txt / .md / .pdf，大小不超过 20MB；上传后自动分块并向量化（需配置 DashScope API Key）
          </div>
        </template>
      </el-upload>
      <div v-if="uploading" class="knowledge-view__progress">
        <el-icon class="is-loading"><Loading /></el-icon>
        文档解析与向量化中，请稍候…
      </div>

      <el-table v-loading="loading" :data="documents" class="knowledge-view__table">
        <el-table-column prop="title" label="文档" min-width="220" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ (row.fileType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100" align="center">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="知识分块" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'READY' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'" size="small">
              {{ row.status === 'READY' ? '已就绪' : row.status === 'FAILED' ? '失败' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有文档，上传第一份文档构建你的知识库吧" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '../styles/variables.scss' as v;

.knowledge-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--sqs-bg-subtle);

  &__header {
    padding: 20px 28px 8px;

    h2 {
      margin: 0 0 6px;
      font-size: 20px;
      color: var(--sqs-text);
    }

    p {
      margin: 0;
      font-size: 13px;
      color: var(--sqs-text-secondary);
    }
  }

  &__body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 12px 28px 28px;
  }

  &__upload {
    margin-bottom: 16px;
  }

  &__progress {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 13px;
    color: var(--sqs-primary);
  }

  &__table {
    width: 100%;
  }
}
</style>
