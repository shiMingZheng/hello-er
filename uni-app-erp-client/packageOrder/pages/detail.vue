<template>
	<view class="container">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-box">
			<text>加载中...</text>
		</view>

		<!-- 订单详情 -->
		<view v-else-if="order" class="order-detail">
			<!-- 订单状态卡片 -->
			<view class="status-card">
				<view class="status-icon">
					<text>{{ getStatusIcon() }}</text>
				</view>
				<text class="status-text">{{ getStatusText() }}</text>
				<text class="status-tip">{{ getStatusTip() }}</text>
			</view>

			<!-- 订单信息 -->
			<view class="section-card">
				<view class="section-title">订单信息</view>
				<view class="info-row">
					<text class="label">订单编号:</text>
					<text class="value">{{ order.orderNo }}</text>
				</view>
				<view class="info-row">
					<text class="label">下单时间:</text>
					<text class="value">{{ formatTime(order.createTime) }}</text>
				</view>
				<view class="info-row">
					<text class="label">订单状态:</text>
					<text class="value status-badge" :class="getStatusClass()">
						{{ getStatusText() }}
					</text>
				</view>
			</view>

			<!-- 商品清单 -->
			<view class="section-card">
				<view class="section-title">商品清单</view>
				<view v-for="item in order.items" :key="item.id" class="item-row">
					<view class="item-info">
						<text class="item-name">{{ item.productName }}</text>
						<text class="item-spec">¥{{ item.price }} × {{ item.quantity }}</text>
					</view>
					<text class="item-subtotal">¥{{ item.subtotal }}</text>
				</view>
			</view>

			<!-- 费用明细 -->
			<view class="section-card">
				<view class="section-title">费用明细</view>
				<view class="fee-row">
					<text class="fee-label">商品总价:</text>
					<text class="fee-value">¥{{ order.totalAmount }}</text>
				</view>
				<view class="fee-row total">
					<text class="fee-label">订单总额:</text>
					<text class="fee-value total-price">¥{{ order.totalAmount }}</text>
				</view>
			</view>

			<!-- 备注 -->
			<view v-if="order.remark" class="section-card">
				<view class="section-title">订单备注</view>
				<text class="remark-text">{{ order.remark }}</text>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view v-if="order" class="footer-bar">
			<button class="action-btn" @click="goBack">返回</button>
			<button v-if="canCancel()" class="action-btn cancel" @click="cancelOrder">
				取消订单
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getOrderDetail } from '@/api/order';

const order = ref(null);
const loading = ref(false);

onLoad((options) => {
	if (options.id) {
		loadOrderDetail(options.id);
	} else {
		uni.showToast({ title: '订单ID缺失', icon: 'error' });
		uni.navigateBack();
	}
});

/**
 * 加载订单详情
 */
const loadOrderDetail = async (orderId) => {
	loading.value = true;
	try {
		const data = await getOrderDetail(orderId);
		order.value = data;
		console.log('✅ 订单详情加载成功:', data);
	} catch (e) {
		console.error('❌ 加载订单详情失败:', e);
		uni.showToast({ title: '加载失败', icon: 'error' });
		setTimeout(() => uni.navigateBack(), 1500);
	} finally {
		loading.value = false;
	}
};

/**
 * 获取状态图标
 */
const getStatusIcon = () => {
	const icons = {
		PENDING: '⏳',
		APPROVED: '✅',
		SHIPPED: '🚚',
		COMPLETED: '✔️',
		CANCELLED: '❌'
	};
	return icons[order.value.status] || '📦';
};

/**
 * 获取状态文本
 */
const getStatusText = () => {
	const texts = {
		PENDING: '待审核',
		APPROVED: '已审核',
		SHIPPED: '已发货',
		COMPLETED: '已完成',
		CANCELLED: '已取消'
	};
	return texts[order.value.status] || '未知';
};

/**
 * 获取状态提示
 */
const getStatusTip = () => {
	const tips = {
		PENDING: '订单已提交，等待审核',
		APPROVED: '订单已通过审核，准备发货',
		SHIPPED: '商品正在配送中',
		COMPLETED: '订单已完成',
		CANCELLED: '订单已取消'
	};
	return tips[order.value.status] || '';
};

/**
 * 获取状态样式类
 */
const getStatusClass = () => {
	const classes = {
		PENDING: 'pending',
		APPROVED: 'approved',
		SHIPPED: 'shipped',
		COMPLETED: 'completed',
		CANCELLED: 'cancelled'
	};
	return classes[order.value.status] || '';
};

/**
 * 格式化时间
 */
const formatTime = (timeStr) => {
	if (!timeStr) return '-';
	const date = new Date(timeStr);
	return date.toLocaleString('zh-CN', { 
		year: 'numeric', 
		month: '2-digit', 
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit'
	});
};

/**
 * 是否可以取消订单
 */
const canCancel = () => {
	return order.value.status === 'PENDING';
};

/**
 * 取消订单
 */
const cancelOrder = () => {
	uni.showModal({
		title: '确认取消',
		content: '确定要取消这个订单吗？',
		success: (res) => {
			if (res.confirm) {
				// TODO: 调用取消订单 API
				uni.showToast({ title: '取消成功', icon: 'success' });
			}
		}
	});
};

/**
 * 返回
 */
const goBack = () => {
	uni.navigateBack();
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 120rpx;
}

.loading-box {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 200rpx 0;
	color: #999;
}

/* 状态卡片 */
.status-card {
	background-color: #fff;
	padding: 60rpx 40rpx;
	text-align: center;
	margin-bottom: 20rpx;
}

.status-icon {
	font-size: 100rpx;
	margin-bottom: 20rpx;
}

.status-text {
	display: block;
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 10rpx;
}

.status-tip {
	display: block;
	font-size: 26rpx;
	color: #999;
}

/* 分段卡片 */
.section-card {
	background-color: #fff;
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
}

.status-badge {
	padding: 5rpx 15rpx;
	border-radius: 5rpx;
	font-size: 24rpx;
}

.status-badge.pending {
	background-color: #fff3e0;
	color: #ff9800;
}

.status-badge.approved {
	background-color: #e8f5e9;
	color: #4caf50;
}

.status-badge.shipped {
	background-color: #e3f2fd;
	color: #2196f3;
}

.status-badge.completed {
	background-color: #f3e5f5;
	color: #9c27b0;
}

.status-badge.cancelled {
	background-color: #ffebee;
	color: #f44336;
}

/* 商品行 */
.item-row {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	padding: 25rpx 0;
	border-bottom: 1px solid #f5f5f5;
}

.item-row:last-child {
	border-bottom: none;
}

.item-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.item-name {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 10rpx;
}

.item-spec {
	font-size: 24rpx;
	color: #999;
}

.item-subtotal {
	font-size: 30rpx;
	color: #e4393c;
	font-weight: bold;
}

/* 费用行 */
.fee-row {
	display: flex;
	justify-content: space-between;
	padding: 20rpx 0;
	font-size: 28rpx;
}

.fee-label {
	color: #666;
}

.fee-value {
	color: #333;
}

.fee-row.total {
	border-top: 1px solid #f0f0f0;
	padding-top: 30rpx;
	margin-top: 20rpx;
}

.total-price {
	font-size: 36rpx;
	color: #e4393c;
	font-weight: bold;
}

/* 备注 */
.remark-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

/* 底部操作栏 */
.footer-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: #fff;
	border-top: 1px solid #f0f0f0;
	padding: 20rpx;
	display: flex;
	gap: 20rpx;
}

.action-btn {
	flex: 1;
	height: 80rpx;
	line-height: 80rpx;
	margin: 0;
}

.action-btn.cancel {
	background-color: #fff;
	color: #f44336;
	border: 1px solid #f44336;
}
</style>
