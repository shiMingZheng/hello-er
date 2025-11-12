package com.yourcompany.erp.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 订单创建请求 DTO
 */
@Data
public class OrderDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> items;

    private String remark;  // 备注

    /**
     * 订单明细 DTO
     */
    @Data
    public static class OrderItemDTO {
        
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }

}
