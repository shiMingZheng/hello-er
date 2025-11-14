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
		<view v-else class="profile-content">
			<!-- 用户头部卡片 -->
			<view class="user-header">
				<image class="avatar" src="/static/logo.png" mode="aspectFit"></image>
				<view class="user-details">
					<text class="username">{{ userStore.userInfo.username }}</text>
					<text class="user-role">{{ getRoleName() }}</text>
				</view>
			</view>

			<!-- 客户信息卡片（如果是客户角色） -->
			<view v-if="isCustomer && customerInfo" class="customer-card">
				<view class="card-header">
					<text class="card-title">客户信息</text>
					<text class="customer-level" :class="getLevelClass()">
						{{ getLevelText() }}
					</text>
				</view>

				<view class="customer-info">
					<view class="info-item">
						<text class="label">客户名称</text>
						<text class="value">{{ customerInfo.name }}</text>
					</view>
					<view class="info-item" v-if="customerInfo.phone">
						<text class="label">联系电话</text>
						<text class="value">{{ customerInfo.phone }}</text>
					</view>
				</view>

				<!-- 信用信息 -->
				<view class="credit-section">
					<text class="section-title">信用额度</text>
					<view class="credit-grid">
						<view class="credit-item">
							<text class="credit-label">总额度</text>
							<text class="credit-value">¥{{ customerInfo.creditLimit }}</text>
						</view>
						<view class="credit-item">
							<text class="credit-label">已使用</text>
							<text class="credit-value debt">¥{{ customerInfo.balance }}</text>
						</view>
						<view class="credit-item">
							<text class="credit-label">可用</text>
							<text class="credit-value available">¥{{ customerInfo.availableCredit }}</text>
						</view>
					</view>
					
					<!-- 进度条 -->
					<view class="progress-bar">
						<view class="bar-bg">
							<view class="bar-fill" :style="{ width: getCreditUsagePercent() + '%' }"></view>
						</view>
						<text class="bar-text">已使用 {{ getCreditUsagePercent() }}%</text>
					</view>
				</view>

				<!-- 快捷入口 -->
				<view class="quick-actions">
					<button class="action-btn" @click="goToFinance" size="mini">
						💰 财务中心
					</button>
				</view>
			</view>

			<!-- 功能菜单 -->
			<view class="menu-section">
				<view class="menu-item" @click="goToOrders">
					<text class="menu-icon">📦</text>
					<text class="menu-text">我的订单</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @click="goToFinance" v-if="isCustomer">
					<text class="menu-icon">💰</text>
					<text class="menu-text">财务中心</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @click="viewCustomerDetail" v-if="isCustomer">
					<text class="menu-icon">👤</text>
					<text class="menu-text">客户详情</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>

			<!-- 退出登录 -->
			<button @click="handleLogout" class="logout-btn">
				退出登录
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';
import { getMyCustomerInfo } from '@/api/customer';

const userStore = useUserStore();
const customerInfo = ref(null);

// 判断是否是客户角色
const isCustomer = computed(() => {
	return userStore.userInfo?.role === 'CUSTOMER';
});

// 页面显示时加载数据
onShow(() => {
	if (userStore.isLoggedIn && isCustomer.value) {
		loadCustomerInfo();
	}
});

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (newVal) => {
	if (newVal && isCustomer.value) {
		loadCustomerInfo();
	} else {
		customerInfo.value = null;
	}
});

/**
 * 加载客户信息
 */
const loadCustomerInfo = async () => {
	try {
		const data = await getMyCustomerInfo();
		customerInfo.value = data;
		console.log('✅ 客户信息加载成功:', data);
	} catch (e) {
		console.error('❌ 加载客户信息失败:', e);
	}
};

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
 * 获取客户等级文本
 */
const getLevelText = () => {
	if (!customerInfo.value) return '';
	return customerInfo.value.level === 'VIP' ? 'VIP客户' : '普通客户';
};

/**
 * 获取等级样式类
 */
const getLevelClass = () => {
	if (!customerInfo.value) return '';
	return customerInfo.value.level === 'VIP' ? 'vip' : 'normal';
};

/**
 * 计算信用额度使用百分比
 */
const getCreditUsagePercent = () => {
	if (!customerInfo.value || customerInfo.value.creditLimit === 0) return 0;
	const percent = (customerInfo.value.balance / customerInfo.value.creditLimit) * 100;
	return Math.min(100, Math.max(0, percent.toFixed(1)));
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
 * 跳转订单列表
 */
const goToOrders = () => {
	uni.switchTab({
		url: '/packageOrder/pages/list'
	});
};

/**
 * 跳转财务中心
 */
const goToFinance = () => {
	uni.switchTab({
		url: '/pages/finance/finance'
	});
};

/**
 * 查看客户完整详情
 */
const viewCustomerDetail = () => {
	uni.navigateTo({
		url: '/pages/customer/customer'
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
				customerInfo.value = null;
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
.profile-content {
	padding: 20rpx;
}

/* 用户头部 */
.user-header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 50rpx 40rpx;
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background-color: #fff;
	margin-right: 30rpx;
}

.user-details {
	flex: 1;
}

.username {
	font-size: 36rpx;
	color: #fff;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}

.user-role {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
}

/* 客户信息卡片 */
.customer-card {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1px solid #f0f0f0;
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.customer-level {
	font-size: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
}

.customer-level.vip {
	background-color: #ff9800;
	color: #fff;
}

.customer-level.normal {
	background-color: #e0e0e0;
	color: #666;
}

.customer-info {
	margin-bottom: 30rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	padding: 20rpx 0;
	font-size: 28rpx;
}

.label {
	color: #666;
}

.value {
	color: #333;
	font-weight: bold;
}

/* 信用信息 */
.credit-section {
	padding-top: 30rpx;
	border-top: 1px solid #f0f0f0;
}

.section-title {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 20rpx;
	display: block;
}

.credit-grid {
	display: flex;
	justify-content: space-around;
	margin-bottom: 30rpx;
}

.credit-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.credit-label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 10rpx;
}

.credit-value {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.credit-value.debt {
	color: #f44336;
}

.credit-value.available {
	color: #4caf50;
}

/* 进度条 */
.progress-bar {
	margin-top: 20rpx;
}

.bar-bg {
	height: 16rpx;
	background-color: #f0f0f0;
	border-radius: 8rpx;
	overflow: hidden;
	margin-bottom: 10rpx;
}

.bar-fill {
	height: 100%;
	background: linear-gradient(90deg, #4caf50 0%, #ff9800 70%, #f44336 100%);
	border-radius: 8rpx;
	transition: width 0.3s;
}

.bar-text {
	font-size: 22rpx;
	color: #999;
	text-align: center;
	display: block;
}

/* 快捷入口 */
.quick-actions {
	margin-top: 30rpx;
	display: flex;
	justify-content: center;
}

.action-btn {
	margin: 0;
}

/* 功能菜单 */
.menu-section {
	background-color: #fff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 35rpx 30rpx;
	border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.menu-text {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.menu-arrow {
	font-size: 40rpx;
	color: #ccc;
}

/* 退出按钮 */
.logout-btn {
	width: 100%;
	background-color: #fff;
	color: #f44336;
	border: 1px solid #f44336;
}
</style>
