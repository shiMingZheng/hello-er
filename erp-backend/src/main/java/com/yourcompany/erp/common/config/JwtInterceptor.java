package com.yourcompany.erp.common.config;

import com.yourcompany.erp.common.exception.UnauthorizedException;
import com.yourcompany.erp.common.utils.JwtUtils;
import com.yourcompany.erp.common.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求头中未包含有效的 Token");
            throw new UnauthorizedException("未登录或 Token 无效");
        }

        String token = authHeader.substring(7); // 去掉 "Bearer "
        
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token 已过期或无效");
            throw new UnauthorizedException("Token 已过期或无效");
        }

        try {
            Claims claims = jwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // 将用户信息存入 ThreadLocal
            UserContext.set(userId, username, role);
            
            log.debug("用户 {} (ID: {}) 访问接口: {}", username, userId, request.getRequestURI());
            
            return true;
        } catch (Exception e) {
            log.error("Token 解析失败", e);
            throw new UnauthorizedException("Token 解析失败");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

}
