import request from '@/utils/request';

/**
 * 模拟获取商品详情
 * @param {string} id 
 */
export function getProductDetail(id) {
    // 实际调用
	// return request({
	// 	url: `/product/detail?id=${id}`,
	// 	method: 'GET'
	// });
    
    // --- 阶段一：返回一个假的 Promise (模拟数据) ---
    console.log(`API: 准备获取商品 ${id} (模拟)`);
    return new Promise((resolve) => {
        setTimeout(() => {
            console.log("API: 模拟数据返回");
            resolve({
                id: id,
                name: "模拟商品名称",
                price: 99.99
            });
        }, 500); // 模拟 500ms 网络延迟
    });
}