package com.yourcompany.erp.product.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.product.dto.ProductVO;
import com.yourcompany.erp.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * 客户端 - 商品浏览
 */
@Slf4j
@Tag(name = "3. 客户端-商品浏览", description = "客户浏览上架商品（需要 Token）")
@RestController
@RequestMapping("/app/product")
public class AppProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取上架商品列表
     */
    @GetMapping
	@Operation(summary = "获取上架商品列表", description = "查询所有已上架商品，价格根据客户等级显示")
    public Result<List<ProductVO>> getOnlineProducts() {
        List<ProductVO> products = productService.getOnlineProducts();
        return Result.success(products);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
	@Operation(summary = "获取商品详情", description = "根据 ID 查询商品详细信息")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        ProductVO vo = productService.getProduct(id);
        return Result.success(vo);
    }

}