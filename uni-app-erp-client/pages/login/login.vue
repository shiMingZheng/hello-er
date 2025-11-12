<template>
	<view class="container">
		<view class="logo-box">
			<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
			<text class="app-name">企业进销存系统</text>
		</view>
		
		<view class="form-box">
			<view class="input-group">
				<text class="label">用户名</text>
				<input 
					v-model="formData.username" 
					placeholder="请输入用户名" 
					class="input"
					:maxlength="20"
				/>
			</view>
			
			<view class="input-group">
				<text class="label">密码</text>
				<input 
					v-model="formData.password" 
					type="password"
					placeholder="请输入密码" 
					class="input"
					:maxlength="20"
				/>
			</view>
			
			<button 
				class="login-btn" 
				type="primary" 
				@click="handleLogin"
				:loading="loading"
				:disabled="loading"
			>
				{{ loading ? '登录中...' : '登录' }}
			</button>
			
			<view class="footer-links">
				<text class="link" @click="goRegister">还没账号？立即注册</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '@/store/user';
import { login } from '@/api/auth';

const userStore = useUserStore();
const loading = ref(false);

const formData = ref({
	username: '',
	password: ''
});

/**
 * 处理登录
 */
const handleLogin = async () => {
	// 1. 表单验证
	if (!formData.value.username) {
		uni.showToast({ title: '请输入用户名', icon: 'none' });
		return;
	}
	if (!formData.value.password) {
		uni.showToast({ title: '请输入密码', icon: 'none' });
		return;
	}
	
	// 2. 调用登录 API
	loading.value = true;
	try {
		const res = await login({
			username: formData.value.username,
			password: formData.value.password
		});
		
		console.log('登录响应:', res);
		
		// 3. 保存 Token 和用户信息到 Store
		userStore.token = res.token;
		userStore.userInfo = res.userInfo;
		
		// 持久化存储
		uni.setStorageSync('token', res.token);
		uni.setStorageSync('userInfo', res.userInfo);
		
		// 4. 提示成功
		uni.showToast({
			title: '登录成功',
			icon: 'success',
			duration: 1500
		});
		
		// 5. 跳转首页（延迟跳转，让提示显示完）
		setTimeout(() => {
			uni.switchTab({
				url: '/pages/index/index'
			});
		}, 1500);
		
	} catch (e) {
		console.error('登录失败:', e);
		// request.js 已经显示了 Toast，这里不用再提示
	} finally {
		loading.value = false;
	}
};

/**
 * 跳转注册页
 */
const goRegister = () => {
	uni.navigateTo({
		url: '/pages/register/register'
	});
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 80rpx 40rpx;
}

.logo-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 80rpx;
}

.logo {
	width: 150rpx;
	height: 150rpx;
	margin-bottom: 20rpx;
}

.app-name {
	font-size: 38rpx;
	color: #fff;
	font-weight: bold;
}

.form-box {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
}

.input-group {
	margin-bottom: 40rpx;
}

.label {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 15rpx;
	font-weight: bold;
}

.input {
	width: 100%;
	height: 90rpx;
	background-color: #f5f5f5;
	border-radius: 10rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.login-btn {
	width: 100%;
	height: 90rpx;
	line-height: 90rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border: none;
	border-radius: 10rpx;
	color: #fff;
	font-size: 32rpx;
	font-weight: bold;
	margin-top: 20rpx;
}

.footer-links {
	text-align: center;
	margin-top: 40rpx;
}

.link {
	font-size: 26rpx;
	color: #667eea;
}
</style>