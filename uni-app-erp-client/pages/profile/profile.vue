<template>
	<view class="content">
		<text class="title">我的 (主包)</text>
		
		<view class="info-box">
			<text>状态: {{ userStore.isLoggedIn ? '已登录' : '未登录' }}</text>
			<text>昵称: {{ userStore.userInfo.nickname }}</text>
			<text class="token">Token: {{ userStore.token || '无' }}</text>
		</view>
		
		<button @click="handleLogin" type="primary" class="action-btn">
			模拟登录
		</button>
		
		<button @click="handleLogout" class="action-btn">
			模拟登出
		</button>
	</view>
</template>

<script setup>
import { useUserStore } from '@/store/user.js'; // 引入 Store

// 1. 获取 store 实例
const userStore = useUserStore();

// 2. 定义方法, 调用 store.actions
const handleLogin = () => {
	const fakeToken = 'tk_' + Date.now();
	userStore.login(fakeToken);
};

const handleLogout = () => {
	userStore.logout();
};
</script>

<style>
.content {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx;
}
.title {
	font-size: 36rpx;
	margin-bottom: 40rpx;
}
.info-box {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	border: 1px solid #eee;
	padding: 20rpx;
	width: 90%;
}
.token {
	word-break: break-all;
}
.action-btn {
	margin-top: 30rpx;
	width: 90%;
}
</style>