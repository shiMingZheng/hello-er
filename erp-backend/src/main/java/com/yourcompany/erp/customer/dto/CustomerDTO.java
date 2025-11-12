package com.yourcompany.erp.customer.dto;

import com.yourcompany.erp.customer.entity.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 客户请求 DTO
 */
@Data
public class CustomerDTO {

    @NotBlank(message = "客户名称不能为空")
    private String name;

    private String contact;  // 联系人

    private String phone;  // 电话

    // --- 改造点 1：添加 email ---
    private String email;  // 邮箱

    private String address;  // 地址

    private Customer.CustomerLevel level;  // 客户等级

    @Positive(message = "信用额度必须大于0")
    private Double creditLimit;  // 信用额度
	private Customer.CustomerStatus status; // 状态

    // --- 改造点 2：添加 remark ---
    private String remark; // 备注

}
