<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2>订单详情</h2>
          <div>
            <el-button @click="$router.back()">
              <el-icon><Back /></el-icon>
              返回
            </el-button>
            <el-button 
              v-if="order.status === 'PENDING'" 
              type="success" 
              @click="handleApprove"
            >
              <el-icon><Select /></el-icon>
              审核订单
            </el-button>
            <el-button 
              v-if="order.status === 'APPROVED'" 
              type="warning" 
              @click="handleShip"
            >
              <el-icon><Van /></el-icon>
              订单发货
            </el-button>
            <el-button 
              v-if="order.status === 'SHIPPED'" 
              type="primary" 
              @click="handleComplete"
            >
              <el-icon><CircleCheck /></el-icon>
              完成订单
            </el-button>
            <el-button type="primary" @click="handlePrint">
              <el-icon><Printer /></el-icon>
              打印订单
            </el-button>
          </div>
        </div>
      </template>

      <!-- 订单基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">
          <el-tag type="info">{{ order.orderNo }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">
          {{ order.customerName }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.status)">
            {{ getStatusText(order.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(order.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额" :span="2">
          <span style="font-size: 20px; font-weight: bold; color: #f56c6c;">
            ¥{{ order.totalAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <span v-if="order.remark">{{ order.remark }}</span>
          <span v-else style="color: #ccc;">-</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>订单明细</el-divider>

      <!-- 订单明细表格 -->
      <el-table :data="order.items" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="price" label="单价" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column prop="subtotal" label="小计" width="120" align="right">
          <template #default="{ row }">
            <span style="font-weight: bold;">
              ¥{{ row.subtotal?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 合计 -->
      <div style="margin-top: 20px; text-align: right; font-size: 16px;">
        <span style="margin-right: 10px;">订单总额：</span>
        <span style="font-size: 24px; font-weight: bold; color: #f56c6c;">
          ¥{{ order.totalAmount?.toFixed(2) }}
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Back, Select, Van, CircleCheck, Printer 
} from '@element-plus/icons-vue'
import { 
  getOrderDetail, 
  approveOrder, 
  shipOrder, 
  completeOrder 
} from '@/api/order'
import { formatDateTime } from '@/utils/format'
import { printOrder } from '@/utils/print'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref({
  orderNo: '',
  customerName: '',
  status: '',
  totalAmount: 0,
  remark: '',
  createTime: '',
  items: []
})

// 加载订单详情
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
    router.back()
  } finally {
    loading.value = false
  }
}

// 审核订单
const handleApprove = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要审核此订单吗？',
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await approveOrder(route.params.id)
    ElMessage.success('审核成功')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败: ' + error.message)
    }
  }
}

// 订单发货
const handleShip = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要发货此订单吗？',
      '发货确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await shipOrder(route.params.id)
    ElMessage.success('发货成功')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发货失败: ' + error.message)
    }
  }
}

// 完成订单
const handleComplete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要完成此订单吗？',
      '完成确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await completeOrder(route.params.id)
    ElMessage.success('订单已完成')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

// 打印订单
const handlePrint = () => {
  try {
    printOrder(order.value)
    ElMessage.success('正在打印...')
  } catch (error) {
    ElMessage.error('打印失败: ' + error.message)
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已审核',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 初始化
onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
