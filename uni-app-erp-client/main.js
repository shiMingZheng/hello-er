// [修正后] uni-app-erp-client/main.js
import { createSSRApp } from "vue";
import * as Pinia from 'pinia';
import App from './App.vue'; // <-- [关键] 必须导入 App.vue

export function createApp() {
	const app = createSSRApp(App);
	app.use(Pinia.createPinia());
	return {
		app,
		Pinia,
	};
}