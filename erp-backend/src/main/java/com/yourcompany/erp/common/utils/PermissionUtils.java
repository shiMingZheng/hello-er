package com.yourcompany.erp.common.utils;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.common.exception.UnauthorizedException;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限校验工具类（防越权核心）
 */
@Slf4j
@Component
public class PermissionUtils {

    @Autowired
    private UserService userService;

    /**
     * 检查当前用户是否有权访问指定客户的数据
     * @param targetCustomerId 目标客户 ID
     * @throws UnauthorizedException 如果无权访问
     */
    public void checkCustomerPermission(Long targetCustomerId) {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        
        // 管理员有全部权限
        if ("ADMIN".equals(currentRole)) {
            log.debug("✅ 权限检查通过: 管理员访问客户 {} 的数据", targetCustomerId);
            return;
        }
        
        // 客户角色：只能访问自己的数据
        if ("CUSTOMER".equals(currentRole)) {
            User user = userService.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            
            if (!targetCustomerId.equals(user.getCustomerId())) {
                log.warn("⚠️ 权限拒绝: 用户 {} (客户 {}) 尝试访问客户 {} 的数据", 
                    currentUserId, user.getCustomerId(), targetCustomerId);
                throw new UnauthorizedException("无权访问其他客户的数据");
            }
            
            log.debug("✅ 权限检查通过: 客户 {} 访问自己的数据", targetCustomerId);
        }
    }

    /**
     * 检查当前用户是否是管理员
     * @throws UnauthorizedException 如果不是管理员
     */
    public void requireAdmin() {
        String currentRole = UserContext.getRole();
        if (!"ADMIN".equals(currentRole)) {
            log.warn("⚠️ 权限拒绝: 用户 {} 尝试访问管理员功能，当前角色: {}", 
                UserContext.getUserId(), currentRole);
            throw new UnauthorizedException("需要管理员权限");
        }
        log.debug("✅ 权限检查通过: 管理员访问");
    }

    /**
     * 获取当前用户的客户 ID（如果是客户角色）
     * @return 客户 ID，如果不是客户角色则返回 null
     */
    public Long getCurrentCustomerId() {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        
        if ("CUSTOMER".equals(currentRole)) {
            User user = userService.findById(currentUserId).orElse(null);
            return user != null ? user.getCustomerId() : null;
        }
        
        return null;
    }

    /**
     * 检查是否是当前用户自己的订单
     * @param orderId 订单 ID
     * @throws UnauthorizedException 如果不是自己的订单
     */
    public void checkOrderPermission(Long orderId, Long orderCustomerId) {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        
        // 管理员有全部权限
        if ("ADMIN".equals(currentRole)) {
            return;
        }
        
        // 客户角色：只能访问自己的订单
        if ("CUSTOMER".equals(currentRole)) {
            User user = userService.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            
            if (!orderCustomerId.equals(user.getCustomerId())) {
                log.warn("⚠️ 权限拒绝: 用户 {} 尝试访问订单 {} (属于客户 {})", 
                    currentUserId, orderId, orderCustomerId);
                throw new UnauthorizedException("无权访问其他客户的订单");
            }
        }
    }
}
