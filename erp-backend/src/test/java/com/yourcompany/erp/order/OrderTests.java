package com.yourcompany.erp.order;

import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import com.yourcompany.erp.order.dto.OrderDTO;
import com.yourcompany.erp.order.dto.OrderVO;
import com.yourcompany.erp.order.service.OrderService;
import com.yourcompany.erp.product.entity.Product;
import com.yourcompany.erp.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 测试后自动回滚
class OrderTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer testCustomer;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 创建测试客户
        testCustomer = new Customer();
        testCustomer.setName("测试客户");
        testCustomer.setLevel(Customer.CustomerLevel.NORMAL);
        testCustomer.setCreditLimit(10000.0);
        testCustomer.setBalance(0.0);
        testCustomer = customerRepository.save(testCustomer);

        // 创建测试商品
        testProduct = new Product();
        testProduct.setName("测试商品");
        testProduct.setNormalPrice(100.0);
        testProduct.setVipPrice(90.0);
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testCreateOrder() {
        // 准备订单数据
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setRemark("测试订单");

        OrderDTO.OrderItemDTO itemDto = new OrderDTO.OrderItemDTO();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(5);
        dto.setItems(List.of(itemDto));

        // 创建订单
        OrderVO vo = orderService.createOrder(dto);

        // 断言
        assertNotNull(vo.getId());
        assertNotNull(vo.getOrderNo());
        assertEquals(testCustomer.getId(), vo.getCustomerId());
        assertEquals(500.0, vo.getTotalAmount());  // 100 * 5
        assertEquals(1, vo.getItems().size());

        // 验证库存扣减
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(95, updatedProduct.getStock());
    }

    @Test
    void testOrderStatusFlow() {
        // 创建订单
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(testCustomer.getId());
        OrderDTO.OrderItemDTO itemDto = new OrderDTO.OrderItemDTO();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(2);
        dto.setItems(List.of(itemDto));

        OrderVO order = orderService.createOrder(dto);

        // 测试状态流转
        orderService.approveOrder(order.getId());
        OrderVO approved = orderService.getOrderDetail(order.getId());
        assertEquals("APPROVED", approved.getStatus().name());

        orderService.shipOrder(order.getId());
        OrderVO shipped = orderService.getOrderDetail(order.getId());
        assertEquals("SHIPPED", shipped.getStatus().name());

        orderService.completeOrder(order.getId());
        OrderVO completed = orderService.getOrderDetail(order.getId());
        assertEquals("COMPLETED", completed.getStatus().name());
    }

    @Test
    void testInsufficientStock() {
        // 准备库存不足的订单
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(testCustomer.getId());

        OrderDTO.OrderItemDTO itemDto = new OrderDTO.OrderItemDTO();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(200);  // 超过库存
        dto.setItems(List.of(itemDto));

        // 断言抛出异常
        assertThrows(Exception.class, () -> orderService.createOrder(dto));
    }

    @Test
    void testExceedCreditLimit() {
        // 准备超额订单
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(testCustomer.getId());

        OrderDTO.OrderItemDTO itemDto = new OrderDTO.OrderItemDTO();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(150);  // 100 * 150 = 15000，超过信用额度 10000
        dto.setItems(List.of(itemDto));

        // 断言抛出异常
        assertThrows(Exception.class, () -> orderService.createOrder(dto));
    }

}
