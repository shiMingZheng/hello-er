package com.yourcompany.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应收账龄分析 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableAgeVO {

    private Long customerId;
    private String customerName;

    // 按账龄分组统计
    private Double within15Days = 0.0;   // 15天内
    private Double within30Days = 0.0;   // 16-30天
    private Double within60Days = 0.0;   // 31-60天
    private Double over60Days = 0.0;     // 60天以上

    private Double totalUnpaid = 0.0;    // 未收总额

    /**
     * 计算总额
     */
    public void calculateTotal() {
        this.totalUnpaid = within15Days + within30Days + within60Days + over60Days;
    }

}
