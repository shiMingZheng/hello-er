<template>
	<view class="content">
		<image class="detail-image" :src="product.image" mode="aspectFit"></image>
		
		<view class="info-box">
			<text class="detail-name">{{ product.name }}</text>
			<text class="detail-price">¥ {{ product.price }}</text>
		</view>

		<button @click="handleAddToCart" type="primary" class="add-cart-btn">
			加入购物车
		</button>
		
		<button @click="goToCart" type="default">
			查看购物车 ({{ cartStore.totalCount }})
		</button>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useCartStore } from '@/store/cart.js'; // 1. 导入 cart store

const cartStore = useCartStore(); // 2. 获取实例
const product = ref({}); // 存储从首页传来的商品对象

onLoad((options) => {
	if (options.product) {
		// 解码并解析商品对象
		try {
			const productJson = decodeURIComponent(options.product);
			product.value = JSON.parse(productJson);
		} catch (e) {
			console.error("解析商品数据失败", e);
			uni.showToast({ title: '商品加载失败', icon: 'error' });
			// 失败后返回
			uni.navigateBack();
		}
	} else {
		// 容错处理
		uni.showToast({ title: '商品ID缺失', icon: 'error' });
		uni.navigateBack();
	}
});

// 3. 定义“加入购物车”方法
const handleAddToCart = () => {
	// 调用 store.actions
	cartStore.addItem(product.value);
};

// 跳转购物车
const goToCart = () => {
	uni.navigateTo({
		url: '/packageOrder/pages/cart'
	});
};
</script>

<style>
.content {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx;
}
.detail-image {
	width: 100%;
	height: 400rpx;
	background-color: #f4f4f4;
	margin-bottom: 30rpx;
}
.info-box {
	width: 100%;
	margin-bottom: 50rpx;
}
.detail-name {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
}
.detail-price {
	font-size: 36rpx;
	color: #e4393c;
}
.add-cart-btn {
	width: 100%;
}
</style>