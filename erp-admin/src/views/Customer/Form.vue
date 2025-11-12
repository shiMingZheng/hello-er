<template>
  <div class="customer-form">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑客户' : '新增客户' }}</span>
          <el-button link @click="handleBack">
            <el-icon><Back /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="客户名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入客户名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contact">
          <el-input
            v-model="formData.contact"
            placeholder="请输入联系人姓名"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱地址"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="客户等级" prop="level">
          <el-radio-group v-model="formData.level">
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="VIP">VIP</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="信用额度" prop="creditLimit">
          <el-input-number
            v-model="formData.creditLimit"
            :min="0"
            :max="10000000"
            :precision="2"
            :step="1000"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">元</span>
        </el-form-item>

        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="formData.status">
            <el-radio value="ACTIVE">正常</el-radio>
            <el-radio value="INACTIVE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存' : '提交' }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Check, Refresh, Close } from '@element-plus/icons-vue'
import { getCustomerDetail, createCustomer, updateCustomer } from '@/api/customer'

const router = useRouter()
const route = useRoute()

// 是否编辑模式
const isEdit = computed(() => route.params.id && route.params.id !== 'add')

// 表单引用
const formRef = ref(null)

// 提交loading
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  level: 'NORMAL',
  creditLimit: 50000,
  status: 'ACTIVE',
  remark: ''
})

// 校验规则
const rules = {
  name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择客户等级', trigger: 'change' }
  ],
  creditLimit: [
    { required: true, message: '请输入信用额度', trigger: 'blur' },
    { type: 'number', min: 0, message: '信用额度不能为负数', trigger: 'blur' }
  ]
}

// 加载详情
const loadDetail = async () => {
  try {
    const res = await getCustomerDetail(route.params.id)
    Object.assign(formData, res)
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
    router.back()
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value.validate()
    
    submitLoading.value = true
    let res
    if (isEdit.value) {
      res = await updateCustomer(route.params.id, formData)
    } else {
      res = await createCustomer(formData)
    }

    
    ElMessage.success(isEdit.value ? '保存成功' : '新增成功')
    router.push('/customer/list')

  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      ElMessage.error('操作失败: ' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value.resetFields()
}

// 返回列表
const handleBack = () => {
  router.push('/customer/list')
}

// 初始化
onMounted(() => {
  if (isEdit.value) {
    loadDetail()
  }
})
</script>

<style scoped>
.customer-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
