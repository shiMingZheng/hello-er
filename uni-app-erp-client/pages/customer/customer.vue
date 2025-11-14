<template>
	<view class="container">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-box">
			<text>加载中...</text>
		</view>

		<!-- 客户信息 -->
		<view v-else-if="customerInfo" class="customer-info">
			<!-- 头部卡片 -->
			<view class="header-card">
				<image class="avatar" src="/static/logo.png" mode="aspectFit"></image>
				<view class="header-info">
					<text class="customer-name">{{ customerInfo.name }}</text>
					<text class="customer-level" :class="getLevelClass()">
						{{ getLevelText() }}
					</text>
				</view>
			</view>

			<!-- 基本信息 -->
			<view class="section-card">
				<view class="section-title">基本信息</view>
				<view class="info-row">
					<text class="label">客户编号:</text>
					<text class="value">{{ customerInfo.id }}</text>
				</view>
				<view class="info-row">
					<text class="label">客户名称:</text>
					<text class="value">{{ customerInfo.name }}</text>
				</view>
				<view class="info-row" v-if="customerInfo.contact">
					<text class="label">联系人:</text>
					<text class="value">{{ customerInfo.contact }}</text>
				</view>
				<view class="info-row" v-if="customerInfo.phone">
					<text class="label">联系电话:</text>
					<text class="value">{{ customerInfo.phone }}</text>
				</view>
				<view class="info-row" v-if="customerInfo.email">
					<text class="label">邮箱:</text>
					<text class="value">{{ customerInfo.email }}</text>
				</view>
				<view class="info-row" v-if="customerInfo.address">
					<text class="label">地址:</text>
					<text class="value">{{ customerInfo.address }}</text>
				</view>
			</view>

			<!-- 信用信息 -->
			<view class="section-card">
				<view class="section-title">信用信息</view>
				<view class="credit-info">
					<view class="credit-item">
						<text class="credit-label">信用额度</text>
						<text class="credit-value">¥{{ customerInfo.creditLimit }}</text>
					</view>
					<view class="credit-item">
						<text class="credit-label">当前欠款</text>
						<text class="credit-value debt">¥{{ customerInfo.balance }}</text>
					</view>
					<view class="credit-item">
						<text class="credit-label">可用额度</text>
						<text class="credit-value available">¥{{ customerInfo.availableCredit }}</text>
					</view>
				</view>
				
				<!-- 信用额度进度条 -->
				<view class="credit-bar">
					<view class="bar-bg">
						<view class="bar-fill" :style="{ width: getCreditUsagePercent() + '%' }"></view>
					</view>
					<text class="bar-text">已使用 {{ getCreditUsagePercent() }}%</text>
				</view>
			</view>

			<!-- 账户状态 -->
			<view class="section-card">
				<view class="section-title">账户状态</view>
				<view class="info-row">
					<text class="label">当前状态:</text>
					<text class="value status-badge" :class="getStatusClass()">
						{{ getStatusText() }}
					</text>
				</view>
				<view class="info-row">
					<text class="label">注册时间:</text>
					<text class="value">{{ formatTime(customerInfo.createTime) }}</text>
				</view>
			</view>

			<!-- 备注信息 -->
			<view v-if="customerInfo.remark" class="section-card">
				<view class="section-title">备注信息</view>
				<text class="remark-text">{{ customerInfo.remark }}</text>
			</view>

			<!-- 快捷操作 -->
			<view class="action-section">
				<button class="action-btn" @click="goToOrders">我的订单</button>
				<button class="action-btn" @click="goToFinance">财务中心</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getMyCustomerInfo } from '@/api/customer';

const customerInfo = ref(null);
const loading = ref(false);

onLoad(() => {
	loadCustomerInfo();
});

// 页面显示时刷新数据
onShow(() => {
	loadCustomerInfo();
});

/**
 * 加载客户信息
 */
const loadCustomerInfo = async () => {
	loading.value = true;
	try {
		const data = await getMyCustomerInfo();
		customerInfo.value = data;
		console.log('✅ 客户信息加载成功:', data);
	} catch (e) {
		console.error('❌ 加载客户信息失败:', e);
		uni.showToast({ title: '加载失败', icon: 'error' });
	} finally {
		loading.value = false;
	}
};

/**
 * 获取客户等级文本
 */
const getLevelText = () => {
	return customerInfo.value.level === 'VIP' ? 'VIP 客户' : '普通客户';
};

/**
 * 获取等级样式类
 */
const getLevelClass = () => {
	return customerInfo.value.level === 'VIP' ? 'vip' : 'normal';
};

/**
 * 获取状态文本
 */
const getStatusText = () => {
	return customerInfo.value.status === 'ACTIVE' ? '正常' : '已禁用';
};

/**
 * 获取状态样式类
 */
const getStatusClass = () => {
	return customerInfo.value.status === 'ACTIVE' ? 'active' : 'inactive';
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
 * 格式化时间
 */
const formatTime = (timeStr) => {
	if (!timeStr) return '-';
	const date = new Date(timeStr);
	return date.toLocaleDateString('zh-CN');
};

/**
 * 跳转到订单列表
 */
const goToOrders = () => {
	uni.switchTab({
		url: '/packageOrder/pages/list'
	});
};

/**
 * 跳转到财务中心
 */
const goToFinance = () => {
	uni.navigateTo({
		url: '/pages/finance/finance'
	});
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 20rpx;
	padding-bottom: 40rpx;
}

.loading-box {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 200rpx 0;
	color: #999;
}

/* 头部卡片 */
.header-card {
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

.header-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.customer-name {
	font-size: 36rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 15rpx;
}

.customer-level {
	font-size: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	align-self: flex-start;
}

.customer-level.vip {
	background-color: #ff9800;
	color: #fff;
}

.customer-level.normal {
	background-color: rgba(255, 255, 255, 0.3);
	color: #fff;
}

/* 分段卡片 */
.section-card {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1px solid #f0f0f0;
}

/* 信息行 */
.info-row {
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
	text-align: right;
	max-width: 400rpx;
}

.status-badge {
	padding: 5rpx 15rpx;
	border-radius: 5rpx;
	font-size: 24rpx;
}

.status-badge.active {
	background-color: #e8f5e9;
	color: #4caf50;
}

.status-badge.inactive {
	background-color: #ffebee;
	color: #f44336;
}

/* 信用信息 */
.credit-info {
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
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.credit-value.debt {
	color: #f44336;
}

.credit-value.available {
	color: #4caf50;
}

/* 信用额度进度条 */
.credit-bar {
	margin-top: 30rpx;
}

.bar-bg {
	height: 20rpx;
	background-color: #f0f0f0;
	border-radius: 10rpx;
	overflow: hidden;
	margin-bottom: 10rpx;
}

.bar-fill {
	height: 100%;
	background: linear-gradient(90deg, #4caf50 0%, #ff9800 70%, #f44336 100%);
	border-radius: 10rpx;
	transition: width 0.3s;
}

.bar-text {
	font-size: 24rpx;
	color: #999;
	text-align: center;
	display: block;
}

/* 备注 */
.remark-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

/* 快捷操作 */
.action-section {
	display: flex;
	gap: 20rpx;
	margin-top: 20rpx;
}

.action-btn {
	flex: 1;
	height: 90rpx;
	line-height: 90rpx;
	margin: 0;
}
</style>
