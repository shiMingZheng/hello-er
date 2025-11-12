<template>
	<view class="container">
		<text class="title">✅ 结算页 (分包)</text>
		
		<view class="checkout-summary section-box">
			<text class="summary-title">订单商品 (共 {{ cartStore.totalCount }} 件)</text>
			<view class="total-price">
				<text>订单总价：</text>
				<text class="price-value">¥ {{ cartStore.totalPrice }}</text>
			</view>
		</view>

		<view class="section-box">
			<text class="section-title">收货地址</text>
			<text class="section-content">张三, 138****1234, XX省XX市...</text>
			<text class="section-tip">(未来在此选择)</text>
		</view>

		<view class="section-box">
			<text class="section-title">支付方式</text>
			<text class="section-content">在线支付</text>
			<text class="section-tip">(未来在此选择)</text>
		</view>

		<view class="footer-bar">
			<view class="footer-price">
				<text>总计：</text>
				<text class="price-value">¥ {{ cartStore.totalPrice }}</text>
			</view>
			<button class="submit-btn" type="primary" @click="submitOrder">
				提交订单
			</button>
		</view>
	</view>
</template>

<script setup>
import { useCartStore } from '@/store/cart.js';
import { useUserStore } from '@/store/user.js';
import { useOrderStore } from '@/store/order.js'; // 1. [新增] 导入 order store
import { onShow } from '@dcloudio/uni-app';
const cartStore = useCartStore();
const userStore = useUserStore();
const orderStore = useOrderStore(); // 2. [新增] 获取 order store 实例

onShow(() => {
	// (onShow 逻辑保持不变)
	if (cartStore.totalCount === 0) {
		uni.showToast({
			title: '购物车已空，无需结算',
			icon: 'none',
			success: () => {
				setTimeout(() => { uni.navigateBack(); }, 1500);
			}
		});
	}
});

/**
 * 提交订单
 */
const submitOrder = () => {
	console.log("提交订单...");
	
	// 1. 检查登录 (保持不变)
	if (!userStore.isLoggedIn) {
		uni.showToast({ /* ... */ });
		return;
	}

	// 2. 准备订单数据 (保持不变)
	const orderData = {
		items: cartStore.items, // 直接使用 Pinia 的 items
		totalPrice: cartStore.totalPrice,
		address: "模拟的收货地址"
	};
	console.log("订单数据:", orderData);

	// 3. [修改] 真正创建订单 (不再是模拟)
	try {
		// 调用 order store 的 action
		orderStore.createOrder(orderData);
		
		uni.showToast({
			title: '订单提交成功！',
			icon: 'success',
			duration: 1500
		});

		// 4. 提交成功后清空购物车 (保持不变)
		cartStore.clearCart();

		// 5. 跳转到订单列表页 (保持不变)
		setTimeout(() => {
			uni.switchTab({
				url: '/packageOrder/pages/list'
			});
		}, 1500);

	} catch (e) {
		console.error("创建订单失败", e);
		uni.showToast({
			title: '提交失败，请重试',
			icon: 'error'
		});
	}
};
</script>

<style>
.container {
	padding: 20rpx;
	/* 为底部提交按钮留出空间 */
	padding-bottom: 140rpx; 
}
.title {
	font-size: 36rpx;
	font-weight: bold;
	margin-bottom: 30rpx;
	color: green;
}
.section-box {
	background-color: #fff;
	border-radius: 10rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}
.section-title {
	font-size: 30rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}
.section-content {
	font-size: 28rpx;
	color: #333;
}
.section-tip {
	font-size: 26rpx;
	color: #999;
	margin-left: 20rpx;
}
.total-price {
	display: flex;
	justify-content: space-between;
	margin-top: 20rpx;
	font-size: 28rpx;
}
.price-value {
	color: #e4393c;
	font-weight: bold;
	font-size: 32rpx;
}

/* 底部提交栏 */
.footer-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 120rpx;
	background-color: #fff;
	border-top: 1px solid #eee;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20rpx;
	box-sizing: border-box; /* 确保 padding 不会撑开宽度 */
}
.footer-price {
	flex: 1;
	font-size: 30rpx;
}
.submit-btn {
	width: 240rpx;
	height: 80rpx;
	line-height: 80rpx;
	margin: 0;
}
</style>