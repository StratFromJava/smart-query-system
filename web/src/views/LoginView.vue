<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const remember = ref(true)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功，欢迎回来')
    router.replace(route.query.redirect || '/chat')
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-view">
    <!-- 左侧品牌区 -->
    <section class="brand">
      <div class="brand__glow brand__glow--one" />
      <div class="brand__glow brand__glow--two" />
      <div class="brand__content">
        <div class="brand__logo">
          <span class="brand__mark">SQ</span>
          <span class="brand__name">SmartQuery</span>
        </div>
        <h1 class="brand__title">智能问答，<br />洞察每一条数据</h1>
        <p class="brand__desc">
          基于 Spring AI Alibaba 通义千问打造，用自然语言与你的系统对话。
        </p>
        <ul class="brand__points">
          <li>流式响应，逐字呈现思考过程</li>
          <li>多模型自由切换，按需取用</li>
          <li>会话云端留存，随时继续追问</li>
        </ul>
      </div>
    </section>

    <!-- 右侧表单区 -->
    <section class="form-side">
      <div class="form-card">
        <h2 class="form-card__title">欢迎回来</h2>
        <p class="form-card__subtitle">登录后继续你的智能对话</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-card__row">
            <el-checkbox v-model="remember">记住我</el-checkbox>
          </div>

          <el-button
            class="form-card__submit"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <p class="form-card__footer">
          还没有账号？
          <router-link to="/register" class="form-card__link">立即注册</router-link>
        </p>
        <p class="form-card__hint">默认账号：admin / admin123（执行 sql/schema.sql 后可用）</p>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
@use '../styles/variables.scss' as v;

.login-view {
  display: flex;
  height: 100%;
  background: var(--sqs-bg);
}

/* ---------- 品牌区 ---------- */
.brand {
  position: relative;
  flex: 1.1;
  overflow: hidden;
  background: v.$gradient-brand;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 8%;

  &__glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.55;
    animation: float 14s ease-in-out infinite;
  }

  &__glow--one {
    width: 420px;
    height: 420px;
    background: rgba(255, 255, 255, 0.35);
    top: -120px;
    left: -80px;
  }

  &__glow--two {
    width: 360px;
    height: 360px;
    background: rgba(61, 55, 214, 0.9);
    bottom: -140px;
    right: -60px;
    animation-delay: -6s;
  }

  &__content {
    position: relative;
    z-index: 1;
    max-width: 460px;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 48px;
  }

  &__mark {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.22);
    backdrop-filter: blur(6px);
    display: grid;
    place-items: center;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.5px;
  }

  &__name {
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 0.6px;
  }

  &__title {
    font-size: 34px;
    line-height: 1.35;
    font-weight: 600;
    margin-bottom: 16px;
  }

  &__desc {
    font-size: 15px;
    line-height: 1.8;
    opacity: 0.85;
    margin: 0 0 28px;
  }

  &__points {
    list-style: none;
    padding: 0;
    margin: 0;
    display: grid;
    gap: 10px;
    font-size: 14px;
  }

  &__points li {
    display: flex;
    align-items: center;
    gap: 10px;
    opacity: 0.9;
  }

  &__points li::before {
    content: '';
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
  }
}

/* ---------- 表单区 ---------- */
.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--sqs-bg-subtle);
}

.form-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 36px 32px;
  border-radius: v.$radius-xl;
  background: var(--sqs-bg);
  border: 1px solid var(--sqs-border);
  box-shadow: var(--sqs-shadow-lg);

  &__title {
    font-size: 26px;
    font-weight: 600;
    color: var(--sqs-text);
  }

  &__subtitle {
    margin: 8px 0 28px;
    font-size: 14px;
    color: var(--sqs-text-secondary);
  }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
  }

  &__submit {
    width: 100%;
    margin-top: 12px;
    height: 44px;
    font-size: 15px;
    letter-spacing: 4px;
    border-radius: v.$radius-md;
    box-shadow: var(--sqs-shadow-brand);
  }

  &__footer {
    margin: 20px 0 0;
    text-align: center;
    font-size: 14px;
    color: var(--sqs-text-secondary);
  }

  &__link {
    color: var(--sqs-primary);
    font-weight: 500;

    &:hover {
      color: var(--sqs-primary-deep);
    }
  }

  &__hint {
    margin: 14px 0 0;
    text-align: center;
    font-size: 12px;
    color: var(--sqs-text-muted);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }
  50% {
    transform: translate3d(24px, -28px, 0);
  }
}

@media (max-width: 900px) {
  .brand {
    display: none;
  }

  .form-side {
    padding: 24px;
  }
}
</style>
