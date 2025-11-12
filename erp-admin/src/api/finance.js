import request from './request'

/**
 * 获取应收账款列表（分页）
 */
export function getReceivableList(params) {
  return request({
    url: '/admin/finance/receivable',
    method: 'get',
    params
  })
}

/**
 * 获取客户应收列表
 */
export function getCustomerReceivables(customerId) {
  return request({
    url: `/admin/finance/receivable/customer/${customerId}`,
    method: 'get'
  })
}

/**
 * 录入收款（简单核销）
 */
export function recordPayment(data) {
  return request({
    url: '/admin/finance/payment',
    method: 'post',
    data
  })
}

/**
 * 批量核销（一笔收款核销多个订单）
 */
export function recordBatchPayment(data) {
  return request({
    url: '/admin/finance/payment/batch',
    method: 'post',
    data
  })
}

/**
 * 获取收款记录列表
 */
export function getPaymentList(customerId) {
  return request({
    url: `/admin/finance/payment/customer/${customerId}`,
    method: 'get'
  })
}

/**
 * 获取客户欠款汇总
 */
export function getCustomerDebt(customerId) {
  return request({
    url: `/admin/finance/debt/customer/${customerId}`,
    method: 'get'
  })
}

/**
 * 账龄分析
 */
export function analyzeReceivableAge(customerId) {
  return request({
    url: `/admin/finance/age/customer/${customerId}`,
    method: 'get'
  })
}

/**
 * 生成月度对账单
 */
export function generateMonthlyStatement(customerId, year, month) {
  return request({
    url: `/admin/finance/statement/customer/${customerId}`,
    method: 'get',
    params: { year, month }
  })
}
