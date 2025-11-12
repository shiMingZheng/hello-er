import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo.username || '',
    role: (state) => state.userInfo.role || ''
  },

  actions: {
    /**
     * 登录
     */
    async login(loginForm) {
      try {
        const data = await loginApi(loginForm)
        
        // 先存储到 localStorage（同步操作）
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
        
        // 再更新 Pinia 状态
        this.token = data.token
        this.userInfo = data.userInfo
        
        ElMessage.success('登录成功')
        
        // 使用 nextTick 确保状态更新完成后再跳转
        await new Promise(resolve => setTimeout(resolve, 100))
        
        // 使用 replace 而不是 push，避免返回到登录页
        router.replace('/')
      } catch (error) {
        console.error('登录失败：', error)
        throw error
      }
    },

    /**
     * 退出登录
     */
    logout() {
      // 先清除状态
      this.token = ''
      this.userInfo = {}
      
      // 再清除 localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      ElMessage.success('已退出登录')
      
      // 使用 replace 避免返回到需要登录的页面
      router.replace('/login')
    }
  }
})