<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>ERP 管理系统 - 登录</h2>
      </template>
      
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tip">
        <p>测试账号：</p>
        <p>用户名：admin</p>
        <p>密码：admin123</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authStore.login(loginForm.value)
    } catch (error) {
      console.error('登录失败', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.login-card :deep(.el-card__header) {
  text-align: center;
  background-color: #f5f7fa;
}

.login-card h2 {
  margin: 0;
  color: #303133;
}

.login-tip {
  margin-top: 20px;
  padding: 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}

.login-tip p {
  margin: 4px 0;
}
</style>
