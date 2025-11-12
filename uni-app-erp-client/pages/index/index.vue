<template>
	<view class="container">
		<view class="header">
			<text class="title">商品列表</text>
			<button @click="goToCart" size="mini" type="default">
				查看购物车 ({{ cartStore.totalCount }})
			</button>
		</view>

		<!-- 加载状态 -->
		<view v-if="loading" class="loading-box">
			<text>加载中...</text>
		</view>

		<!-- 空状态 -->
		<view v-else-if="products.length === 0" class="empty-state">
			<text>暂无商品</text>
		</view>

		<!-- 商品列表 -->
		<view v-else class="product-list">
			<view 
				v-for="product in products" 
				:key="product.id" 
				class="product-card" 
				@click="goToDetail(product)"
			>
				<image 
					class="product-image" 
					:src="product.image || '/static/logo.png'" 
					mode="aspectFill"
				></image>
				<view class="product-info">
					<text class="product-name">{{ product.name }}</text>
					<view class="price-box">
						<!-- 显示根据客户等级的价格 -->
						<text class="product-price">¥ {{ getProductPrice(product) }}</text>
						<!-- 如果是 VIP，显示节省金额 -->
						<text v-if="isVipCustomer && product.normalPrice > product.vipPrice" class="save-tip">
							节省 ¥{{ (product.normalPrice - product.vipPrice).toFixed(2) }}
						</text>
					</view>
					<!-- 库存提示 -->
					<text class="stock-info" :class="{ 'low-stock': product.stock < 10 }">
						库存: {{ product.stock }}
					</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useCartStore } from '@/store/cart.js';
import { useUserStore } from '@/store/user.js';
import { getProductList } from '@/api/product';

const cartStore = useCartStore();
const userStore = useUserStore();

// 商品列表
const products = ref([]);
const loading = ref(false);

// 判断是否是 VIP 客户
const isVipCustomer = computed(() => {
	// 从 userInfo 中获取客户等级（后端会返回）
	// 如果 userInfo.customerLevel 是 'VIP'，则返回 true
	return userStore.userInfo?.customerLevel === 'VIP';
});

/**
 * 页面加载时获取商品列表
 */
onLoad(() => {
	loadProducts();
});

/**
 * 加载商品列表
 */
const loadProducts = async () => {
	loading.value = true;
	try {
		const list = await getProductList();
		products.value = list;
		console.log('✅ 商品列表加载成功:', list);
	} catch (e) {
		console.error('❌ 加载商品失败:', e);
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

/**
 * 获取商品价格（根据客户等级）
 * 注意：后端 /app/product 接口已经自动根据客户等级返回对应价格
 * 这里我们直接使用后端返回的 price 字段
 */
const getProductPrice = (product) => {
	// 方案一：如果后端返回了统一的 price 字段
	if (product.price) {
		return product.price.toFixed(2);
	}
	
	// 方案二：如果后端返回了 normalPrice 和 vipPrice，前端自己判断
	if (isVipCustomer.value) {
		return product.vipPrice.toFixed(2);
	} else {
		return product.normalPrice.toFixed(2);
	}
};

/**
 * 跳转商品详情页
 */
const goToDetail = (product) => {
	// uni-app 导航无法传递复杂对象，先转为 JSON 字符串
	const productJson = JSON.stringify(product);
	
	uni.navigateTo({
		// 使用 encodeURIComponent 编码 JSON 字符串
		url: `/packageProduct/pages/detail?product=${encodeURIComponent(productJson)}`
	});
};

/**
 * 跳转购物车
 */
const goToCart = () => {
	uni.navigateTo({
		url: '/packageOrder/pages/cart'
	});
};
</script>

<style scoped>
.container {
	padding: 20rpx;
	min-height: 100vh;
	background-color: #f5f5f5;
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding: 20rpx;
	background-color: #fff;
	border-radius: 10rpx;
}

.title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

/* 加载状态 */
.loading-box {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 200rpx 0;
	color: #999;
	font-size: 28rpx;
}

/* 空状态 */
.empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 200rpx 0;
	color: #999;
	font-size: 28rpx;
}

/* 商品列表 */
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
	border-radius: 10rpx;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
	transition: all 0.3s;
}

.product-card:active {
	transform: scale(0.98);
	box-shadow: 0 1rpx 5rpx rgba(0,0,0,0.1);
}

.product-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 10rpx;
	margin-right: 20rpx;
	background-color: #f0f0f0;
	flex-shrink: 0;
}

.product-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.product-name {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.price-box {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
}

.product-price {
	font-size: 32rpx;
	color: #e4393c;
	font-weight: bold;
	margin-right: 10rpx;
}

.save-tip {
	font-size: 22rpx;
	color: #ff6600;
	background-color: #fff3e0;
	padding: 2rpx 10rpx;
	border-radius: 5rpx;
}

.stock-info {
	font-size: 24rpx;
	color: #666;
}

.stock-info.low-stock {
	color: #ff6600;
	font-weight: bold;
}
</style>