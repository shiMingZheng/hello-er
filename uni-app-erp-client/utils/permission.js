// utils/permission.js - 前端权限工具
import { useUserStore } from '@/store/user';

/**
 * 权限标识说明：
 * admin:*:* - 管理员权限（全部）
 * app:product:view - 客户查看商品
 * app:order:create - 客户创建订单
 * app:order:view - 客户查看自己的订单
 * app:finance:view - 客户查看自己的财务
 */

/**
 * 检查是否有指定权限
 * @param {String} permission - 权限标识，如 'admin:order:approve'
 * @returns {Boolean}
 */
export function hasPermission(permission) {
	const userStore = useUserStore();
	const role = userStore.userInfo?.role;
	
	// 未登录无权限
	if (!role) {
		return false;
	}
	
	// 管理员有全部权限
	if (role === 'ADMIN') {
		return true;
	}
	
	// 客户角色的权限判断
	if (role === 'CUSTOMER') {
		// 客户允许的权限列表
		const customerPermissions = [
			'app:product:view',    // 查看商品
			'app:order:create',    // 创建订单
			'app:order:view',      // 查看自己的订单
			'app:finance:view',    // 查看自己的财务
			'app:customer:view'    // 查看自己的客户信息
		];
		
		return customerPermissions.includes(permission);
	}
	
	return false;
}

/**
 * 检查是否是管理员
 * @returns {Boolean}
 */
export function isAdmin() {
	const userStore = useUserStore();
	return userStore.userInfo?.role === 'ADMIN';
}

/**
 * 检查是否是客户
 * @returns {Boolean}
 */
export function isCustomer() {
	const userStore = useUserStore();
	return userStore.userInfo?.role === 'CUSTOMER';
}

/**
 * 检查是否有任意一个权限
 * @param {Array<String>} permissions - 权限数组
 * @returns {Boolean}
 */
export function hasAnyPermission(permissions) {
	return permissions.some(permission => hasPermission(permission));
}

/**
 * 检查是否有全部权限
 * @param {Array<String>} permissions - 权限数组
 * @returns {Boolean}
 */
export function hasAllPermissions(permissions) {
	return permissions.every(permission => hasPermission(permission));
}
