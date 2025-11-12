package com.yourcompany.erp.finance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批量核销请求 DTO（方案二：一笔收款核销多个订单）
 */
@Data
public class PaymentBatchDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "收款总金额不能为空")
    @Positive(message = "收款金额必须大于0")
    private Double totalAmount;

    private String paymentMethod;  // 支付方式

    private String remark;  // 备注

    @NotEmpty(message = "核销明细不能为空")
    private List<AllocationItem> allocations;  // 核销明细

    /**
     * 核销明细项
     */
    @Data
    public static class AllocationItem {

        @NotNull(message = "应收账款ID不能为空")
        private Long receivableId;

        @NotNull(message = "核销金额不能为空")
        @Positive(message = "核销金额必须大于0")
        private Double amount;
    }

}
