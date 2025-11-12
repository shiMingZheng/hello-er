package com.yourcompany.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 月度对账单 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementVO {

    private Long customerId;
    private String customerName;
    private LocalDate startDate;
    private LocalDate endDate;

    // 期初余额
    private Double openingBalance = 0.0;

    // 本期发生额
    private Double periodSales = 0.0;      // 本期销售额（应收）
    private Double periodPayments = 0.0;   // 本期收款额

    // 期末余额
    private Double closingBalance = 0.0;

    // 明细
    private List<StatementDetail> details;

    /**
     * 对账单明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatementDetail {
        private LocalDate date;
        private String type;        // 类型：销售/收款
        private String refNo;       // 单号
        private Double debit;       // 借方（应收）
        private Double credit;      // 贷方（收款）
        private Double balance;     // 余额
    }

}
