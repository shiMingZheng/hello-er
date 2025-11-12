import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/Layout/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      // 商品管理
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('@/views/Product/List.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'product/form/:id?',
        name: 'ProductForm',
        component: () => import('@/views/Product/Form.vue'),
        meta: { title: '商品表单' }
      },
      // 订单管理
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('@/views/Order/List.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/Order/Detail.vue'),
        meta: { title: '订单详情' }
      },
      // 财务管理
      {
        path: 'finance/receivable',
        name: 'Receivable',
        component: () => import('@/views/Finance/Receivable.vue'),
        meta: { title: '应收账款' }
      },
      {
        path: 'finance/payment',
        name: 'Payment',
        component: () => import('@/views/Finance/Payment.vue'),
        meta: { title: '收款录入' }
      },
      {
        path: 'finance/statement',
        name: 'Statement',
        component: () => import('@/views/Finance/Statement.vue'),
        meta: { title: '对账单' }
      },
	  // 在 MainLayout 的 children 中添加
	  {
	  path: '/customer',
	  name: 'Customer',
	  redirect: '/customer/list',
	  meta: { title: '客户管理' },
	  children: [
	  	{
	  	path: 'list',
	  	name: 'CustomerList',
	  	component: () => import('@/views/Customer/List.vue'),
	  	meta: { title: '客户列表' }
	  	},
	  	{
	  	path: 'add',
	  	name: 'CustomerAdd',
	  	component: () => import('@/views/Customer/Form.vue'),
	  	meta: { title: '新增客户' }
	  	},
	  	{
	  	path: 'edit/:id',
	  	name: 'CustomerEdit',
	  	component: () => import('@/views/Customer/Form.vue'),
	  	meta: { title: '编辑客户' }
	  	}
	  ]
	  }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ERP 管理系统` : 'ERP 管理系统'
  
  // 从 localStorage 直接读取 token（更可靠）
  const token = localStorage.getItem('token')
  const authStore = useAuthStore()
  
  // 调试日志（生产环境可以删除）
  console.log('路由守卫:', {
    from: from.path,
    to: to.path,
    hasToken: !!token,
    storeToken: !!authStore.token
  })
  
  // 需要登录的页面
  if (to.meta.requiresAuth !== false) {
    if (token) {
      // 如果 store 中没有 token，但 localStorage 有，同步一下
      if (!authStore.token) {
        authStore.token = token
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
          authStore.userInfo = JSON.parse(userInfo)
        }
      }
      next()
    } else {
      // 未登录，跳转到登录页
      next('/login')
    }
  } else {
    // 不需要登录的页面（如登录页）
    if (to.path === '/login' && token) {
      // 已登录用户访问登录页，跳转到首页
      next('/')
    } else {
      next()
    }
  }
})

export default router