package com.yourcompany.erp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 商品请求 DTO
 */
@Data
public class ProductDTO {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String code;  // 商品编码（可选）

    @NotNull(message = "普通价格不能为空")
    @Positive(message = "普通价格必须大于0")
    private Double normalPrice;

    @NotNull(message = "VIP价格不能为空")
    @Positive(message = "VIP价格必须大于0")
    private Double vipPrice;

    @NotNull(message = "库存数量不能为空")
    private Integer stock;

    private Integer stockWarning = 10;  // 默认预警阈值

    private String description;

}