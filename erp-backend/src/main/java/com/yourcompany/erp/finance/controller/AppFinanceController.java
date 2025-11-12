package com.yourcompany.erp.finance.controller;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.common.utils.UserContext;
import com.yourcompany.erp.finance.dto.CustomerDebtVO;
import com.yourcompany.erp.finance.dto.PaymentVO;
import com.yourcompany.erp.finance.dto.ReceivableVO;
import com.yourcompany.erp.finance.service.FinanceService;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/**
 * 客户端 - 财务查询
 */
@Slf4j
@Tag(name = "7. 客户端-财务查询", description = "客户查看自己的欠款、应收、收款记录（需要 Token）")
@RestController
@RequestMapping("/app/finance")
public class AppFinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前客户ID
     */
    private Long getCurrentCustomerId() {
        Long userId = UserContext.getUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getCustomerId() == null) {
            throw new BusinessException("该用户不是客户");
        }

        return user.getCustomerId();
    }

    /**
     * 获取我的欠款汇总
     */
	@Operation(summary = "获取我的欠款汇总", description = "查询当前客户的总欠款、信用额度、可用额度")
    @GetMapping("/debt/my")
    public Result<CustomerDebtVO> getMyDebt() {
        Long customerId = getCurrentCustomerId();
        CustomerDebtVO vo = financeService.getCustomerDebt(customerId);
        return Result.success(vo);
    }

    /**
     * 获取我的应收列表
     */
	@Operation(summary = "获取我的应收列表", description = "查询当前客户的所有应收账款")
    @GetMapping("/receivable/my")
    public Result<List<ReceivableVO>> getMyReceivables() {
        Long customerId = getCurrentCustomerId();
        List<ReceivableVO> receivables = financeService.getCustomerReceivables(customerId);
        return Result.success(receivables);
    }

    /**
     * 获取我的收款记录
     */
    @GetMapping("/payment/my")
	@Operation(summary = "获取我的收款记录", description = "查询当前客户的所有收款记录")
    public Result<List<PaymentVO>> getMyPayments() {
        Long customerId = getCurrentCustomerId();
        List<PaymentVO> payments = financeService.getPaymentList(customerId);
        return Result.success(payments);
    }

}
