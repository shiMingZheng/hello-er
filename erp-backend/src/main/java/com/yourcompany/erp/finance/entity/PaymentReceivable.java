package com.yourcompany.erp.finance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 收款核销关联表（方案二：支持一笔收款核销多个订单）
 */
@Data
@Entity
@Table(name = "payment_receivable")
public class PaymentReceivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;  // 收款记录ID

    @Column(name = "receivable_id", nullable = false)
    private Long receivableId;  // 应收账款ID

    @Column(nullable = false)
    private Double amount;  // 本次核销金额

}
