package com.yourcompany.erp.customer.dto;

import com.yourcompany.erp.customer.entity.Customer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户响应 VO
 */
@Data
public class CustomerVO {

    private Long id;
    private String name;
    private String email;
    private String contact;
    private String phone;
    private String address;
    private Customer.CustomerLevel level;
    private Double creditLimit;
    private Double balance;  // 当前欠款
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
	private Customer.CustomerStatus status;

    private String remark;

    // 扩展字段
    private Double availableCredit;  // 可用信用额度

}
