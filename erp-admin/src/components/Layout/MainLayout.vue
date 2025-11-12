<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon class="menu-toggle" @click="toggleSidebar">
          <Fold v-if="!appStore.sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
        <h2>ERP 管理系统</h2>
      </div>
      <div class="header-right">
        <span class="username">{{ authStore.username }}</span>
        <el-button type="danger" size="small" @click="authStore.logout">
          退出登录
        </el-button>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="appStore.sidebarCollapsed ? '64px' : '200px'" class="layout-aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <!-- 首页 -->
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
		  <!-- 客户管理 -->
		  <el-sub-menu index="customer">
		  <template #title>
		  	<el-icon><User /></el-icon>
		  	<span>客户管理</span>
		  </template>
		  <el-menu-item index="/customer/list">客户列表</el-menu-item>
		  </el-sub-menu>

          <!-- 商品管理 -->
          <el-sub-menu index="product">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/product/list">商品列表</el-menu-item>
            <el-menu-item index="/product/form">新增商品</el-menu-item>
          </el-sub-menu>

          <!-- 订单管理 -->
          <el-sub-menu index="order">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/order/list">订单列表</el-menu-item>
          </el-sub-menu>

          <!-- 财务管理 -->
          <el-sub-menu index="finance">
            <template #title>
              <el-icon><Wallet /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item index="/finance/receivable">应收账款</el-menu-item>
            <el-menu-item index="/finance/payment">收款录入</el-menu-item>
            <el-menu-item index="/finance/statement">对账单</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import {
  Fold,
  Expand,
  HomeFilled,
  ShoppingCart,
  Document,
  Wallet,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  appStore.toggleSidebar()
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #409eff;
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.menu-toggle {
  font-size: 22px;
  cursor: pointer;
  transition: transform 0.3s;
}

.menu-toggle:hover {
  transform: scale(1.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
}

.layout-aside {
  background-color: #fff;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.05);
  overflow-x: hidden;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.layout-main {
  background-color: #f0f2f5;
  overflow-y: auto;
  padding: 20px;
}

/* 菜单项样式优化 */
.el-menu-item,
.el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}

.el-menu-item:hover,
.el-sub-menu__title:hover {
  background-color: #ecf5ff !important;
}

.el-menu-item.is-active {
  background-color: #e6f7ff !important;
  color: #409eff !important;
  font-weight: 600;
}
</style>
