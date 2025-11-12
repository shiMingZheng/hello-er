package com.yourcompany.erp.finance.repository;

import com.yourcompany.erp.finance.entity.PaymentReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentReceivableRepository extends JpaRepository<PaymentReceivable, Long> {

    /**
     * 根据收款ID查询核销记录
     */
    List<PaymentReceivable> findByPaymentId(Long paymentId);

    /**
     * 根据应收ID查询核销记录
     */
    List<PaymentReceivable> findByReceivableId(Long receivableId);

    /**
     * 查询应收的已核销总额
     */
    @Query("SELECT COALESCE(SUM(pr.amount), 0) FROM PaymentReceivable pr " +
           "WHERE pr.receivableId = :receivableId")
    Double sumAllocatedByReceivableId(@Param("receivableId") Long receivableId);

}
