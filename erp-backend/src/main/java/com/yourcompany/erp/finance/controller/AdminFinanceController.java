package com.yourcompany.erp.finance.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.finance.dto.*;
import com.yourcompany.erp.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/**
 * 管理端 - 财务管理（方案二增强版）
 */
@Slf4j
@Tag(name = "6. 管理端-财务管理", description = "应收管理、收款录入、账龄分析、对账单（需要 Token）")
@RestController
@RequestMapping("/admin/finance")
public class AdminFinanceController {

    @Autowired
    private FinanceService financeService;

    /**
     * 获取应收账款列表（分页）
     */
    @GetMapping("/receivable")
	@Operation(summary = "获取应收账款列表（分页）", description = "查询所有应收账款，支持分页")
	@Parameter(name = "page", description = "页码，从 0 开始")
	@Parameter(name = "size", description = "每页数量")
    @Parameter(name = "customerId", description = "客户 ID")
    @Parameter(name = "status", description = "收款状态 (UNPAID, PARTIAL, PAID)")
    public Result<Page<ReceivableVO>> getReceivableList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // --- 改造点 2：接收搜索参数 ---
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {

        // --- 改造点 3：将参数传递给 Service ---
        Page<ReceivableVO> receivables = financeService.getReceivableList(
                customerId, status, PageRequest.of(page, size));
        return Result.success(receivables);
    }

    /**
     * 获取客户应收列表
     */
	@Operation(summary = "获取客户应收列表", description = "查询指定客户的所有应收账款")
    @GetMapping("/receivable/customer/{customerId}")
    public Result<List<ReceivableVO>> getCustomerReceivables(@PathVariable Long customerId) {
        List<ReceivableVO> receivables = financeService.getCustomerReceivables(customerId);
        return Result.success(receivables);
    }

    /**
     * 录入收款（简单核销）
     */
	@Operation(summary = "录入收款（简单核销）", description = "一笔收款核销一个订单")
    @PostMapping("/payment")
    public Result<PaymentVO> recordPayment(@Valid @RequestBody PaymentDTO dto) {
        log.info("录入收款，客户: {}, 金额: {}", dto.getCustomerId(), dto.getAmount());
        PaymentVO vo = financeService.recordPayment(dto);
        return Result.success(vo);
    }

    /**
     * 批量核销（一笔收款核销多个订单）
     */
    @PostMapping("/payment/batch")
	@Operation(summary = "批量核销（推荐）", description = "一笔收款核销多个订单，支持部分核销")
    public Result<PaymentVO> recordBatchPayment(@Valid @RequestBody PaymentBatchDTO dto) {
        log.info("批量核销，客户: {}, 收款金额: {}, 核销 {} 笔",
                dto.getCustomerId(), dto.getTotalAmount(), dto.getAllocations().size());
        PaymentVO vo = financeService.recordBatchPayment(dto);
        return Result.success(vo);
    }

    /**
     * 获取收款记录列表
     */
	@Operation(summary = "获取收款记录列表", description = "查询指定客户的所有收款记录")
    @GetMapping("/payment/customer/{customerId}")
    public Result<List<PaymentVO>> getPaymentList(@PathVariable Long customerId) {
        List<PaymentVO> payments = financeService.getPaymentList(customerId);
        return Result.success(payments);
    }

    /**
     * 获取客户欠款汇总
     */
    @GetMapping("/debt/customer/{customerId}")
	@Operation(summary = "获取客户欠款汇总", description = "查询客户总欠款、信用额度、可用额度")
    public Result<CustomerDebtVO> getCustomerDebt(@PathVariable Long customerId) {
        CustomerDebtVO vo = financeService.getCustomerDebt(customerId);
        return Result.success(vo);
    }

    /**
     * 账龄分析
     */
    @GetMapping("/age/customer/{customerId}")
	@Operation(summary = "账龄分析", description = "分析客户应收账款的账龄分布（15天、30天、60天、60天以上）")
    public Result<ReceivableAgeVO> analyzeReceivableAge(@PathVariable Long customerId) {
        log.info("查询客户 {} 账龄分析", customerId);
        ReceivableAgeVO vo = financeService.analyzeReceivableAge(customerId);
        return Result.success(vo);
    }

    /**
     * 生成月度对账单
     */
    @GetMapping("/statement/customer/{customerId}")
	@Operation(summary = "生成月度对账单", description = "生成指定月份的客户对账单，含期初、期末余额")
	@Parameter(name = "customerId", description = "客户 ID")
	@Parameter(name = "year", description = "年份（如 2025）")
	@Parameter(name = "month", description = "月份（1-12）")
    public Result<AccountStatementVO> generateMonthlyStatement(
            @PathVariable Long customerId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("生成客户 {} 的 {}-{} 对账单", customerId, year, month);
        AccountStatementVO vo = financeService.generateMonthlyStatement(customerId, year, month);
        return Result.success(vo);
    }

}
