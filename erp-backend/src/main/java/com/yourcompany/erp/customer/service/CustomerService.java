package com.yourcompany.erp.customer.service;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.customer.dto.CustomerDTO;
import com.yourcompany.erp.customer.dto.CustomerVO;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 创建客户
     */
    @Transactional
    public CustomerVO createCustomer(CustomerDTO dto) {
        // 检查客户名称是否重复
        if (customerRepository.existsByName(dto.getName())) {
            throw new BusinessException("客户名称已存在");
        }

        // 检查电话是否重复
        if (dto.getPhone() != null && customerRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new BusinessException("电话号码已存在");
        }

        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        
        // 设置默认值
        if (customer.getLevel() == null) {
            customer.setLevel(Customer.CustomerLevel.NORMAL);
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(5000.0);  // 默认信用额度 5000
        }
        customer.setBalance(0.0);

        customer = customerRepository.save(customer);
        log.info("客户 {} 创建成功，ID: {}", customer.getName(), customer.getId());

        return convertToVO(customer);
    }

    /**
     * 更新客户
     */
    @Transactional
    public CustomerVO updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // 检查名称是否与其他客户重复
        if (dto.getName() != null && !dto.getName().equals(customer.getName())) {
            if (customerRepository.existsByName(dto.getName())) {
                throw new BusinessException("客户名称已存在");
            }
        }

        // 检查电话是否与其他客户重复
        if (dto.getPhone() != null && !dto.getPhone().equals(customer.getPhone())) {
            customerRepository.findByPhone(dto.getPhone()).ifPresent(c -> {
                if (!c.getId().equals(id)) {
                    throw new BusinessException("电话号码已存在");
                }
            });
        }

        // 更新字段

        if (dto.getName() != null) customer.setName(dto.getName());
        if (dto.getContact() != null) customer.setContact(dto.getContact());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getAddress() != null) customer.setAddress(dto.getAddress());
        if (dto.getLevel() != null) customer.setLevel(dto.getLevel());
        if (dto.getCreditLimit() != null) customer.setCreditLimit(dto.getCreditLimit());
        // 增加对 email, remark, status 的更新
        if (dto.getEmail() != null) customer.setEmail(dto.getEmail());
        if (dto.getRemark() != null) customer.setRemark(dto.getRemark());
        if (dto.getStatus() != null) customer.setStatus(dto.getStatus());
        customer = customerRepository.save(customer);
        log.info("客户 {} 更新成功", customer.getName());

        return convertToVO(customer);
    }

    /**
     * 删除客户
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // 检查是否有未结清的欠款
        if (customer.getBalance() > 0) {
            throw new BusinessException("客户还有未结清欠款，无法删除");
        }

        customerRepository.delete(customer);
        log.info("客户 {} 已删除", customer.getName());
    }

    /**
     * 获取客户详情
     */
    public CustomerVO getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));
        return convertToVO(customer);
    }

    /**
     * 获取客户列表（分页 + 高级筛选）
     */
    // --- 改造点开始：重写此方法 ---
    public Page<CustomerVO> getCustomerList(
            String customerName,
            String phone,
            Customer.CustomerLevel level,
            String status, // 接收 String 类型的 status
            Pageable pageable) {

        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerName != null && !customerName.isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + customerName + "%"));
            }
            if (phone != null && !phone.isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
            }
            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }
            if (status != null && !status.isBlank()) {
                try {
                    // 将 String 转换为 Enum
                    Customer.CustomerStatus customerStatus = Customer.CustomerStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), customerStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的客户状态: {}", status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return customerRepository.findAll(spec, pageable).map(this::convertToVO);
    }
	
	/**
     * 切换客户状态
     */
    @Transactional
    public CustomerVO toggleCustomerStatus(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        if (customer.getStatus() == Customer.CustomerStatus.ACTIVE) {
            customer.setStatus(Customer.CustomerStatus.INACTIVE);
        } else {
            customer.setStatus(Customer.CustomerStatus.ACTIVE);
        }
        
        customer = customerRepository.save(customer);
        log.info("客户 {} 状态切换为: {}", customer.getName(), customer.getStatus());
        
        return convertToVO(customer);
    }

    /**
     * 获取所有客户列表（不分页）
     */
    public List<CustomerVO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据等级查询客户
     */
    public List<CustomerVO> getCustomersByLevel(Customer.CustomerLevel level) {
        return customerRepository.findByLevel(level).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为 VO
     */
    private CustomerVO convertToVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(customer, vo);
        
        // 计算可用额度
        vo.setAvailableCredit(customer.getCreditLimit() - customer.getBalance());
        
        return vo;
    }

}
