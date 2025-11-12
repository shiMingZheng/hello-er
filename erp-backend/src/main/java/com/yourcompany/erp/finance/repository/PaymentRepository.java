package com.yourcompany.erp.finance.repository;

import com.yourcompany.erp.finance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 根据客户ID查询收款记录
     */
    List<Payment> findByCustomerIdOrderByCreateTimeDesc(Long customerId);


}
