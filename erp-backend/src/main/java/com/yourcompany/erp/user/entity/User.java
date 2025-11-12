package com.yourcompany.erp.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Entity
@Table(name = "\"user\"")  // PostgreSQL 中 user 是关键字，需要用双引号
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;  // ADMIN 或 CUSTOMER

    @Column(name = "customer_id")
    private Long customerId;  // 如果是客户，关联客户ID

    @Column(nullable = false)
    private Integer status = 1;  // 1=正常，0=禁用

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN,    // 管理员
        CUSTOMER  // 客户
    }

}
