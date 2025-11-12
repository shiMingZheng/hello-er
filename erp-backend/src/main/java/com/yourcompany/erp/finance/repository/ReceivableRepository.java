package com.yourcompany.erp.finance.repository;

import com.yourcompany.erp.finance.entity.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivableRepository extends JpaRepository<Receivable, Long>, JpaSpecificationExecutor<Receivable> {

    /**
     * 根据订单ID查询应收
     */
    Optional<Receivable> findByOrderId(Long orderId);

    /**
     * 根据客户ID查询应收列表
     */
    List<Receivable> findByCustomerId(Long customerId);

    /**
     * 查询客户未收款总额
     */
    @Query("SELECT COALESCE(SUM(r.amount - r.paidAmount), 0) FROM Receivable r " +
           "WHERE r.customerId = :customerId AND r.status != 'PAID'")
    Double sumUnpaidByCustomerId(@Param("customerId") Long customerId);

    /**
     * 根据状态查询应收
     */
    List<Receivable> findByStatus(Receivable.ReceivableStatus status);

}
