package com.yourcompany.erp.order.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.order.entity.Order;
import com.yourcompany.erp.order.dto.OrderVO;
import com.yourcompany.erp.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 管理端 - 订单管理
 */
@Slf4j
@Tag(name = "4. 管理端-订单管理", description = "订单审核、发货、状态管理（需要 Token）")
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取订单列表（分页）", description = "查询所有订单，支持按订单号和状态筛选")
    @Parameter(name = "page", description = "页码，从 0 开始")
    @Parameter(name = "size", description = "每页数量")
    @Parameter(name = "orderNo", description = "订单编号（模糊查询）")
    @Parameter(name = "status", description = "订单状态 (PENDING, APPROVED, SHIPPED, COMPLETED, CANCELLED)")
    public Result<Page<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // --- 改造点 3：接收搜索参数 ---
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {

        // --- 改造点 4：将参数传递给 Service ---
        Page<OrderVO> orders = orderService.getOrderList(orderNo, status, PageRequest.of(page, size));
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
	@Operation(summary = "获取订单详情", description = "根据 ID 查询订单详细信息（含明细）")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO vo = orderService.getOrderDetail(id);
        return Result.success(vo);
    }

    /**
     * 审核订单
     */
	@Operation(summary = "审核订单", description = "将待审核订单改为已审核状态")
    @PostMapping("/{id}/approve")
    public Result<Void> approveOrder(@PathVariable Long id) {
        log.info("审核订单: {}", id);
        orderService.approveOrder(id);
        return Result.success("订单审核成功", null);
    }

    /**
     * 发货
     */
	@Operation(summary = "订单发货", description = "将已审核订单改为已发货状态")
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id) {
        log.info("订单发货: {}", id);
        orderService.shipOrder(id);
        return Result.success("订单已发货", null);
    }

    /**
     * 完成订单
     */
	@Operation(summary = "完成订单", description = "将已发货订单改为已完成状态")
    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        log.info("完成订单: {}", id);
        orderService.completeOrder(id);
        return Result.success("订单已完成", null);
    }

}
