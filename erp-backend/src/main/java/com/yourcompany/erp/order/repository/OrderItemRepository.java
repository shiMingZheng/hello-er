package com.yourcompany.erp.order.repository;

import com.yourcompany.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根据订单ID查询明细
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 删除订单明细
     */
    void deleteByOrderId(Long orderId);

}
