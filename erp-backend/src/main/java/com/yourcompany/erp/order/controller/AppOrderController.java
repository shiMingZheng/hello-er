package com.yourcompany.erp.order.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.common.utils.UserContext;
import com.yourcompany.erp.order.dto.OrderDTO;
import com.yourcompany.erp.order.dto.OrderVO;
import com.yourcompany.erp.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 客户端 - 订单管理
 */
@Slf4j
@Tag(name = "5. 客户端-订单管理", description = "客户下单、查看订单（需要 Token）")
@RestController
@RequestMapping("/app/order")
public class AppOrderController {

    @Autowired
    private OrderService orderService;
	
	@Autowired  // <-- 增加这一行
	private UserService userService; // <-- 增加这一行

    /**
     * 创建订单
     */
    @PostMapping
	@Operation(summary = "创建订单", description = "客户下单，自动扣减库存、生成应收、检查信用额度")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderDTO dto) {
        // 从 Token 中获取当前客户ID
        Long userId = UserContext.getUserId();
        
        log.info("客户 {} 创建订单", userId);
        OrderVO vo = orderService.createOrder(dto);
        return Result.success(vo);
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/my")
	@Operation(summary = "获取我的订单列表", description = "查询当前客户的所有订单")
    public Result<List<OrderVO>> getMyOrders() {
        Long userId = UserContext.getUserId();
    
    // 从User表查询customerId
		User user = userService.findById(userId)
        .orElseThrow(() -> new BusinessException("用户不存在"));
    
		List<OrderVO> orders = orderService.getCustomerOrders(user.getCustomerId());
		return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
	@Operation(summary = "获取订单详情", description = "查询订单详细信息（含明细）")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO vo = orderService.getOrderDetail(id);
        return Result.success(vo);
    }

}
