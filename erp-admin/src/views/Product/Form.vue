<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
          <el-button link @click="$router.back()">
            <el-icon><Back /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>

      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="120px"
        style="max-width: 800px;"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入商品名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品编码" prop="code">
          <el-input 
            ref="codeInputRef"
            v-model="form.code" 
            placeholder="请输入商品编码或使用扫码枪扫描"
            maxlength="50"
            @keyup.enter="handleCodeEnter"
          >
            <template #append>
              <el-button :icon="Search" @click="focusCodeInput">
                扫码
              </el-button>
            </template>
          </el-input>
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            支持扫码枪输入，扫码后会自动填入编码
          </div>
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input 
            v-model="form.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-divider>价格设置</el-divider>

        <el-form-item label="普通价格" prop="normalPrice">
          <el-input-number 
            v-model="form.normalPrice" 
            :min="0" 
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #999;">元</span>
        </el-form-item>

        <el-form-item label="VIP 价格" prop="vipPrice">
          <el-input-number 
            v-model="form.vipPrice" 
            :min="0" 
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #999;">元</span>
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            VIP价格应低于普通价格
          </div>
        </el-form-item>

        <el-divider>库存设置</el-divider>

        <el-form-item label="初始库存" prop="stock">
          <el-input-number 
            v-model="form.stock" 
            :min="0"
            :step="1"
            controls-position="right"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #999;">件</span>
        </el-form-item>

        <el-form-item label="库存预警" prop="stockWarning">
          <el-input-number 
            v-model="form.stockWarning" 
            :min="0"
            :step="1"
            controls-position="right"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #999;">件</span>
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            库存低于此数量时会显示预警标识
          </div>
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
          <el-button @click="$router.back()">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Check, Refresh, Close, Search } from '@element-plus/icons-vue'
import { getProduct, createProduct, updateProduct } from '@/api/product'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => route.params.id && route.params.id !== 'form')

// 表单引用
const formRef = ref(null)
const codeInputRef = ref(null)

// 提交loading
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  name: '',
  code: '',
  description: '',
  normalPrice: 0,
  vipPrice: 0,
  stock: 0,
  stockWarning: 10
})

// 校验规则
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
  ],
  code: [
    { max: 50, message: '商品编码最多50个字符', trigger: 'blur' }
  ],
  normalPrice: [
    { required: true, message: '请输入普通价格', trigger: 'blur' },
    { 
      type: 'number', 
      min: 0.01, 
      message: '价格必须大于0', 
      trigger: 'blur' 
    }
  ],
  vipPrice: [
    { required: true, message: '请输入VIP价格', trigger: 'blur' },
    { 
      type: 'number', 
      min: 0.01, 
      message: '价格必须大于0', 
      trigger: 'blur' 
    },
    {
      validator: (rule, value, callback) => {
        if (value > form.normalPrice) {
          callback(new Error('VIP价格应低于或等于普通价格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { 
      type: 'number', 
      min: 0, 
      message: '库存不能为负数', 
      trigger: 'blur' 
    }
  ],
  stockWarning: [
    { 
      type: 'number', 
      min: 0, 
      message: '预警阈值不能为负数', 
      trigger: 'blur' 
    }
  ]
}

// 加载商品详情
const loadDetail = async () => {
  try {
    const res = await getProduct(route.params.id)
    Object.assign(form, res)
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
    router.back()
  }
}

// 聚焦到编码输入框（用于扫码枪）
const focusCodeInput = () => {
  nextTick(() => {
    codeInputRef.value?.focus()
  })
}

// 编码输入框回车事件
const handleCodeEnter = () => {
  // 扫码枪扫描后会触发回车，可以在这里做额外处理
  ElMessage.success('商品编码已录入')
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value.validate()
    
    submitLoading.value = true
    if (isEdit.value) {
      await updateProduct(route.params.id, form)
      ElMessage.success('保存成功')
    } else {
      await createProduct(form)
      ElMessage.success('新增成功')
    }
    router.push('/product/list')
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
  if (isEdit.value) {
    loadDetail()
  }
}

// 初始化
onMounted(() => {
  if (isEdit.value) {
    loadDetail()
  } else {
    // 新增时自动聚焦到编码输入框
    focusCodeInput()
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
