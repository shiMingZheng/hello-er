// utils/request.js（增强版）
import { useUserStore } from '@/store/user';

// 基础配置
const BASE_URL = 'http://localhost:8080'; // 后端地址，实际部署时改为线上地址

/**
 * 封装的 uni.request（增强版）
 */
const request = (options) => {
	console.log('🚀 Request: 开始请求', options.url);

	return new Promise((resolve, reject) => {
		
		// 1. 请求拦截器 (Request Interceptor)
		const userStore = useUserStore();
		
		// 自动注入 Token
		const header = options.header || {};
		if (userStore.token) {
			header.Authorization = `Bearer ${userStore.token}`;
			console.log('🔑 Request: 注入 Token', userStore.token.substring(0, 20) + '...');
		} else {
			console.log('⚠️ Request: 未登录，无 Token');
		}

		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header,
			timeout: options.timeout || 10000, // 默认 10 秒超时
			
			success: (res) => {
				console.log('✅ Response:', res.statusCode, options.url);
				
				// 2. 响应拦截器 (Response Interceptor)
				
				// 2.1 处理 Token 失效（401）
				if (res.statusCode === 401) {
					console.error('🚫 Response: Token 失效 (401)');
					userStore.logout(); // 清除本地 token
					
					uni.showToast({
						title: '登录已过期，请重新登录',
						icon: 'none',
						duration: 2000
					});
					
					// 强制跳转登录页（延迟 1.5 秒，让提示显示完）
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						});
					}, 1500);
					
					return reject({ code: 401, message: 'Token 失效' });
				}
				
				// 2.2 处理 200 OK
				if (res.statusCode === 200) {
					// 后端返回格式：{ code: 200, message: "success", data: {...} }
					if (res.data.code === 200) {
						console.log('✅ Response: 请求成功', res.data.data);
						resolve(res.data.data); // 只返回 data 部分
					} else {
						// 业务错误（例如：库存不足）
						console.warn('⚠️ Response: 业务错误', res.data.message);
						uni.showToast({
							icon: 'none',
							title: res.data.message || '操作失败'
						});
						reject({ code: res.data.code, message: res.data.message });
					}
				} else {
					// 其他 HTTP 错误
					console.error('❌ Response: HTTP 错误', res.statusCode);
					uni.showToast({
						icon: 'none',
						title: `请求失败 (${res.statusCode})`
					});
					reject({ code: res.statusCode, message: 'HTTP 错误' });
				}
			},
			
			fail: (err) => {
				// 3. 网络错误处理
				console.error('❌ Request: 网络请求失败', err);
				
				let errorMessage = '网络开小差了';
				if (err.errMsg) {
					if (err.errMsg.includes('timeout')) {
						errorMessage = '请求超时，请检查网络';
					} else if (err.errMsg.includes('fail')) {
						errorMessage = '无法连接服务器';
					}
				}
				
				uni.showToast({
					icon: 'none',
					title: errorMessage
				});
				reject(err);
			}
		});
	});
};

export default request;
