package com.yourcompany.erp.auth.controller;

import com.yourcompany.erp.common.response.Result;
import com.yourcompany.erp.common.utils.PasswordUtils;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 临时初始化接口（仅用于开发环境）
 * 生产环境请删除此文件
 */
@Tag(name = "0. 系统初始化", description = "仅用于开发环境初始化，生产环境请删除")
@Slf4j
@RestController
@RequestMapping("/init")
public class InitController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUtils passwordUtils;

    /**
     * 初始化管理员账号
     * POST http://localhost:8080/init/admin
     */
	@Operation(summary = "初始化管理员账号", description = "创建默认管理员账号（用户名: admin, 密码: admin123）")
    @PostMapping("/admin")
    public Result<Void> initAdmin() {
        // 检查是否已存在
        if (userService.existsByUsername("admin")) {
            return Result.error("管理员账号已存在");
        }

        // 创建管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordUtils.encode("admin123"));  // 密码：admin123
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(1);
        userService.save(admin);

        log.info("✅ 管理员账号初始化成功");
        log.info("   用户名: admin");
        log.info("   密码: admin123");
        
        return Result.success("管理员账号初始化成功", null);
    }

}
