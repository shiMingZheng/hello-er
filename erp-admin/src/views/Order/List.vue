<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">订单列表</h2>
    </div>

    <!-- 搜索区域 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单编号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单编号"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已审核" value="APPROVED" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
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
      <el-table 
        v-loading="loading" 
        :data="orders" 
        border 
        stripe
        @row-dblclick="viewDetail"
      >
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">
              ¥{{ row.totalAmount?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.remark">{{ row.remark }}</span>
            <span v-else style="color: #ccc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'"
              type="success" 
              size="small" 
              link 
              @click="handleApprove(row)"
            >
              <el-icon><Select /></el-icon>
              审核
            </el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Select } from '@element-plus/icons-vue'
import { getOrderList, approveOrder } from '@/api/order'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: null
})

// 表格数据
const orders = ref([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status || undefined
    }
    const res = await getOrderList(params)
    orders.value = res.content || []
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
  searchForm.orderNo = ''
  searchForm.status = null
  handleSearch()
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/order/detail/${row.id}`)
}

// 审核订单
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要审核订单【${row.orderNo}】吗?`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await approveOrder(row.id)
    ElMessage.success('审核成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败: ' + error.message)
    }
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
