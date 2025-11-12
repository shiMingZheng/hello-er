package com.yourcompany.erp.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;  // 客户名称

    @Column(length = 50)
    private String contact;  // 联系人

    // --- 改造点 1：添加 email ---
    @Column(length = 100)
    private String email;  // 邮箱

    @Column(length = 20)
    private String phone;  // 电话

    @Column(length = 255)
    private String address;  // 地址

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerLevel level = CustomerLevel.NORMAL;  // 客户等级

    @Column(name = "credit_limit", nullable = false)
    private Double creditLimit = 0.0;  // 信用额度（简化版，固定值）

    @Column(nullable = false)
    private Double balance = 0.0;  // 当前欠款余额
	

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE; // 默认

    // --- 改造点 2：添加 remark ---
    @Column(length = 500)
    private String remark; // 备注

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 客户等级枚举
     */
    public enum CustomerLevel {
        NORMAL,  // 普通客户
        VIP      // VIP 客户
    }
	/**
     * 客户状态枚举
     */
    public enum CustomerStatus {
        ACTIVE,  // 正常
        INACTIVE // 禁用
    }

}
