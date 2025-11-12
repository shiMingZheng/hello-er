import request from './request'

/**
 * 获取订单列表（分页）
 */
export function getOrderList(params) {
  return request({
    url: '/admin/order',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(id) {
  return request({
    url: `/admin/order/${id}`,
    method: 'get'
  })
}

/**
 * 审核订单
 */
export function approveOrder(id) {
  return request({
    url: `/admin/order/${id}/approve`,
    method: 'post'
  })
}

/**
 * 订单发货
 */
export function shipOrder(id) {
  return request({
    url: `/admin/order/${id}/ship`,
    method: 'post'
  })
}

/**
 * 完成订单
 */
export function completeOrder(id) {
  return request({
    url: `/admin/order/${id}/complete`,
    method: 'post'
  })
}
