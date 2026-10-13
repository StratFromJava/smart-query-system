<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Postcard, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', nickname: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '用户名为 4-20 位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

/** 简易密码强度：长度 + 字符种类 */
const strength = computed(() => {
  const pwd = form.password
  if (!pwd) return { level: 0, text: '', color: 'transparent' }
  let score = 0
  if (pwd.length >= 6) score += 1
  if (pwd.length >= 10) score += 1
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 1
  if (/\d/.test(pwd) || /[^A-Za-z0-9]/.test(pwd)) score += 1
  const map = [
    { level: 1, text: '弱', color: '#e54545' },
    { level: 2, text: '中', color: '#f5a623' },
    { level: 3, text: '较强', color: '#5b5bf7' },
    { level: 4, text: '强', color: '#17b26a' }
  ]
  return map[Math.min(score, 4) - 1]
})

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功，已自动登录')
    router.replace('/chat')
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-view">
    <section class="brand">
      <div class="brand__glow brand__glow--one" />
      <div class="brand__glow brand__glow--two" />
      <div class="brand__content">
        <div class="brand__logo">
          <span class="brand__mark">SQ</span>
          <span class="brand__name">SmartQuery</span>
        </div>
        <h1 class="brand__title">创建账号，<br />开启智能问答之旅</h1>
        <p class="brand__desc">注册后即可拥有独立的会话空间，数据严格隔离。</p>
      </div>
    </section>

    <section class="form-side">
      <div class="form-card">
        <h2 class="form-card__title">创建账号</h2>
        <p class="form-card__subtitle">填写以下信息完成注册</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（4-20 位字母/数字/下划线）" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称（可选）" :prefix-icon="Postcard" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（至少 6 位）"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <div v-if="strength.level" class="strength">
            <div class="strength__bar">
              <span :style="{ width: `${strength.level * 25}%`, background: strength.color }" />
            </div>
            <span class="strength__text" :style="{ color: strength.color }">{{ strength.text }}</span>
          </div>
          <el-form-item prop="confirm">
            <el-input
              v-model="form.confirm"
              type="password"
              placeholder="确认密码"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-button
            class="form-card__submit"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form>

        <p class="form-card__footer">
          已有账号？
          <router-link to="/login" class="form-card__link">去登录</router-link>
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
@use '../styles/variables.scss' as v;

.register-view {
  display: flex;
  height: 100%;
  background: var(--sqs-bg);
}

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
    width: 400px;
    height: 400px;
    background: rgba(255, 255, 255, 0.32);
    top: -100px;
    right: -60px;
  }

  &__glow--two {
    width: 340px;
    height: 340px;
    background: rgba(61, 55, 214, 0.9);
    bottom: -120px;
    left: -40px;
    animation-delay: -6s;
  }

  &__content {
    position: relative;
    z-index: 1;
    max-width: 440px;
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
    display: grid;
    place-items: center;
    font-size: 14px;
    font-weight: 700;
  }

  &__name {
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 0.6px;
  }

  &__title {
    font-size: 32px;
    line-height: 1.35;
    font-weight: 600;
    margin-bottom: 16px;
  }

  &__desc {
    font-size: 15px;
    line-height: 1.8;
    opacity: 0.85;
    margin: 0;
  }
}

.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--sqs-bg-subtle);
  overflow-y: auto;
}

.form-card {
  width: 100%;
  max-width: 380px;
  padding: 36px;
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
    margin: 8px 0 24px;
    font-size: 14px;
    color: var(--sqs-text-secondary);
  }

  &__submit {
    width: 100%;
    margin-top: 8px;
    height: 44px;
    font-size: 15px;
    letter-spacing: 4px;
    border-radius: v.$radius-md;
    box-shadow: var(--sqs-shadow-brand);
  }

  &__footer {
    margin: 18px 0 0;
    text-align: center;
    font-size: 14px;
    color: var(--sqs-text-secondary);
  }

  &__link {
    color: var(--sqs-primary);
    font-weight: 500;
  }
}

.strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: -8px 0 12px;

  &__bar {
    flex: 1;
    height: 4px;
    border-radius: v.$radius-pill;
    background: var(--sqs-border);
    overflow: hidden;

    span {
      display: block;
      height: 100%;
      border-radius: v.$radius-pill;
      transition: width v.$duration-base v.$ease-standard;
    }
  }

  &__text {
    font-size: 12px;
    min-width: 28px;
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
