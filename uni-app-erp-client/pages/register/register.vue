<template>
	<view class="container">
		<view class="header">
			<text class="title">客户注册</text>
		</view>
		
		<view class="form-box">
			<view class="input-group">
				<text class="label">用户名</text>
				<input 
					v-model="formData.username" 
					placeholder="3-20个字符" 
					class="input"
					:maxlength="20"
				/>
			</view>
			
			<view class="input-group">
				<text class="label">密码</text>
				<input 
					v-model="formData.password" 
					type="password"
					placeholder="6-20个字符" 
					class="input"
					:maxlength="20"
				/>
			</view>
			
			<view class="input-group">
				<text class="label">确认密码</text>
				<input 
					v-model="formData.confirmPassword" 
					type="password"
					placeholder="再次输入密码" 
					class="input"
					:maxlength="20"
				/>
			</view>
			
			<view class="input-group">
				<text class="label">客户名称</text>
				<input 
					v-model="formData.customerName" 
					placeholder="公司或个人名称" 
					class="input"
					:maxlength="50"
				/>
			</view>
			
			<view class="input-group">
				<text class="label">联系电话（可选）</text>
				<input 
					v-model="formData.phone" 
					placeholder="手机号码" 
					class="input"
					type="number"
					:maxlength="11"
				/>
			</view>
			
			<button 
				class="register-btn" 
				type="primary" 
				@click="handleRegister"
				:loading="loading"
				:disabled="loading"
			>
				{{ loading ? '注册中...' : '立即注册' }}
			</button>
			
			<view class="footer-links">
				<text class="link" @click="goLogin">已有账号？去登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { register } from '@/api/auth';

const loading = ref(false);

const formData = ref({
	username: '',
	password: '',
	confirmPassword: '',
	customerName: '',
	phone: ''
});

/**
 * 处理注册
 */
const handleRegister = async () => {
	// 1. 表单验证
	if (!formData.value.username) {
		uni.showToast({ title: '请输入用户名', icon: 'none' });
		return;
	}
	if (formData.value.username.length < 3 || formData.value.username.length > 20) {
		uni.showToast({ title: '用户名长度为 3-20 个字符', icon: 'none' });
		return;
	}
	if (!formData.value.password) {
		uni.showToast({ title: '请输入密码', icon: 'none' });
		return;
	}
	if (formData.value.password.length < 6 || formData.value.password.length > 20) {
		uni.showToast({ title: '密码长度为 6-20 个字符', icon: 'none' });
		return;
	}
	if (formData.value.password !== formData.value.confirmPassword) {
		uni.showToast({ title: '两次密码输入不一致', icon: 'none' });
		return;
	}
	if (!formData.value.customerName) {
		uni.showToast({ title: '请输入客户名称', icon: 'none' });
		return;
	}
	
	// 2. 调用注册 API
	loading.value = true;
	try {
		await register({
			username: formData.value.username,
			password: formData.value.password,
			customerName: formData.value.customerName,
			phone: formData.value.phone || undefined
		});
		
		// 3. 提示成功
		uni.showToast({
			title: '注册成功，请登录',
			icon: 'success',
			duration: 2000
		});
		
		// 4. 跳转登录页
		setTimeout(() => {
			uni.navigateBack();
		}, 2000);
		
	} catch (e) {
		console.error('注册失败:', e);
		// request.js 已经显示了 Toast
	} finally {
		loading.value = false;
	}
};

/**
 * 返回登录页
 */
const goLogin = () => {
	uni.navigateBack();
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 40rpx;
}

.header {
	margin-bottom: 40rpx;
}

.title {
	font-size: 40rpx;
	font-weight: bold;
	color: #333;
}

.form-box {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
}

.input-group {
	margin-bottom: 35rpx;
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
	height: 80rpx;
	background-color: #f5f5f5;
	border-radius: 10rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.register-btn {
	width: 100%;
	height: 90rpx;
	line-height: 90rpx;
	margin-top: 20rpx;
}

.footer-links {
	text-align: center;
	margin-top: 40rpx;
}

.link {
	font-size: 26rpx;
	color: #007AFF;
}
</style>