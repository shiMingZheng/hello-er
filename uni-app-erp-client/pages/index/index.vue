<template>
	<view class="container">
		<view class="header">
			<text class="title">模拟商品列表 (首页)</text>
			<button @click="goToCart" size="mini" type="default">
				查看购物车 ({{ cartStore.totalCount }})
			</button>
		</view>

		<view class="product-list">
			<view v-for="product in mockProducts" :key="product.id" class="product-card" @click="goToDetail(product)">
				<image class="product-image" :src="product.image" mode="aspectFill"></image>
				<text class="product-name">{{ product.name }}</text>
				<text class="product-price">¥ {{ product.price }}</text>
			</view>
		</view>

	</view>
</template>

<script setup>
import { ref } from 'vue';
import { useCartStore } from '@/store/cart.js'; // 1. 导入 cart store

const cartStore = useCartStore(); // 2. 获取实例

// 模拟的商品数据
const mockProducts = ref([
	{ id: '123', name: '高档A4打印纸', price: 29.90, image: '/static/logo.png' },
	{ id: '124', name: '得力订书机', price: 15.50, image: '/static/logo.png' },
	{ id: '125', name: '晨光签字笔 (10支装)', price: 9.90, image: '/static/logo.png' },
]);

// 跳转详情页 (改为传递整个 product 对象)
const goToDetail = (product) => {
	// uni-app 导航无法传递复杂对象, 先转为 JSON 字符串
	const productJson = JSON.stringify(product);
	
	uni.navigateTo({
		// 使用 encodeURIComponent 编码 JSON 字符串
		url: `/packageProduct/pages/detail?product=${encodeURIComponent(productJson)}`
	});
};

// 跳转购物车
const goToCart = () => {
	uni.navigateTo({
		url: '/packageOrder/pages/cart'
	});
};
</script>

<style>
.container {
	padding: 20rpx;
}
.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}
.title {
	font-size: 36rpx;
	font-weight: bold;
}
.product-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}
.product-card {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background-color: #fff;
	border: 1px solid #eee;
	border-radius: 10rpx;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.product-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 10rpx;
	margin-right: 20rpx;
	background-color: #f0f0f0; /* 占位符颜色 */
}
.product-name {
	font-size: 30rpx;
	flex: 1;
}
.product-price {
	font-size: 32rpx;
	color: #e4393c;
	font-weight: bold;
}
</style>