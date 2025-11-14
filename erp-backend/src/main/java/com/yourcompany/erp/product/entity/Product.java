package com.yourcompany.erp.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;  // 商品名称
	
	@Column(length = 255)
	private String image;  // 商品图片 URL

    @Column(length = 50)
    private String code;  // 商品编码（如条形码）

    @Column(name = "normal_price", nullable = false)
    private Double normalPrice;  // 普通客户价格

    @Column(name = "vip_price", nullable = false)
    private Double vipPrice;  // VIP 客户价格

    @Column(nullable = false)
    private Integer stock = 0;  // 库存数量

    @Column(name = "stock_warning")
    private Integer stockWarning = 10;  // 库存预警阈值

    @Column(nullable = false)
    private Integer status = 1;  // 1=上架，0=下架

    @Column(length = 500)
    private String description;  // 商品描述

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}