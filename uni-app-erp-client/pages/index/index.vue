<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="header">
			<!-- 菜单按钮 -->
			<view class="menu-btn" @click="toggleDrawer">
				<text class="menu-icon">☰</text>
			</view>
			
			<text class="title">商品列表</text>
			
			<button @click="goToCart" size="mini" type="default">
				购物车 ({{ cartStore.totalCount }})
			</button>
		</view>

		<!-- 侧边栏抽屉（遮罩层） -->
		<view v-if="drawerVisible" class="drawer-mask" @click="toggleDrawer">
			<!-- 侧边栏内容 -->
			<view class="drawer" @click.stop>
				<view class="drawer-header">
					<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
					<text class="app-name">企业进销存</text>
				</view>
				
				<view class="drawer-menu">
					<view 
						v-for="item in menuItems" 
						:key="item.path"
						class="menu-item"
						:class="{ active: currentPath === item.path }"
						@click="navigateTo(item.path)"
					>
						<text class="menu-icon">{{ item.icon }}</text>
						<text class="menu-text">{{ item.name }}</text>
					</view>
				</view>
				
				<view class="drawer-footer">
					<text class="version">v1.0.0</text>
				</view>
			</view>
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
						<text class="product-price">¥ {{ getProductPrice(product) }}</text>
						<text v-if="isVipCustomer && product.normalPrice > product.vipPrice" class="save-tip">
							节省 ¥{{ (product.normalPrice - product.vipPrice).toFixed(2) }}
						</text>
					</view>
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

// 侧边栏状态
const drawerVisible = ref(false);
const currentPath = ref('/pages/index/index');

// 菜单项
const menuItems = ref([
	{ path: '/pages/index/index', name: '首页', icon: '🏠' },
	{ path: '/packageOrder/pages/list', name: '我的订单', icon: '📦' },
	{ path: '/pages/profile/profile', name: '个人中心', icon: '👤' },
]);

// 判断是否是 VIP 客户
const isVipCustomer = computed(() => {
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
 * 切换侧边栏
 */
const toggleDrawer = () => {
	drawerVisible.value = !drawerVisible.value;
};

/**
 * 导航到指定页面
 */
const navigateTo = (path) => {
	drawerVisible.value = false; // 关闭抽屉
	
	// 判断是否是 TabBar 页面
	const tabBarPages = ['/pages/index/index', '/packageOrder/pages/list', '/pages/profile/profile'];
	
	if (tabBarPages.includes(path)) {
		uni.switchTab({ url: path });
	} else {
		uni.navigateTo({ url: path });
	}
};

/**
 * 获取商品价格（根据客户等级）
 */
const getProductPrice = (product) => {
	if (product.price) {
		return product.price.toFixed(2);
	}
	
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
	const productJson = JSON.stringify(product);
	uni.navigateTo({
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
	padding-bottom: 20rpx;
	min-height: 100vh;
	background-color: #f5f5f5;
}

/* 顶部导航栏 */
.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx;
	background-color: #fff;
	border-bottom: 1px solid #f0f0f0;
	position: sticky;
	top: 0;
	z-index: 100;
}

.menu-btn {
	width: 80rpx;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.menu-icon {
	font-size: 40rpx;
	color: #333;
}

.title {
	flex: 1;
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	text-align: center;
}

/* 抽屉遮罩层 */
.drawer-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 999;
	animation: fadeIn 0.3s;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

/* 侧边栏 */
.drawer {
	position: absolute;
	left: 0;
	top: 0;
	bottom: 0;
	width: 500rpx;
	background-color: #fff;
	display: flex;
	flex-direction: column;
	animation: slideIn 0.3s;
}

@keyframes slideIn {
	from { transform: translateX(-100%); }
	to { transform: translateX(0); }
}

.drawer-header {
	padding: 60rpx 40rpx 40rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
}

.logo {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	margin-bottom: 20rpx;
	background-color: #fff;
}

.app-name {
	font-size: 32rpx;
	color: #fff;
	font-weight: bold;
}

.drawer-menu {
	flex: 1;
	padding: 20rpx 0;
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 30rpx 40rpx;
	transition: background-color 0.3s;
}

.menu-item.active {
	background-color: #f5f5f5;
}

.menu-item .menu-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.menu-item .menu-text {
	font-size: 30rpx;
	color: #333;
}

.drawer-footer {
	padding: 40rpx;
	text-align: center;
	border-top: 1px solid #f0f0f0;
}

.version {
	font-size: 24rpx;
	color: #999;
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
	padding: 20rpx;
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
