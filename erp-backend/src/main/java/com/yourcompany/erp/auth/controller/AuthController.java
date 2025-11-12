package com.yourcompany.erp.auth.controller;

import com.yourcompany.erp.auth.dto.LoginDTO;
import com.yourcompany.erp.auth.dto.LoginVO;
import com.yourcompany.erp.auth.dto.RegisterDTO;
import com.yourcompany.erp.auth.service.AuthService;
import com.yourcompany.erp.common.response.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 认证接口
 */
@Tag(name = "认证模块", description = "用户登录、注册接口")
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
	@Operation(summary = "用户登录", description = "返回 JWT Token 和用户信息")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户 {} 尝试登录", dto.getUsername());
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    /**
     * 客户注册
     */
	@Operation(summary = "客户注册", description = "注册新客户账号")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        log.info("新用户 {} 尝试注册", dto.getUsername());
        authService.register(dto);
        return Result.success("注册成功", null);
    }

}
