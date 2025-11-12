// [新文件] uni-app-erp-client/store/order.js
import { defineStore } from 'pinia';

export const useOrderStore = defineStore('order', {
	state: () => ({
		/**
		 * 订单列表
		 * 格式: [{ id: 'xxx', items: [...], totalPrice: 99, createTime: '...' }]
		 */
		orders: [],
	}),
	
	getters: {
		/**
		 * 按时间倒序排列订单
		 */
		sortedOrders: (state) => {
			// .slice() 是为了防止“原地”排序修改了 state
			return state.orders.slice().sort((a, b) => b.createTimeRaw - a.createTimeRaw);
		}
	},
	
	actions: {
		/**
		 * 创建一个新订单
		 * @param {object} orderData (来自 checkout 页的数据, 包含 items 和 totalPrice)
		 */
		createOrder(orderData) {
			const now = new Date();
			
			const newOrder = {
				id: 'Order-' + now.getTime(), // 模拟一个唯一ID
				items: orderData.items, // 购物车商品
				totalPrice: orderData.totalPrice,
				createTime: now.toLocaleString(), // '2025/11/5 10:30:00'
				createTimeRaw: now.getTime(), // 用于排序
				status: '待发货' // 模拟状态
			};
			
			// 将新订单添加到列表的“最前面”
			this.orders.unshift(newOrder);
			
			console.log('Order Store: 新订单已创建', newOrder.id);
		}
	}
});