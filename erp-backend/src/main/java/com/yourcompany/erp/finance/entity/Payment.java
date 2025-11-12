package com.yourcompany.erp.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 收款记录表（方案二：支持一笔收款核销多个订单）
 */
@Data
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, unique = true, length = 50)
    private String paymentNo;  // 收款编号

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // 客户ID

    @Column(nullable = false)
    private Double amount;  // 收款总金额

    @Column(name = "allocated_amount", nullable = false)
    private Double allocatedAmount = 0.0;  // 已分配金额（用于部分核销）

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;  // 支付方式（现金、转账、支票等）

    @Column(length = 500)
    private String remark;  // 备注

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

}
