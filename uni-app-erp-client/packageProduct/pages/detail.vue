<template>
	<view class="content">
		<image class="detail-image" :src="product.image || '/static/logo.png'" mode="aspectFit"></image>
		
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
import { useUserStore } from '@/store/user.js'; // 新增这行
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
	// 【新增】检查登录状态
		if (!userStore.isLoggedIn) {
			uni.showModal({
				title: '提示',
				content: '请先登录后再加入购物车',
				confirmText: '去登录',
				cancelText: '取消',
				success: (res) => {
					if (res.confirm) {
						// 跳转登录页
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}
				}
			});
			return;
		}
	// 调用 store.actions，传递完整的商品对象
	const cartItem = {
		id: product.value.id,
		name: product.value.name,
		price: getProductPrice(), // 根据客户等级获取价格
		image: product.value.image || '/static/logo.png', // 图片（默认值）
		productId: product.value.id // 保留原始商品ID
	};
	
	cartStore.addItem(cartItem);
};

// 新增：获取当前客户应该看到的价格
const getProductPrice = () => {
	// 从 userStore 获取客户等级
	const userStore = useUserStore();
	const isVip = userStore.userInfo?.customerLevel === 'VIP';
	
	// 后端返回的商品对象可能包含 normalPrice 和 vipPrice
	if (isVip && product.value.vipPrice) {
		return product.value.vipPrice;
	}
	return product.value.normalPrice || product.value.price || 0;
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