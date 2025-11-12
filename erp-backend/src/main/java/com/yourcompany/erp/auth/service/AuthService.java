package com.yourcompany.erp.auth.service;

import com.yourcompany.erp.auth.dto.LoginDTO;
import com.yourcompany.erp.auth.dto.LoginVO;
import com.yourcompany.erp.auth.dto.RegisterDTO;
import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.common.utils.JwtUtils;
import com.yourcompany.erp.common.utils.PasswordUtils;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import com.yourcompany.erp.user.entity.User;
import com.yourcompany.erp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordUtils passwordUtils;

    /**
     * 登录
     */
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        User user = userService.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 2. 验证密码
        if (!passwordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        // 5. 构造响应
        LoginVO.UserInfo userInfo = new LoginVO.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCustomerId()
        );

        log.info("用户 {} 登录成功", user.getUsername());
        
        return new LoginVO(token, userInfo);
    }

    /**
     * 客户注册
     */
    @Transactional
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        if (userService.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建客户记录
        Customer customer = new Customer();
        customer.setName(dto.getCustomerName());
        customer.setPhone(dto.getPhone());
        customer.setLevel(Customer.CustomerLevel.NORMAL);  // 默认普通客户
        customer.setCreditLimit(5000.0);  // 默认信用额度 5000
        customer.setBalance(0.0);
        customer = customerRepository.save(customer);

        // 3. 创建用户记录
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRole(User.UserRole.CUSTOMER);
        user.setCustomerId(customer.getId());
        user.setStatus(1);
        userService.save(user);

        log.info("客户 {} 注册成功，客户ID: {}", dto.getUsername(), customer.getId());
    }

}
