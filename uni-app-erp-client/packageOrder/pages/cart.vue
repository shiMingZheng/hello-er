<template>
	<view class="container">
		
		<view v-if="cartStore.totalCount === 0" class="empty-state">
			<text>购物车还是空的</text>
			<button @click="goHome" size="mini">去逛逛</button>
		</view>

		<view v-else class="cart-list">
			<view v-for="item in cartStore.items" :key="item.id" class="cart-item">
				<image class="item-image" :src="item.image" mode="aspectFill"></image>
				<view class="item-info">
					<text class="item-name">{{ item.name }}</text>
					<text class="item-price">¥ {{ item.price }}</text>
				</view>
				<view class="item-controls">
					<uni-number-box 
						:value="item.quantity" 
						:min="0"
						@change="(value) => onQuantityChange(item.id, value)" 
					/>
				</view>
			</view>
		</view>

		<view v-if="cartStore.totalCount > 0" class="checkout-bar">
			<view class="total-price">
				<text>合计：</text>
				<text class="price-value">¥ {{ cartStore.totalPrice }}</text>
			</view>
			<button class="checkout-btn" type="primary" @click="goToCheckout">
				去结算 ({{ cartStore.totalCount }})
			</button>
		</view>

	</view>
</template>

<script setup>
import { useCartStore } from '@/store/cart.js';

const cartStore = useCartStore();

// 数量变更时
const onQuantityChange = (id, value) => {
	// value 是 uni-number-box 返回的数字
	cartStore.updateQuantity(id, value);
};

// 去首页
const goHome = () => {
	uni.switchTab({
		url: '/pages/index/index'
	});
};

// 去结算
const goToCheckout = () => {
	// 下一步：跳转到真正的结算页
	uni.navigateTo({
		url: '/packageOrder/pages/checkout'
	});
};
</script>

<style>
.container {
	padding-bottom: 120rpx; /* 为结算栏留出空间 */
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

/* 列表 */
.cart-list {
	padding: 20rpx;
}
.cart-item {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background-color: #fff;
	border-radius: 10rpx;
	margin-bottom: 20rpx;
}
.item-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 10rpx;
	margin-right: 20rpx;
}
.item-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}
.item-name {
	font-size: 28rpx;
}
.item-price {
	font-size: 30rpx;
	color: #e4393c;
	margin-top: 10rpx;
}
.item-controls {
	/* 数量控制 */
}

/* 结算栏 */
.checkout-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 100rpx;
	background-color: #fff;
	border-top: 1px solid #eee;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20rpx;
}
.total-price {
	font-size: 32rpx;
}
.price-value {
	color: #e4393c;
	font-weight: bold;
}
.checkout-btn {
	height: 80rpx;
	line-height: 80rpx;
	margin: 0;
}
</style>