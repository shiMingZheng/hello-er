<script>
	/**
	 * App.vue 是 uni-app 的主组件
	 * 1. 它用于监听应用的生命周期 (onLaunch, onShow, onHide)
	 * 2. 它用于配置全局样式 (style 标签)
	 * 3. 它本身没有 <template> 模板，因为它不负责渲染页面
	 */
	export default {
		onLaunch: function() {
			console.log('✅ App Launch: 应用启动成功...');
			// 在这里可以进行应用的初始化，例如检查登录状态、检查更新等
		},
		onShow: function() {
			console.log('App Show: 应用展示到前台');
		},
		onHide: function() {
			console.log('App Hide: 应用进入后台');
		}
	}
</script>

<style>
	/* [关键] 这里是全局样式 (Global CSS / App.vue Style) */
	/* 在这里定义的样式，会应用到项目的每一个页面 */
	
	/* 示例：设置一个全局的盒子模型，让布局更可控 */
	view,
	text,
	image,
	button {
		box-sizing: border-box;
	}
	
	/* 示例：定义一个全局的主题色类名 */
	.theme-color {
		color: #007AFF;
	}

	/* 示例：为所有按钮设置一个基础的上下边距 */
	button {
		margin-top: 10rpx;
		margin-bottom: 10rpx;
	}
</style>