import { defineStore } from 'pinia';

// 定义一个叫 'user' 的 store
export const useUserStore = defineStore('user', {
    // 状态
	state: () => ({
		token: null,
		userInfo: {
			nickname: '未登录'
		},
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
		login(fakeToken) {
			this.token = fakeToken;
			this.userInfo.nickname = '已登录用户';
			console.log('Pinia: 登录成功, Token已设置');
		},

		/**
		 * 模拟登出
		 */
		logout() {
			this.token = null;
			this.userInfo.nickname = '未登录';
			console.log('Pinia: 成功登出');
		},
	},
});