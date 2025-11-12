package com.yourcompany.erp.finance.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收款记录响应 VO
 */
@Data
public class PaymentVO {

    private Long id;
    private String paymentNo;
    private Long customerId;
    private String customerName;  // 客户名称（冗余）
    private Long receivableId;
    private String orderNo;  // 订单编号（冗余）
    private Double amount;
    private String paymentMethod;
    private String remark;
    private LocalDateTime createTime;

}
