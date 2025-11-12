<template>
  <div class="home-container">
    <el-card>
      <template #header>
        <h2>欢迎使用 ERP 管理系统</h2>
      </template>
      
      <div class="welcome-content">
        <el-result
          icon="success"
          title="系统集成完成！"
          sub-title="所有核心功能已接入后端真实接口"
        >
          <template #extra>
            <div class="user-info">
              <p><strong>当前用户：</strong>{{ authStore.username }}</p>
              <p><strong>用户角色：</strong>{{ authStore.role }}</p>
              <p><strong>Token 状态：</strong>
                <el-tag type="success">已认证</el-tag>
              </p>
            </div>
            
            <div class="actions">
              <el-button type="primary" @click="router.push('/customer/list')">
                <el-icon><User /></el-icon>
                客户管理
              </el-button>
              <el-button type="primary" @click="router.push('/product/list')">
                <el-icon><ShoppingCart /></el-icon>
                商品管理
              </el-button>
              <el-button type="primary" @click="router.push('/order/list')">
                <el-icon><Document /></el-icon>
                订单管理
              </el-button>
              <el-button type="primary" @click="router.push('/finance/receivable')">
                <el-icon><Wallet /></el-icon>
                财务管理
              </el-button>
            </div>
          </template>
        </el-result>
        
        <el-divider />
        
        <div class="feature-info">
          <h3>✅ 已完成的功能模块：</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <h4>🏢 客户管理</h4>
                <ul>
                  <li>客户列表（分页、筛选）</li>
                  <li>新增/编辑客户</li>
                  <li>客户等级管理（普通/VIP）</li>
                  <li>信用额度管理</li>
                  <li>启用/禁用客户</li>
                </ul>
              </el-card>
            </el-col>
            
            <el-col :span="12">
              <el-card shadow="hover">
                <h4>📦 商品管理</h4>
                <ul>
                  <li>商品列表（分页、搜索）</li>
                  <li>新增/编辑商品</li>
                  <li>扫码枪支持（商品编码）</li>
                  <li>库存预警功能</li>
                  <li>手动调整库存</li>
                  <li>上架/下架管理</li>
                </ul>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <el-card shadow="hover">
                <h4>📋 订单管理</h4>
                <ul>
                  <li>订单列表（分页、状态筛选）</li>
                  <li>订单详情查看</li>
                  <li>订单审核流程</li>
                  <li>订单发货/完成</li>
                  <li>订单打印功能</li>
                </ul>
              </el-card>
            </el-col>
            
            <el-col :span="12">
              <el-card shadow="hover">
                <h4>💰 财务管理</h4>
                <ul>
                  <li>应收账款列表（客户筛选）</li>
                  <li>收款录入（简单核销）</li>
                  <li>批量核销（一笔收款核销多个订单）</li>
                  <li>月度对账单生成</li>
                  <li>对账单打印功能</li>
                </ul>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <el-divider />
        
        <div class="tips">
          <h3>💡 使用提示：</h3>
          <el-alert
            title="扫码枪使用"
            description="在商品列表和商品表单中，输入框已支持扫码枪。扫码后会自动触发搜索或填入编码。"
            type="info"
            :closable="false"
            style="margin-bottom: 10px;"
          />
          <el-alert
            title="打印功能"
            description="订单详情和对账单页面支持打印功能。点击打印按钮会打开新窗口并自动调用浏览器打印。"
            type="success"
            :closable="false"
            style="margin-bottom: 10px;"
          />
          <el-alert
            title="批量核销"
            description="收款录入支持批量核销，可以一笔收款同时核销多个订单，非常适合客户一次性结清多笔欠款的场景。"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, ShoppingCart, Document, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-content {
  padding: 20px;
}

.user-info {
  margin-bottom: 20px;
  line-height: 2;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.feature-info {
  margin-top: 20px;
}

.feature-info h3 {
  color: #303133;
  margin-bottom: 15px;
}

.feature-info h4 {
  color: #409eff;
  margin-bottom: 10px;
}

.feature-info ul {
  list-style: none;
  padding-left: 0;
  line-height: 1.8;
  color: #606266;
}

.feature-info ul li {
  padding: 3px 0;
}

.feature-info ul li:before {
  content: "✓ ";
  color: #67c23a;
  font-weight: bold;
  margin-right: 5px;
}

.tips {
  margin-top: 20px;
}

.tips h3 {
  color: #303133;
  margin-bottom: 15px;
}
</style>
