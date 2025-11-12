// api/product.js - 商品相关 API
import request from '@/utils/request';

/**
 * 获取上架商品列表（客户端）
 * 注意：返回的价格会根据当前登录客户的等级自动计算
 */
export function getProductList() {
	return request({
		url: '/app/product',
		method: 'GET'
	});
}

/**
 * 获取商品详情
 * @param {Number} id - 商品 ID
 */
export function getProductDetail(id) {
	return request({
		url: `/app/product/${id}`,
		method: 'GET'
	});
}
