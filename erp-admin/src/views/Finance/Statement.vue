<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2>客户对账单</h2>
          <el-button 
            type="primary" 
            :disabled="!statement.customerId"
            @click="handlePrint"
          >
            <el-icon><Printer /></el-icon>
            打印对账单
          </el-button>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="选择客户">
          <el-select 
            v-model="queryForm.customerId" 
            placeholder="请选择客户"
            filterable
            style="width: 250px;"
          >
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="对账月份">
          <el-date-picker 
            v-model="queryForm.month" 
            type="month" 
            placeholder="选择月份"
            value-format="YYYY-MM"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 对账单内容 -->
      <div v-if="statement.customerId" class="statement-content">
        <el-divider />

        <!-- 客户信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户名称">
            {{ statement.customerName }}
          </el-descriptions-item>
          <el-descriptions-item label="对账期间">
            {{ statement.startDate }} 至 {{ statement.endDate }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>对账汇总</el-divider>

        <!-- 对账汇总 -->
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="6">
            <el-statistic 
              title="期初余额" 
              :value="statement.openingBalance" 
              precision="2" 
              prefix="¥" 
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="本期销售" 
              :value="statement.periodSales" 
              precision="2" 
              prefix="¥" 
            >
              <template #suffix>
                <span style="color: #f56c6c;">元</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="本期收款" 
              :value="statement.periodPayments" 
              precision="2" 
              prefix="¥" 
            >
              <template #suffix>
                <span style="color: #67c23a;">元</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="期末余额" 
              :value="statement.closingBalance" 
              precision="2" 
              prefix="¥" 
            >
              <template #suffix>
                <span style="color: #409eff; font-weight: bold;">元</span>
              </template>
            </el-statistic>
          </el-col>
        </el-row>

        <el-divider>对账明细</el-divider>

        <!-- 对账明细 -->
        <el-table :data="statement.details" border max-height="500">
          <el-table-column prop="date" label="日期" width="120" align="center" />
          <el-table-column prop="type" label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.type === '销售'" type="danger">{{ row.type }}</el-tag>
              <el-tag v-else type="success">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="refNo" label="单号" width="180" />
          <el-table-column prop="debit" label="应收" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.debit > 0" style="color: #f56c6c;">
                ¥{{ row.debit.toFixed(2) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="credit" label="实收" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.credit > 0" style="color: #67c23a;">
                ¥{{ row.credit.toFixed(2) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="140" align="right">
            <template #default="{ row }">
              <span style="font-weight: bold;">
                ¥{{ row.balance.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.remark">{{ row.remark }}</span>
              <span v-else style="color: #ccc;">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-else 
        description="请选择客户和月份查询对账单"
        :image-size="200"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Printer } from '@element-plus/icons-vue'
import { generateMonthlyStatement } from '@/api/finance'
import { getCustomerList } from '@/api/customer'
import { printStatement } from '@/utils/print'

// 查询表单
const queryForm = reactive({
  customerId: null,
  month: new Date().toISOString().slice(0, 7) // 默认当前月份 YYYY-MM
})

// 客户列表
const customers = ref([])

// 对账单数据
const statement = ref({
  customerId: null,
  customerName: '',
  startDate: '',
  endDate: '',
  openingBalance: 0,
  periodSales: 0,
  periodPayments: 0,
  closingBalance: 0,
  details: []
})

const loading = ref(false)

// 加载客户列表
const loadCustomers = async () => {
  try {
    const res = await getCustomerList({ page: 0, size: 1000 })
    customers.value = res.content || []
  } catch (error) {
    ElMessage.error('加载客户列表失败: ' + error.message)
  }
}

// 查询对账单
const handleQuery = async () => {
  if (!queryForm.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!queryForm.month) {
    ElMessage.warning('请选择对账月份')
    return
  }
  
  loading.value = true
  try {
    const [year, month] = queryForm.month.split('-')
    const res = await generateMonthlyStatement(
      queryForm.customerId, 
      parseInt(year), 
      parseInt(month)
    )
    statement.value = res
    
    if (res.details.length === 0) {
      ElMessage.info('该月份暂无对账数据')
    } else {
      ElMessage.success('对账单生成成功')
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打印对账单
const handlePrint = () => {
  try {
    printStatement(statement.value)
    ElMessage.success('正在打印...')
  } catch (error) {
    ElMessage.error('打印失败: ' + error.message)
  }
}

// 初始化
onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.statement-content {
  margin-top: 20px;
}
</style>
