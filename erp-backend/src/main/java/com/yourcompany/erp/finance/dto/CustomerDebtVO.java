package com.yourcompany.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户欠款汇总 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDebtVO {

    private Long customerId;
    private String customerName;
    private Double creditLimit;  // 信用额度
    private Double totalDebt;    // 总欠款
    private Double availableCredit;  // 可用额度

}
