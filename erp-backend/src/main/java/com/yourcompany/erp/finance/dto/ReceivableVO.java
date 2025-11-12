package com.yourcompany.erp.finance.dto;

import com.yourcompany.erp.finance.entity.Receivable;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应收账款响应 VO
 */
@Data
public class ReceivableVO {

    private Long id;
    private Long orderId;
    private String orderNo;  // 订单编号（冗余）
    private Long customerId;
    private String customerName;  // 客户名称（冗余）
    private Double amount;
    private Double paidAmount;
    private Double unpaidAmount;  // 未收金额（计算字段）
    private Receivable.ReceivableStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
