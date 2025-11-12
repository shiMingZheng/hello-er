package com.yourcompany.erp.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品响应 VO
 */
@Data
public class ProductVO {
    
    private Long id;
    private String name;
    private String code;
    private Double normalPrice;
    private Double vipPrice;
    private Integer stock;
    private Integer stockWarning;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private Boolean isLowStock;  // 是否库存预警
    
}