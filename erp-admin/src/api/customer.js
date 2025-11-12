import request from './request'

/**
 * 客户管理 API
 */

// 获取客户列表(分页)
export function getCustomerList(params) {
  return request({
    url: '/admin/customer',
    method: 'get',
    params
  })
}

// 获取客户详情
export function getCustomerDetail(id) {
  return request({
    url: `/admin/customer/${id}`,
    method: 'get'
  })
}

// 新增客户
export function createCustomer(data) {
  return request({
    url: '/admin/customer',
    method: 'post',
    data
  })
}

// 编辑客户
export function updateCustomer(id, data) {
  return request({
    url: `/admin/customer/${id}`,
    method: 'put',
    data
  })
}

// 删除客户
export function deleteCustomer(id) {
  return request({
    url: `/admin/customer/${id}`,
    method: 'delete'
  })
}

// 启用/禁用客户
export function toggleCustomerStatus(id) {
  return request({
    url: `/admin/customer/${id}/toggle`,
    method: 'post'
  })
}
