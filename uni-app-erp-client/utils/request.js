// 导入 user store, 准备读取 token
// 注意：不能在文件顶层调用 useUserStore()，必须在函数内部调用
import { useUserStore } from '@/store/user';

// 基础配置
const BASE_URL = 'https://api.yourdomain.com/v1'; // 替换为您的 API 地址

/**
 * 封装的 uni.request
 */
const request = (options) => {
	console.log('Request: 开始请求', options.url);

	return new Promise((resolve, reject) => {
		
		// 1. 请求拦截器 (Request Interceptor)
		const userStore = useUserStore(); // 在函数内获取 store
		
		// 自动注入 Token
		if (userStore.token) {
			if (!options.header) {
				options.header = {};
			}
			options.header.Authorization = `Bearer ${userStore.token}`;
			console.log('Request: 注入 Token');
		}

		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: options.header || {},
			
			success: (res) => {
				// 2. 响应拦截器 (Response Interceptor)
				console.log('Response: 收到响应', res.statusCode);
				
				// 模拟 Token 失效
				if (res.statusCode === 401) {
					console.error('Response: Token 失效 (401)');
					userStore.logout(); // 清除本地 token
					
					// 强制跳转登录页
					uni.navigateTo({
						url: '/pages/login/login' // 假设您未来有一个登录页
					});
					
					return reject('Token 失效');
				}
				
				// 200 OK
				if (res.statusCode === 200) {
					// 这里假设后端返回 { code: 0, data: {...} } 格式
					if (res.data.code === 0) {
						resolve(res.data.data); // 只返回 data 部分
					} else {
						// 业务错误 (例如：库存不足)
						uni.showToast({
							icon: 'none',
							title: res.data.message || '业务错误'
						});
						reject(res.data.message);
					}
				} else {
					// 其他 HTTP 错误
					reject('HTTP 错误 ' + res.statusCode);
				}
			},
			
			fail: (err) => {
				// 3. 网络错误处理
				console.error('Request: 网络请求失败', err);
				uni.showToast({
					icon: 'none',
					title: '网络开小差了'
				});
				reject(err);
			}
		});
	});
};

export default request;