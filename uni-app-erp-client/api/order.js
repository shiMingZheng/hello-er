// api/order.js - 订单相关 API
import request from '@/utils/request';

/**
 * 创建订单
 * @param {Object} data - { customerId, items: [{ productId, quantity }], remark }
 */
export function createOrder(data) {
	return request({
		url: '/app/order',
		method: 'POST',
		data
	});
}

/**
 * 获取我的订单列表
 */
export function getMyOrders() {
	return request({
		url: '/app/order/my',
		method: 'GET'
	});
}

/**
 * 获取订单详情
 * @param {Number} id - 订单 ID
 */
export function getOrderDetail(id) {
	return request({
		url: `/app/order/${id}`,
		method: 'GET'
	});
}
