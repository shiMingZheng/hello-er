package com.yourcompany.erp.product.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.product.dto.ProductDTO;
import com.yourcompany.erp.product.dto.ProductVO;
import com.yourcompany.erp.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * 管理端 - 商品管理
 */
@Tag(name = "2. 管理端-商品管理", description = "商品增删改查、库存管理（需要 Token）")
@Slf4j
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 创建商品
     */
    @PostMapping
	@Operation(summary = "创建商品", description = "新增商品，支持普通价和 VIP 价")
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductDTO dto) {
        log.info("创建商品: {}", dto.getName());
        ProductVO vo = productService.createProduct(dto);
        return Result.success(vo);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
	@Operation(summary = "更新商品", description = "修改商品信息")
    public Result<ProductVO> updateProduct(@PathVariable Long id, 
                                          @Valid @RequestBody ProductDTO dto) {
        log.info("更新商品: {}", id);
        ProductVO vo = productService.updateProduct(id, dto);
        return Result.success(vo);
    }

    /**
     * 删除商品（下架）
     */
    @DeleteMapping("/{id}")
	@Operation(summary = "删除商品（下架）", description = "软删除，将商品状态改为下架")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: {}", id);
        productService.deleteProduct(id);
        return Result.success();
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
	@Operation(summary = "获取商品详情", description = "根据 ID 查询商品信息")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        ProductVO vo = productService.getProduct(id);
        return Result.success(vo);
    }

    /**
     * 获取商品列表（分页）
     */
    @GetMapping
	@Operation(summary = "获取商品列表（分页）", description = "查询所有商品，支持分页")
	@Parameter(name = "page", description = "页码，从 0 开始")
	@Parameter(name = "size", description = "每页数量")
	@Parameter(name = "keyword", description = "关键字（名称或编码）")
    @Parameter(name = "status", description = "状态（1=上架，0=下架）")
    public Result<Page<ProductVO>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<ProductVO> products = productService.getProductList(keyword, status, PageRequest.of(page, size));
		return Result.success(products);
    }

    /**
     * 获取库存预警商品
     */
    @GetMapping("/low-stock")
	@Operation(summary = "获取库存预警商品", description = "查询库存低于预警阈值的商品")
    public Result<List<ProductVO>> getLowStockProducts() {
        List<ProductVO> products = productService.getLowStockProducts();
        return Result.success(products);
    }

    /**
     * 手动调整库存
     */
    @PostMapping("/{id}/stock")
	@Operation(summary = "手动调整库存", description = "增加或减少商品库存（正数增加，负数减少）")
	@Parameter(name = "id", description = "商品 ID")
	@Parameter(name = "quantity", description = "调整数量（可为负数）")
    public Result<Void> adjustStock(@PathVariable Long id,
                                    @RequestParam Integer quantity) {
        log.info("调整商品 {} 库存: {}", id, quantity);
        if (quantity > 0) {
            productService.addStock(id, quantity);
        } else if (quantity < 0) {
            productService.reduceStock(id, Math.abs(quantity));
        }
        return Result.success("库存调整成功", null);
    }

}