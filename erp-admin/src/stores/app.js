import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false, // 侧边栏是否折叠
    currentPage: '首页'       // 当前页面标题
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    setCurrentPage(title) {
      this.currentPage = title
    }
  }
})
