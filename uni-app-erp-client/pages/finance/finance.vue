<template>
	<view class="container">
		<!-- 顶部标签页 -->
		<view class="tabs">
			<view 
				v-for="(tab, index) in tabs" 
				:key="index"
				class="tab-item"
				:class="{ active: currentTab === index }"
				@click="switchTab(index)"
			>
				<text>{{ tab }}</text>
			</view>
		</view>

		<!-- Tab 1: 欠款汇总 -->
		<view v-if="currentTab === 0" class="tab-content">
			<!-- 加载状态 -->
			<view v-if="debtLoading" class="loading-box">
				<text>加载中...</text>
			</view>

			<!-- 欠款卡片 -->
			<view v-else-if="debtInfo" class="debt-overview">
				<view class="debt-card">
					<text class="card-title">当前总欠款</text>
					<text class="debt-amount">¥{{ debtInfo.totalDebt }}</text>
					<view class="debt-detail">
						<view class="detail-item">
							<text class="detail-label">信用额度</text>
							<text class="detail-value">¥{{ debtInfo.creditLimit }}</text>
						</view>
						<view class="detail-item">
							<text class="detail-label">可用额度</text>
							<text class="detail-value available">¥{{ debtInfo.availableCredit }}</text>
						</view>
					</view>
				</view>

				<!-- 温馨提示 -->
				<view class="tips-card">
					<text class="tips-title">💡 温馨提示</text>
					<text class="tips-text">• 请及时结清欠款以维持良好的信用记录</text>
					<text class="tips-text">• 可用额度不足时将无法下单</text>
				</view>
			</view>
		</view>

		<!-- Tab 2: 应收账款 -->
		<view v-if="currentTab === 1" class="tab-content">
			<!-- 加载状态 -->
			<view v-if="receivableLoading" class="loading-box">
				<text>加载中...</text>
			</view>

			<!-- 空状态 -->
			<view v-else-if="receivables.length === 0" class="empty-state">
				<text>暂无应收账款</text>
			</view>

			<!-- 应收列表 -->
			<view v-else class="receivable-list">
				<view v-for="item in receivables" :key="item.id" class="receivable-item">
					<view class="item-header">
						<text class="order-no">订单: {{ item.orderNo }}</text>
						<text class="status-badge" :class="getReceivableStatusClass(item.status)">
							{{ getReceivableStatusText(item.status) }}
						</text>
					</view>
					<view class="item-body">
						<view class="amount-row">
							<text class="label">应收金额:</text>
							<text class="value">¥{{ item.amount }}</text>
						</view>
						<view class="amount-row">
							<text class="label">已收金额:</text>
							<text class="value paid">¥{{ item.paidAmount }}</text>
						</view>
						<view class="amount-row">
							<text class="label">未收金额:</text>
							<text class="value unpaid">¥{{ item.unpaidAmount }}</text>
						</view>
					</view>
					<view class="item-footer">
						<text class="date">{{ formatTime(item.createTime) }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- Tab 3: 收款记录 -->
		<view v-if="currentTab === 2" class="tab-content">
			<!-- 加载状态 -->
			<view v-if="paymentLoading" class="loading-box">
				<text>加载中...</text>
			</view>

			<!-- 空状态 -->
			<view v-else-if="payments.length === 0" class="empty-state">
				<text>暂无收款记录</text>
			</view>

			<!-- 收款列表 -->
			<view v-else class="payment-list">
				<view v-for="item in payments" :key="item.id" class="payment-item">
					<view class="item-header">
						<text class="payment-no">收款: {{ item.paymentNo }}</text>
						<text class="amount">¥{{ item.amount }}</text>
					</view>
					<view class="item-body">
						<view class="info-row">
							<text class="label">关联订单:</text>
							<text class="value">{{ item.orderNo || '-' }}</text>
						</view>
						<view class="info-row">
							<text class="label">支付方式:</text>
							<text class="value">{{ item.paymentMethod || '-' }}</text>
						</view>
						<view v-if="item.remark" class="info-row">
							<text class="label">备注:</text>
							<text class="value">{{ item.remark }}</text>
						</view>
					</view>
					<view class="item-footer">
						<text class="date">{{ formatTime(item.createTime) }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getMyDebt, getMyReceivables, getMyPayments } from '@/api/finance';

const tabs = ['欠款汇总', '应收账款', '收款记录'];
const currentTab = ref(0);

// 欠款信息
const debtInfo = ref(null);
const debtLoading = ref(false);

// 应收账款
const receivables = ref([]);
const receivableLoading = ref(false);

// 收款记录
const payments = ref([]);
const paymentLoading = ref(false);

onLoad(() => {
	loadDebtInfo();
});

/**
 * 切换标签页
 */
const switchTab = (index) => {
	currentTab.value = index;
	
	if (index === 0 && !debtInfo.value) {
		loadDebtInfo();
	} else if (index === 1 && receivables.value.length === 0) {
		loadReceivables();
	} else if (index === 2 && payments.value.length === 0) {
		loadPayments();
	}
};

/**
 * 加载欠款信息
 */
const loadDebtInfo = async () => {
	debtLoading.value = true;
	try {
		const data = await getMyDebt();
		debtInfo.value = data;
		console.log('✅ 欠款信息加载成功:', data);
	} catch (e) {
		console.error('❌ 加载欠款信息失败:', e);
	} finally {
		debtLoading.value = false;
	}
};

/**
 * 加载应收账款
 */
const loadReceivables = async () => {
	receivableLoading.value = true;
	try {
		const data = await getMyReceivables();
		receivables.value = data;
		console.log('✅ 应收账款加载成功:', data);
	} catch (e) {
		console.error('❌ 加载应收账款失败:', e);
	} finally {
		receivableLoading.value = false;
	}
};

/**
 * 加载收款记录
 */
const loadPayments = async () => {
	paymentLoading.value = true;
	try {
		const data = await getMyPayments();
		payments.value = data;
		console.log('✅ 收款记录加载成功:', data);
	} catch (e) {
		console.error('❌ 加载收款记录失败:', e);
	} finally {
		paymentLoading.value = false;
	}
};

/**
 * 获取应收状态文本
 */
const getReceivableStatusText = (status) => {
	const texts = {
		UNPAID: '未收款',
		PARTIAL: '部分收款',
		PAID: '已收款'
	};
	return texts[status] || status;
};

/**
 * 获取应收状态样式
 */
const getReceivableStatusClass = (status) => {
	const classes = {
		UNPAID: 'unpaid',
		PARTIAL: 'partial',
		PAID: 'paid'
	};
	return classes[status] || '';
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
</script>

<style scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 标签页 */
.tabs {
	display: flex;
	background-color: #fff;
	border-bottom: 1px solid #f0f0f0;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 30rpx 0;
	font-size: 28rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
	color: #667eea;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background-color: #667eea;
	border-radius: 2rpx;
}

/* 标签页内容 */
.tab-content {
	padding: 20rpx;
}

.loading-box, .empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 200rpx 0;
	color: #999;
	font-size: 28rpx;
}

/* 欠款卡片 */
.debt-overview {
	
}

.debt-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 50rpx 40rpx;
	margin-bottom: 20rpx;
}

.card-title {
	display: block;
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.8);
	margin-bottom: 20rpx;
}

.debt-amount {
	display: block;
	font-size: 60rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 40rpx;
}

.debt-detail {
	display: flex;
	justify-content: space-around;
}

.detail-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.detail-label {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.7);
	margin-bottom: 10rpx;
}

.detail-value {
	font-size: 32rpx;
	color: #fff;
	font-weight: bold;
}

.detail-value.available {
	color: #4caf50;
}

/* 温馨提示 */
.tips-card {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
}

.tips-title {
	display: block;
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.tips-text {
	display: block;
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 10rpx;
}

/* 应收列表 */
.receivable-list, .payment-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.receivable-item, .payment-item {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1px solid #f0f0f0;
}

.order-no, .payment-no {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
}

.status-badge {
	font-size: 24rpx;
	padding: 5rpx 15rpx;
	border-radius: 5rpx;
}

.status-badge.unpaid {
	background-color: #ffebee;
	color: #f44336;
}

.status-badge.partial {
	background-color: #fff3e0;
	color: #ff9800;
}

.status-badge.paid {
	background-color: #e8f5e9;
	color: #4caf50;
}

.item-body {
	margin-bottom: 20rpx;
}

.amount-row, .info-row {
	display: flex;
	justify-content: space-between;
	padding: 15rpx 0;
	font-size: 26rpx;
}

.label {
	color: #999;
}

.value {
	color: #333;
}

.value.paid {
	color: #4caf50;
}

.value.unpaid {
	color: #f44336;
	font-weight: bold;
}

.item-header .amount {
	font-size: 32rpx;
	color: #4caf50;
	font-weight: bold;
}

.item-footer {
	padding-top: 15rpx;
	border-top: 1px solid #f5f5f5;
}

.date {
	font-size: 24rpx;
	color: #999;
}
</style>
