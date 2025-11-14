<template>
	<view class="container">
		<!-- 未登录状态 -->
		<view v-if="!userStore.isLoggedIn" class="login-prompt">
			<image class="avatar-placeholder" src="/static/logo.png" mode="aspectFit"></image>
			<text class="prompt-text">您还未登录</text>
			<button @click="goToLogin" type="primary" class="login-btn">
				立即登录
			</button>
		</view>
		
		<!-- 已登录状态 -->
		<view v-else class="user-info-section">
			<view class="user-header">
				<image class="avatar" src="/static/logo.png" mode="aspectFit"></image>
				<view class="user-details">
					<text class="username">{{ userStore.userInfo.username }}</text>
					<text class="user-role">{{ getRoleName() }}</text>
				</view>
			</view>
			
			<view class="info-box">
				<view class="info-item">
					<text class="label">用户ID:</text>
					<text class="value">{{ userStore.userInfo.id }}</text>
				</view>
				<view class="info-item">
					<text class="label">角色:</text>
					<text class="value">{{ getRoleName() }}</text>
				</view>
				<view class="info-item" v-if="userStore.userInfo.customerId">
					<text class="label">客户ID:</text>
					<text class="value">{{ userStore.userInfo.customerId }}</text>
				</view>
				<view class="info-item" v-if="customerLevel">
					<text class="label">客户等级:</text>
					<text class="value" :class="{ 'vip-badge': customerLevel === 'VIP' }">
						{{ customerLevel }}
					</text>
				</view>
			</view>
			
			<button @click="handleLogout" class="logout-btn">
				退出登录
			</button>
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();

// 计算客户等级（如果有）
const customerLevel = computed(() => {
	return userStore.userInfo?.customerLevel || null;
});

/**
 * 获取角色名称
 */
const getRoleName = () => {
	const role = userStore.userInfo.role;
	if (role === 'ADMIN') return '管理员';
	if (role === 'CUSTOMER') return '客户';
	return '未知';
};

/**
 * 跳转登录页
 */
const goToLogin = () => {
	uni.navigateTo({
		url: '/pages/login/login'
	});
};

/**
 * 退出登录
 */
const handleLogout = () => {
	uni.showModal({
		title: '确认退出',
		content: '确定要退出登录吗？',
		success: (res) => {
			if (res.confirm) {
				userStore.logout();
				uni.showToast({
					title: '已退出登录',
					icon: 'success'
				});
			}
		}
	});
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 40rpx;
}

/* 未登录状态 */
.login-prompt {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 200rpx;
}

.avatar-placeholder {
	width: 150rpx;
	height: 150rpx;
	border-radius: 50%;
	margin-bottom: 30rpx;
	background-color: #fff;
}

.prompt-text {
	font-size: 32rpx;
	color: #666;
	margin-bottom: 40rpx;
}

.login-btn {
	width: 400rpx;
}

/* 已登录状态 */
.user-info-section {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
}

.user-header {
	display: flex;
	align-items: center;
	padding-bottom: 30rpx;
	border-bottom: 1px solid #f0f0f0;
	margin-bottom: 30rpx;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	margin-right: 30rpx;
	background-color: #f5f5f5;
}

.user-details {
	flex: 1;
}

.username {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 10rpx;
}

.user-role {
	font-size: 26rpx;
	color: #999;
}

.info-box {
	margin-bottom: 40rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1px solid #f5f5f5;
}

.label {
	font-size: 28rpx;
	color: #666;
}

.value {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
}

.vip-badge {
	color: #ff6600;
	background-color: #fff3e0;
	padding: 5rpx 15rpx;
	border-radius: 5rpx;
}

.logout-btn {
	width: 100%;
	background-color: #f5f5f5;
	color: #666;
	border: none;
}
</style>
