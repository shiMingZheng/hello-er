package com.yourcompany.erp.customer.controller;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.common.utils.UserContext;
import com.yourcompany.erp.customer.dto.CustomerVO;
import com.yourcompany.erp.customer.service.CustomerService;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端 - 客户信息查询
 */
@Slf4j
@Tag(name = "8. 客户端-客户信息", description = "客户查看自己的信息（需要 Token）")
@RestController
@RequestMapping("/app/customer")
public class AppCustomerController {

    @Autowired
    private CustomerService customerService;

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
     * 获取我的客户信息
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的客户信息", description = "查询当前登录客户的详细信息")
    public Result<CustomerVO> getMyInfo() {
        Long customerId = getCurrentCustomerId();
        CustomerVO vo = customerService.getCustomer(customerId);
        return Result.success(vo);
    }

}
