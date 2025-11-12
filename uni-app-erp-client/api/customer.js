// api/customer.js - 客户相关 API
import request from '@/utils/request';

/**
 * 获取我的客户信息
 */
export function getMyCustomerInfo() {
	return request({
		url: '/app/customer/my',
		method: 'GET'
	});
}
