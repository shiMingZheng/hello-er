package com.yourcompany.erp.order.dto;

import com.yourcompany.erp.order.entity.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应 VO
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private Double totalAmount;
    private Order.OrderStatus status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime approvedTime;
    private LocalDateTime shippedTime;
    private LocalDateTime completedTime;

    private List<OrderItemVO> items;  // 订单明细

    /**
     * 订单明细 VO
     */
    @Data
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }

}
