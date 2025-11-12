package com.yourcompany.erp.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 收款请求 DTO
 */
@Data
public class PaymentDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "应收账款ID不能为空")
    private Long receivableId;

    @NotNull(message = "收款金额不能为空")
    @Positive(message = "收款金额必须大于0")
    private Double amount;

    private String paymentMethod;  // 支付方式

    private String remark;  // 备注

}
