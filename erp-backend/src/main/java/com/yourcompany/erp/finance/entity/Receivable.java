package com.yourcompany.erp.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 应收账款表
 */
@Data
@Entity
@Table(name = "receivable")
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;  // 订单ID

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // 客户ID

    @Column(nullable = false)
    private Double amount;  // 应收金额

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0.0;  // 已收金额

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReceivableStatus status = ReceivableStatus.UNPAID;  // 状态

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 应收状态枚举
     */
    public enum ReceivableStatus {
        UNPAID,     // 未收款
        PARTIAL,    // 部分收款
        PAID        // 已收款
    }

}
