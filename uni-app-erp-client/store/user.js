import { defineStore } from 'pinia';
import { login as loginApi } from '@/api/auth';

// 定义一个叫 'user' 的 store
export const useUserStore = defineStore('user', {
    // 状态
	state: () => ({
		token: uni.getStorageSync('token') || null, // 从本地读取
		userInfo: uni.getStorageSync('userInfo') || {
			id: null,
			username: null,
			role: null,
			customerId: null
		}
	}),
    
    // Getters (计算属性)
    getters: {
        isLoggedIn: (state) => state.token !== null,
    },
    
    // Actions (方法)
	actions: {
		/**
		 * 模拟登录, 设置 Token
		 * @param {string} fakeToken 
		 */
		async login(credentials) {
			try {
				const res = await loginApi(credentials); // 调用真实 API
				
				// res 结构：{ token, userInfo: { id, username, role, customerId } }
				this.token = res.token;
				this.userInfo = res.userInfo;
				
				// 持久化存储
				uni.setStorageSync('token', res.token);
				uni.setStorageSync('userInfo', res.userInfo);
				
				console.log('✅ User Store: 登录成功', this.userInfo);
			} catch (e) {
				console.error('❌ User Store: 登录失败', e);
				throw e;
			}
		},

		/**
		 * 模拟登出
		 */
		logout() {
			this.token = null;
			this.userInfo = { id: null, username: null, role: null, customerId: null };
			
			// 清除本地存储
			uni.removeStorageSync('token');
			uni.removeStorageSync('userInfo');
			
			console.log('✅ User Store: 成功登出');
		},
	},
});