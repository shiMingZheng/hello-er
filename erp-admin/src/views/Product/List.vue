<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">商品列表</h2>
      <div>
        <el-button type="warning" @click="loadLowStockProducts">
          <el-icon><Warning /></el-icon>
          库存预警
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称/编码">
          <el-input
            ref="scanInputRef"
            v-model="searchForm.keyword"
            placeholder="请输入商品名称或扫码"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            autofocus
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
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
        :data="products" 
        border 
        stripe
        @row-dblclick="handleEdit"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="code" label="商品编码" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.code" type="info">{{ row.code }}</el-tag>
            <span v-else style="color: #ccc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="normalPrice" label="普通价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.normalPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="vipPrice" label="VIP 价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.vipPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="right">
          <template #default="{ row }">
            <el-tag v-if="row.isLowStock" type="danger">
              {{ row.stock }} ⚠️
            </el-tag>
            <span v-else>{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockWarning" label="预警阈值" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">已上架</el-tag>
            <el-tag v-else type="info">已下架</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" size="small" link @click="handleAdjustStock(row)">
              <el-icon><Box /></el-icon>
              调整库存
            </el-button>
            <el-button 
              v-if="row.status === 1"
              type="warning" 
              size="small" 
              link 
              @click="handleToggleStatus(row)"
            >
              <el-icon><Bottom /></el-icon>
              下架
            </el-button>
            <el-button 
              v-else
              type="success" 
              size="small" 
              link 
              @click="handleToggleStatus(row)"
            >
              <el-icon><Top /></el-icon>
              上架
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

    <!-- 调整库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ stockForm.productName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span style="font-size: 18px; font-weight: bold; color: #409eff;">
            {{ stockForm.currentStock }}
          </span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number 
            v-model="stockForm.quantity" 
            :min="-stockForm.currentStock"
            :step="1"
            controls-position="right"
            style="width: 100%;"
          />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            正数为增加库存，负数为减少库存
          </div>
        </el-form-item>
        <el-form-item label="调整后库存">
          <span style="font-size: 16px; color: #67c23a;">
            {{ stockForm.currentStock + stockForm.quantity }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="handleConfirmAdjustStock">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, Edit, Box, Bottom, Top, Warning 
} from '@element-plus/icons-vue'
import { 
  getProductList, 
  deleteProduct, 
  getLowStockProducts,
  adjustStock
} from '@/api/product'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null
})

// 扫码枪输入框引用
const scanInputRef = ref(null)

// 表格数据
const products = ref([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 库存调整对话框
const stockDialogVisible = ref(false)
const stockLoading = ref(false)
const stockForm = reactive({
  id: null,
  productName: '',
  currentStock: 0,
  quantity: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status
    }
    const res = await getProductList(params)
    products.value = res.content || []
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
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
  // 重新聚焦到输入框，方便扫码
  nextTick(() => {
    scanInputRef.value?.focus()
  })
}

// 加载库存预警商品
const loadLowStockProducts = async () => {
  try {
    const res = await getLowStockProducts()
    if (res.length === 0) {
      ElMessage.success('暂无库存预警商品')
      return
    }
    products.value = res
    total.value = res.length
    currentPage.value = 1
    ElMessage.warning(`发现 ${res.length} 个商品库存不足`)
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
  }
}

// 新增
const handleAdd = () => {
  router.push('/product/form')
}

// 编辑
const handleEdit = (row) => {
  router.push(`/product/form/${row.id}`)
}

// 上架/下架
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(
      `确定要${action}商品【${row.name}】吗?`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里使用删除接口实现下架（根据后端实际情况调整）
    if (row.status === 1) {
      await deleteProduct(row.id)
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.warning('上架功能需要后端支持状态修改接口')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败: ` + error.message)
    }
  }
}

// 打开调整库存对话框
const handleAdjustStock = (row) => {
  stockForm.id = row.id
  stockForm.productName = row.name
  stockForm.currentStock = row.stock
  stockForm.quantity = 0
  stockDialogVisible.value = true
}

// 确认调整库存
const handleConfirmAdjustStock = async () => {
  if (stockForm.quantity === 0) {
    ElMessage.warning('请输入调整数量')
    return
  }
  
  stockLoading.value = true
  try {
    await adjustStock(stockForm.id, stockForm.quantity)
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('调整失败: ' + error.message)
  } finally {
    stockLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadData()
  // 自动聚焦到输入框，方便扫码
  nextTick(() => {
    scanInputRef.value?.focus()
  })
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
