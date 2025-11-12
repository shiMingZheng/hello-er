<template>
	<view class="container">
		
		<view v-if="orderStore.orders.length === 0" class="empty-state">
			<text>您还没有订单</text>
			<button @click="goHome" size="mini">去逛逛</button>
		</view>

		<view v-else class="order-list">
			<view v-for="order in orderStore.sortedOrders" :key="order.id" class="order-card">
				<view class="card-header">
					<text class="order-id">{{ order.id }}</text>
					<text class="order-status">{{ order.status }}</text>
				</view>
				
				<view class="order-body">
					<image class="item-image" :src="order.items[0].image" mode="aspectFill"></image>
					<view class="item-info">
						<text class="item-name">{{ order.items[0].name }} (等 {{ order.items.length }} 件)</text>
						<text class="item-time">{{ order.createTime }}</text>
					</view>
				</view>
				
				<view class="card-footer">
					<text>合计：</text>
					<text class="total-price">¥ {{ order.totalPrice }}</text>
					<button size="mini" type="default">查看详情</button>
				</view>
			</view>
		</view>

	</view>
</template>

<script setup>
import { useOrderStore } from '@/store/order.js'; // 1. 导入 order store

const orderStore = useOrderStore(); // 2. 获取实例

// 去首页
const goHome = () => {
	uni.switchTab({
		url: '/pages/index/index'
	});
};
</script>

<style>
.container {
	background-color: #f4f4f4;
	min-height: 100vh;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 200rpx;
	color: #999;
}
.empty-state button {
	margin-top: 20rpx;
}

/* 订单列表 */
.order-list {
	padding: 20rpx;
}
.order-card {
	background-color: #fff;
	border-radius: 10rpx;
	margin-bottom: 20rpx;
	padding: 20rpx;
}
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 20rpx;
	border-bottom: 1px solid #f0f0f0;
}
.order-id {
	font-size: 26rpx;
	color: #666;
}
.order-status {
	font-size: 28rpx;
	color: #007AFF;
}

.order-body {
	display: flex;
	padding: 20rpx 0;
}
.item-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 10rpx;
	margin-right: 20rpx;
	background-color: #f0f0f0;
}
.item-info {
	flex: 1;
}
.item-name {
	font-size: 28rpx;
	font-weight: bold;
}
.item-time {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.card-footer {
	display: flex;
	justify-content: flex-end;
	align-items: center;
	padding-top: 20rpx;
	border-top: 1px solid #f0f0f0;
	font-size: 28rpx;
}
.total-price {
	color: #e4393c;
	font-weight: bold;
	font-size: 32rpx;
	margin-right: 20rpx;
}
</style>