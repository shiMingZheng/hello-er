<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">应收账款</h2>
    </div>

    <!-- 搜索区域 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="客户名称">
          <el-select
            v-model="searchForm.customerId"
            placeholder="请选择客户"
            clearable
            filterable
            @change="handleSearch"
            style="width: 200px;"
          >
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收款状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="未收款" value="UNPAID" />
            <el-option label="部分收款" value="PARTIAL" />
            <el-option label="已收款" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <!-- 统计信息 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="应收总额" :value="statistics.totalAmount" precision="2" prefix="¥" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已收金额" :value="statistics.paidAmount" precision="2" prefix="¥" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="未收金额" :value="statistics.unpaidAmount" precision="2" prefix="¥">
            <template #suffix>
              <span style="color: #f56c6c; font-weight: bold;">元</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="待收笔数" :value="statistics.unpaidCount" suffix="笔" />
        </el-col>
      </el-row>

      <el-divider />

      <el-table 
        v-loading="loading" 
        :data="receivables" 
        border 
        stripe
      >
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="amount" label="应收金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已收金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #67c23a;">
              ¥{{ row.paidAmount?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unpaidAmount" label="未收金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">
              ¥{{ row.unpaidAmount?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="收款状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status !== 'PAID'" 
              type="primary" 
              size="small"
              @click="handlePayment(row)"
            >
              <el-icon><Money /></el-icon>
              收款
            </el-button>
            <span v-else style="color: #999;">已结清</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Money } from '@element-plus/icons-vue'
import { getReceivableList } from '@/api/finance'
import { getCustomerList } from '@/api/customer'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  customerId: null,
  status: null
})

// 客户列表
const customers = ref([])

// 表格数据
const receivables = ref([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 统计信息
const statistics = computed(() => {
  const totalAmount = receivables.value.reduce((sum, item) => sum + (item.amount || 0), 0)
  const paidAmount = receivables.value.reduce((sum, item) => sum + (item.paidAmount || 0), 0)
  const unpaidAmount = receivables.value.reduce((sum, item) => sum + (item.unpaidAmount || 0), 0)
  const unpaidCount = receivables.value.filter(item => item.status !== 'PAID').length
  
  return {
    totalAmount,
    paidAmount,
    unpaidAmount,
    unpaidCount
  }
})

// 加载客户列表
const loadCustomers = async () => {
  try {
    // 获取所有客户（不分页）
    const res = await getCustomerList({ page: 0, size: 1000 })
    customers.value = res.content || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      customerId: searchForm.customerId || undefined,
      status: searchForm.status || undefined
    }
    const res = await getReceivableList(params)
    receivables.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.customerId = null
  searchForm.status = null
  handleSearch()
}

// 收款
const handlePayment = (row) => {
  // 跳转到收款录入页面，并传递应收账款信息
  router.push({
    path: '/finance/payment',
    query: {
      receivableId: row.id,
      customerId: row.customerId,
      customerName: row.customerName,
      unpaidAmount: row.unpaidAmount
    }
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'UNPAID': 'danger',
    'PARTIAL': 'warning',
    'PAID': 'success'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'UNPAID': '未收款',
    'PARTIAL': '部分收款',
    'PAID': '已收款'
  }
  return map[status] || status
}

// 初始化
onMounted(() => {
  loadCustomers()
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
</style>
