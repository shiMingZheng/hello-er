package com.yourcompany.erp.order.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 订单明细表
 */
@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;  // 订单ID

    @Column(name = "product_id", nullable = false)
    private Long productId;  // 商品ID

    @Column(name = "product_name", length = 100)
    private String productName;  // 商品名称（冗余）

    @Column(nullable = false)
    private Double price;  // 成交单价（根据客户等级确定）

    @Column(nullable = false)
    private Integer quantity;  // 数量

    @Column(nullable = false)
    private Double subtotal;  // 小计（price * quantity）

}
