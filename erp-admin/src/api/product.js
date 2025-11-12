import request from './request'

/**
 * 获取商品列表（分页）
 */
export function getProductList(params) {
  return request({
    url: '/admin/product',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 */
export function getProduct(id) {
  return request({
    url: `/admin/product/${id}`,
    method: 'get'
  })
}

/**
 * 创建商品
 */
export function createProduct(data) {
  return request({
    url: '/admin/product',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 */
export function updateProduct(id, data) {
  return request({
    url: `/admin/product/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品（下架）
 */
export function deleteProduct(id) {
  return request({
    url: `/admin/product/${id}`,
    method: 'delete'
  })
}

/**
 * 获取库存预警商品
 */
export function getLowStockProducts() {
  return request({
    url: '/admin/product/low-stock',
    method: 'get'
  })
}

/**
 * 调整库存
 */
export function adjustStock(id, quantity) {
  return request({
    url: `/admin/product/${id}/stock`,
    method: 'post',
    params: { quantity }
  })
}
