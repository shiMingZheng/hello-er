// api/auth.js - 认证相关 API
import request from '@/utils/request';

/**
 * 用户登录
 * @param {Object} data - { username, password }
 */
export function login(data) {
	return request({
		url: '/auth/login',
		method: 'POST',
		data
	});
}

/**
 * 用户注册
 * @param {Object} data - { username, password, customerName, phone }
 */
export function register(data) {
	return request({
		url: '/auth/register',
		method: 'POST',
		data
	});
}
