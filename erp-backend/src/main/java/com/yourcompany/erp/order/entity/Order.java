package com.yourcompany.erp.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 订单主表
 */
@Data
@Entity
@Table(name = "\"order\"")  // PostgreSQL 中 order 是关键字
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;  // 订单编号

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // 客户ID

    @Column(name = "customer_name", length = 100)
    private String customerName;  // 客户名称（冗余，方便查询）

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;  // 订单总金额

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;  // 订单状态

    @Column(length = 500)
    private String remark;  // 备注

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "approved_time")
    private LocalDateTime approvedTime;  // 审核时间

    @Column(name = "shipped_time")
    private LocalDateTime shippedTime;  // 发货时间

    @Column(name = "completed_time")
    private LocalDateTime completedTime;  // 完成时间

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING,    // 待审核
        APPROVED,   // 已审核
        SHIPPED,    // 已发货
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }

}
