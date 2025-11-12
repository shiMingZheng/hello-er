package com.yourcompany.erp.order.repository;

import com.yourcompany.erp.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//支持动态查询
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * 根据订单编号查询
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据客户ID查询订单列表
     */
    List<Order> findByCustomerIdOrderByCreateTimeDesc(Long customerId);

    /**
     * 根据状态查询订单
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * 统计客户订单数量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

}
