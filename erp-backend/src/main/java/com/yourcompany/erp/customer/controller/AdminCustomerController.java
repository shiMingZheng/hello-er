package com.yourcompany.erp.customer.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.customer.dto.CustomerDTO;
import com.yourcompany.erp.customer.dto.CustomerVO;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 客户管理
 */
@Slf4j
@Tag(name = "1. 管理端-客户管理", description = "客户增删改查、等级管理（需要 Token）")
@RestController
@RequestMapping("/admin/customer")
public class AdminCustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 创建客户
     */
    @PostMapping
    @Operation(summary = "创建客户", description = "新增客户信息")
    public Result<CustomerVO> createCustomer(@Valid @RequestBody CustomerDTO dto) {
        log.info("创建客户: {}", dto.getName());
        CustomerVO vo = customerService.createCustomer(dto);
        return Result.success(vo);
    }

    /**
     * 更新客户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新客户", description = "修改客户信息")
    public Result<CustomerVO> updateCustomer(@PathVariable Long id,
                                            @Valid @RequestBody CustomerDTO dto) {
        log.info("更新客户: {}", id);
        CustomerVO vo = customerService.updateCustomer(id, dto);
        return Result.success(vo);
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户", description = "删除客户（有欠款无法删除）")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        log.info("删除客户: {}", id);
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情", description = "根据 ID 查询客户信息")
    public Result<CustomerVO> getCustomer(@PathVariable Long id) {
        CustomerVO vo = customerService.getCustomer(id);
        return Result.success(vo);
    }

    /**
     * 获取客户列表（分页 + 高级筛选）
     */
    @GetMapping
    @Operation(summary = "获取客户列表（分页）", description = "支持按名称、电话、等级筛选")
    @Parameter(name = "page", description = "页码，从 0 开始")
    @Parameter(name = "size", description = "每页数量")
    @Parameter(name = "customerName", description = "客户名称（模糊查询）")
    @Parameter(name = "phone", description = "电话号码")
    @Parameter(name = "level", description = "客户等级（NORMAL/VIP）")
    @Parameter(name = "status", description = "客户状态（ACTIVE/INACTIVE）")
    public Result<Page<CustomerVO>> getCustomerList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Customer.CustomerLevel level,
            @RequestParam(required = false) String status) { // 接收 String

        log.info("查询客户列表 - 页码: {}, 名称: {}, 电话: {}, 等级: {}, 状态: {}",
                page, customerName, phone, level, status); // 改造点

        Page<CustomerVO> customers = customerService.getCustomerList(
                customerName, phone, level, status, PageRequest.of(page, size)); // 改造点
        // --- 改造点结束 ---

        return Result.success(customers);
    }

    /**
     * 启用/禁用客户
     */
    @PostMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用客户", description = "切换客户的激活状态")
    public Result<CustomerVO> toggleCustomerStatus(@PathVariable Long id) {
        log.info("切换客户 {} 状态", id);
        CustomerVO vo = customerService.toggleCustomerStatus(id);
        return Result.success(vo);
    }

    /**
     * 获取所有客户（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有客户", description = "不分页，用于下拉选择")
    public Result<List<CustomerVO>> getAllCustomers() {
        List<CustomerVO> customers = customerService.getAllCustomers();
        return Result.success(customers);
    }

    /**
     * 根据等级查询客户
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "根据等级查询客户", description = "查询指定等级的所有客户")
    public Result<List<CustomerVO>> getCustomersByLevel(@PathVariable Customer.CustomerLevel level) {
        List<CustomerVO> customers = customerService.getCustomersByLevel(level);
        return Result.success(customers);
    }

}
