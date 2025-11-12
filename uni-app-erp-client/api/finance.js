// api/finance.js - 财务相关 API
import request from '@/utils/request';

/**
 * 获取我的欠款汇总
 */
export function getMyDebt() {
	return request({
		url: '/app/finance/debt/my',
		method: 'GET'
	});
}

/**
 * 获取我的应收账款列表
 */
export function getMyReceivables() {
	return request({
		url: '/app/finance/receivable/my',
		method: 'GET'
	});
}

/**
 * 获取我的收款记录
 */
export function getMyPayments() {
	return request({
		url: '/app/finance/payment/my',
		method: 'GET'
	});
}
