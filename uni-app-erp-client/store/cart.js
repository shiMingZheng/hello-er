// [新代码] uni-app-erp-client/store/cart.js
import { defineStore } from 'pinia';
import { useUserStore } from './user'; // 导入 user store (未来可能需要)

export const useCartStore = defineStore('cart', {
	state: () => ({
		/**
		 * 购物车商品列表
		 * 格式: [{ id: '123', name: '商品A', price: 99, quantity: 1, ... }]
		 */
		items: [],
	}),
	
	getters: {
		/**
		 * 购物车商品总数
		 */
		totalCount: (state) => {
			return state.items.reduce((total, item) => total + item.quantity, 0);
		},
		
		/**
		 * 购物车总价
		 */
		totalPrice: (state) => {
			return state.items.reduce((total, item) => total + (item.price * item.quantity), 0).toFixed(2);
		},
		
		/**
		 * 检查某商品是否已在购物车
		 * @param {string} productId 
		 */
		isInCart: (state) => (productId) => {
			return state.items.some(item => item.id === productId);
		}
	},
	
	actions: {
		/**
		 * 添加商品到购物车
		 * @param {object} product 商品对象 (如 { id: '123', name: '商品A', price: 99 })
		 */
		addItem(product) {
			const existingItem = this.items.find(item => item.id === product.id);
			
			if (existingItem) {
				// 1. 如果已存在，数量+1
				existingItem.quantity++;
				console.log('Cart Store: 商品数量 +1');
			} else {
				// 2. 如果不存在，添加新商品
				this.items.push({ ...product, quantity: 1 });
				console.log('Cart Store: 商品已添加到购物车');
			}
			
			uni.showToast({
				title: '添加成功',
				icon: 'success'
			});
		},
		
		/**
		 * 移除商品
		 * @param {string} productId 
		 */
		removeItem(productId) {
			this.items = this.items.filter(item => item.id !== productId);
			console.log(`Cart Store: 商品 ${productId} 已移除`);
		},
		
		/**
		 * 更新商品数量
		 * @param {string} productId 
		 * @param {number} newQuantity 
		 */
		updateQuantity(productId, newQuantity) {
			const item = this.items.find(item => item.id === productId);
			if (item) {
				if (newQuantity <= 0) {
					// 数量为0，则移除
					this.removeItem(productId);
				} else {
					item.quantity = newQuantity;
				}
			}
		},
		
		/**
		 * 清空购物车
		 */
		clearCart() {
			this.items = [];
			console.log('Cart Store: 购物车已清空');
		}
	}
});