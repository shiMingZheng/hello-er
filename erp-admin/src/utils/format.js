import dayjs from 'dayjs'

/**
 * 格式化日期时间
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 */
export function formatDate(date) {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化金额
 */
export function formatMoney(amount, decimals = 2) {
  if (amount === null || amount === undefined) return '¥0.00'
  return `¥${Number(amount).toFixed(decimals)}`
}

/**
 * 格式化订单状态
 */
export function formatOrderStatus(status) {
  const statusMap = {
    'PENDING': { text: '待审核', type: 'warning' },
    'APPROVED': { text: '已审核', type: 'primary' },
    'SHIPPED': { text: '已发货', type: 'success' },
    'COMPLETED': { text: '已完成', type: 'info' },
    'CANCELLED': { text: '已取消', type: 'danger' }
  }
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 格式化应收状态
 */
export function formatReceivableStatus(status) {
  const statusMap = {
    'UNPAID': { text: '未收款', type: 'danger' },
    'PARTIAL': { text: '部分收款', type: 'warning' },
    'PAID': { text: '已收款', type: 'success' }
  }
  return statusMap[status] || { text: status, type: 'info' }
}

/**
 * 格式化客户等级
 */
export function formatCustomerLevel(level) {
  const levelMap = {
    'NORMAL': { text: '普通客户', type: 'info' },
    'VIP': { text: 'VIP 客户', type: 'warning' }
  }
  return levelMap[level] || { text: level, type: 'info' }
}

/**
 * 格式化商品状态
 */
export function formatProductStatus(status) {
  return status === 1 
    ? { text: '已上架', type: 'success' }
    : { text: '已下架', type: 'info' }
}
