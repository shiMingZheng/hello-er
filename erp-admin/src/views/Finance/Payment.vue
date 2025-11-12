<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>收款录入</h2>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 简单核销 -->
        <el-tab-pane label="简单核销" name="simple">
          <el-form 
            ref="simpleFormRef"
            :model="simpleForm" 
            :rules="simpleRules"
            label-width="120px"
            style="max-width: 600px;"
          >
            <el-form-item label="选择客户" prop="customerId">
              <el-select 
                v-model="simpleForm.customerId" 
                placeholder="请选择客户" 
                filterable
                @change="handleCustomerChange"
                style="width: 100%;"
              >
                <el-option
                  v-for="customer in customers"
                  :key="customer.id"
                  :label="customer.name"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="选择应收" prop="receivableId">
              <el-select 
                v-model="simpleForm.receivableId" 
                placeholder="请先选择客户" 
                :disabled="!simpleForm.customerId"
                @change="handleReceivableChange"
                style="width: 100%;"
              >
                <el-option
                  v-for="item in customerReceivables"
                  :key="item.id"
                  :label="`${item.orderNo} - 未收: ¥${item.unpaidAmount?.toFixed(2)}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="未收金额">
              <span style="font-size: 18px; font-weight: bold; color: #f56c6c;">
                ¥{{ selectedReceivable?.unpaidAmount?.toFixed(2) || '0.00' }}
              </span>
            </el-form-item>

            <el-form-item label="收款金额" prop="amount">
              <el-input-number 
                v-model="simpleForm.amount" 
                :min="0" 
                :max="selectedReceivable?.unpaidAmount || 0"
                :precision="2" 
                :step="100"
                controls-position="right"
                style="width: 100%;" 
              />
            </el-form-item>

            <el-form-item label="支付方式">
              <el-select v-model="simpleForm.paymentMethod" placeholder="请选择" style="width: 100%;">
                <el-option label="现金" value="现金" />
                <el-option label="转账" value="转账" />
                <el-option label="支票" value="支票" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>

            <el-form-item label="收款日期">
              <el-date-picker 
                v-model="simpleForm.paymentDate" 
                type="date" 
                placeholder="选择日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item label="备注">
              <el-input 
                v-model="simpleForm.remark" 
                type="textarea" 
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSimpleSubmit">
                <el-icon><Check /></el-icon>
                提交收款
              </el-button>
              <el-button @click="handleSimpleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 批量核销 -->
        <el-tab-pane label="批量核销（推荐）" name="batch">
          <el-form 
            ref="batchFormRef"
            :model="batchForm" 
            :rules="batchRules"
            label-width="120px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="选择客户" prop="customerId">
                  <el-select 
                    v-model="batchForm.customerId" 
                    placeholder="请选择客户" 
                    filterable
                    @change="handleBatchCustomerChange"
                    style="width: 100%;"
                  >
                    <el-option
                      v-for="customer in customers"
                      :key="customer.id"
                      :label="customer.name"
                      :value="customer.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="收款总额" prop="totalAmount">
                  <el-input-number 
                    v-model="batchForm.totalAmount" 
                    :min="0" 
                    :precision="2" 
                    :step="100"
                    controls-position="right"
                    style="width: 100%;" 
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="支付方式">
                  <el-select v-model="batchForm.paymentMethod" placeholder="请选择" style="width: 100%;">
                    <el-option label="现金" value="现金" />
                    <el-option label="转账" value="转账" />
                    <el-option label="支票" value="支票" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="收款日期">
                  <el-date-picker 
                    v-model="batchForm.paymentDate" 
                    type="date" 
                    placeholder="选择日期"
                    style="width: 100%;"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注">
              <el-input 
                v-model="batchForm.remark" 
                type="textarea" 
                :rows="2"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-divider>核销分配</el-divider>

            <!-- 应收账款列表 -->
            <el-table 
              v-if="batchForm.customerId"
              :data="batchReceivables" 
              border
              max-height="400"
            >
              <el-table-column prop="orderNo" label="订单编号" width="180" />
              <el-table-column prop="unpaidAmount" label="未收金额" width="120" align="right">
                <template #default="{ row }">
                  ¥{{ row.unpaidAmount?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="本次核销金额" width="200">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.allocateAmount" 
                    :min="0" 
                    :max="row.unpaidAmount"
                    :precision="2"
                    size="small"
                    controls-position="right"
                    style="width: 100%;"
                    @change="handleAllocateChange"
                  />
                </template>
              </el-table-column>
              <el-table-column label="快捷操作" width="150">
                <template #default="{ row }">
                  <el-button 
                    type="primary" 
                    size="small" 
                    link
                    @click="handleFullAllocate(row)"
                  >
                    全额核销
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    link
                    @click="handleClearAllocate(row)"
                  >
                    清除
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>

            <!-- 核销汇总 -->
            <div v-if="batchForm.customerId" style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-statistic title="收款总额" :value="batchForm.totalAmount" precision="2" prefix="¥" />
                </el-col>
                <el-col :span="8">
                  <el-statistic title="已分配金额" :value="allocatedTotal" precision="2" prefix="¥">
                    <template #suffix>
                      <span :style="{ color: allocatedTotal > batchForm.totalAmount ? '#f56c6c' : '#67c23a' }">
                        元
                      </span>
                    </template>
                  </el-statistic>
                </el-col>
                <el-col :span="8">
                  <el-statistic title="未分配金额" :value="unallocatedAmount" precision="2" prefix="¥">
                    <template #suffix>
                      <span :style="{ color: unallocatedAmount < 0 ? '#f56c6c' : '#909399' }">
                        元
                      </span>
                    </template>
                  </el-statistic>
                </el-col>
              </el-row>
            </div>

            <el-form-item style="margin-top: 20px;">
              <el-button type="primary" :loading="submitLoading" @click="handleBatchSubmit">
                <el-icon><Check /></el-icon>
                提交批量核销
              </el-button>
              <el-button @click="handleBatchReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Refresh } from '@element-plus/icons-vue'
import { 
  recordPayment, 
  recordBatchPayment,
  getCustomerReceivables
} from '@/api/finance'
import { getCustomerList } from '@/api/customer'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

// 当前标签页
const activeTab = ref('simple')

// 表单引用
const simpleFormRef = ref(null)
const batchFormRef = ref(null)

// 提交loading
const submitLoading = ref(false)

// 客户列表
const customers = ref([])

// 客户应收列表
const customerReceivables = ref([])
const batchReceivables = ref([])

// 选中的应收
const selectedReceivable = ref(null)

// ============ 简单核销 ============
const simpleForm = reactive({
  customerId: null,
  receivableId: null,
  amount: 0,
  paymentMethod: '转账',
  paymentDate: new Date().toISOString().split('T')[0],
  remark: ''
})

const simpleRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  receivableId: [
    { required: true, message: '请选择应收账款', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入收款金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '收款金额必须大于0', trigger: 'blur' }
  ]
}

// ============ 批量核销 ============
const batchForm = reactive({
  customerId: null,
  totalAmount: 0,
  paymentMethod: '转账',
  paymentDate: new Date().toISOString().split('T')[0],
  remark: ''
})

const batchRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  totalAmount: [
    { required: true, message: '请输入收款总额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '收款总额必须大于0', trigger: 'blur' }
  ]
}

// 已分配金额
const allocatedTotal = computed(() => {
  return batchReceivables.value.reduce((sum, item) => sum + (item.allocateAmount || 0), 0)
})

// 未分配金额
const unallocatedAmount = computed(() => {
  return batchForm.totalAmount - allocatedTotal.value
})

// 加载客户列表
const loadCustomers = async () => {
  try {
    const res = await getCustomerList({ page: 0, size: 1000 })
    customers.value = res.content || []
    
    // 如果有查询参数，自动填充
    if (route.query.customerId) {
      simpleForm.customerId = parseInt(route.query.customerId)
      await loadCustomerReceivables(simpleForm.customerId)
      
      if (route.query.receivableId) {
        simpleForm.receivableId = parseInt(route.query.receivableId)
        handleReceivableChange()
        
        if (route.query.unpaidAmount) {
          simpleForm.amount = parseFloat(route.query.unpaidAmount)
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载客户列表失败: ' + error.message)
  }
}

// 加载客户应收列表
const loadCustomerReceivables = async (customerId) => {
  try {
    const res = await getCustomerReceivables(customerId)
    const unpaidList = res.filter(item => item.status !== 'PAID')
    
    if (activeTab.value === 'simple') {
      customerReceivables.value = unpaidList
    } else {
      batchReceivables.value = unpaidList.map(item => ({
        ...item,
        allocateAmount: 0
      }))
    }
  } catch (error) {
    ElMessage.error('加载应收列表失败: ' + error.message)
  }
}

// 切换标签页
const handleTabChange = () => {
  handleSimpleReset()
  handleBatchReset()
}

// ============ 简单核销方法 ============
const handleCustomerChange = async () => {
  simpleForm.receivableId = null
  selectedReceivable.value = null
  customerReceivables.value = []
  
  if (simpleForm.customerId) {
    await loadCustomerReceivables(simpleForm.customerId)
  }
}

const handleReceivableChange = () => {
  selectedReceivable.value = customerReceivables.value.find(
    item => item.id === simpleForm.receivableId
  )
  if (selectedReceivable.value) {
    simpleForm.amount = selectedReceivable.value.unpaidAmount
  }
}

const handleSimpleSubmit = async () => {
  try {
    await simpleFormRef.value.validate()
    
    submitLoading.value = true
    await recordPayment(simpleForm)
    ElMessage.success('收款录入成功')
    router.push('/finance/receivable')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleSimpleReset = () => {
  simpleFormRef.value?.resetFields()
  selectedReceivable.value = null
  customerReceivables.value = []
}

// ============ 批量核销方法 ============
const handleBatchCustomerChange = async () => {
  batchReceivables.value = []
  
  if (batchForm.customerId) {
    await loadCustomerReceivables(batchForm.customerId)
  }
}

const handleFullAllocate = (row) => {
  row.allocateAmount = row.unpaidAmount
}

const handleClearAllocate = (row) => {
  row.allocateAmount = 0
}

const handleAllocateChange = () => {
  // 实时计算
}

const handleBatchSubmit = async () => {
  try {
    await batchFormRef.value.validate()
    
    // 验证分配金额
    if (allocatedTotal.value === 0) {
      ElMessage.warning('请至少为一笔应收分配核销金额')
      return
    }
    
    if (Math.abs(allocatedTotal.value - batchForm.totalAmount) > 0.01) {
      ElMessage.warning('已分配金额与收款总额不一致，请调整')
      return
    }
    
    // 构建核销明细
    const allocations = batchReceivables.value
      .filter(item => item.allocateAmount > 0)
      .map(item => ({
        receivableId: item.id,
        amount: item.allocateAmount
      }))
    
    submitLoading.value = true
    await recordBatchPayment({
      customerId: batchForm.customerId,
      totalAmount: batchForm.totalAmount,
      paymentMethod: batchForm.paymentMethod,
      remark: batchForm.remark,
      allocations
    })
    
    ElMessage.success('批量核销成功')
    router.push('/finance/receivable')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleBatchReset = () => {
  batchFormRef.value?.resetFields()
  batchReceivables.value = []
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
</style>
