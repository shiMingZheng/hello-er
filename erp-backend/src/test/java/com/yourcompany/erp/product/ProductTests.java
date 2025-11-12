package com.yourcompany.erp.product;

import com.yourcompany.erp.product.dto.ProductDTO;
import com.yourcompany.erp.product.dto.ProductVO;
import com.yourcompany.erp.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 测试后自动回滚，不污染数据库
class ProductTests {

    @Autowired
    private ProductService productService;

    @Test
    void testCreateProduct() {
        // 准备数据
        ProductDTO dto = new ProductDTO();
        dto.setName("测试商品");
        dto.setCode("TEST001");
        dto.setNormalPrice(100.0);
        dto.setVipPrice(90.0);
        dto.setStock(100);
        dto.setDescription("测试描述");

        // 执行
        ProductVO vo = productService.createProduct(dto);

        // 断言
        assertNotNull(vo.getId());
        assertEquals("测试商品", vo.getName());
        assertEquals(100, vo.getStock());
        assertFalse(vo.getIsLowStock()); // 库存100，预警阈值10，应该不预警
    }

    @Test
    void testReduceStock() {
        // 先创建商品
        ProductDTO dto = new ProductDTO();
        dto.setName("库存测试商品");
        dto.setNormalPrice(50.0);
        dto.setVipPrice(45.0);
        dto.setStock(10);
        ProductVO product = productService.createProduct(dto);

        // 扣减库存
        productService.reduceStock(product.getId(), 5);

        // 验证
        ProductVO updated = productService.getProduct(product.getId());
        assertEquals(5, updated.getStock());
    }

}