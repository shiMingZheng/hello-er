package com.yourcompany.erp.order.service;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import com.yourcompany.erp.finance.service.FinanceService;
import com.yourcompany.erp.order.dto.OrderDTO;
import com.yourcompany.erp.order.dto.OrderVO;
import com.yourcompany.erp.order.entity.Order;
import com.yourcompany.erp.order.entity.OrderItem;
import com.yourcompany.erp.order.repository.OrderItemRepository;
import com.yourcompany.erp.order.repository.OrderRepository;
import com.yourcompany.erp.product.entity.Product;
import com.yourcompany.erp.product.repository.ProductRepository;
import com.yourcompany.erp.product.service.ProductService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private FinanceService financeService;

    /**
     * 创建订单
     */
    @Transactional
    public OrderVO createOrder(OrderDTO dto) {
        // 1. 验证客户
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // 2. 计算订单金额并验证库存
        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderDTO.OrderItemDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new BusinessException("商品 " + itemDto.getProductId() + " 不存在"));

            // 检查库存
            if (product.getStock() < itemDto.getQuantity()) {
                throw new BusinessException("商品 " + product.getName() + " 库存不足");
            }

            // 根据客户等级确定价格
            double price = customer.getLevel() == Customer.CustomerLevel.VIP 
                    ? product.getVipPrice() 
                    : product.getNormalPrice();

            double subtotal = price * itemDto.getQuantity();
            totalAmount += subtotal;

            // 构建订单明细
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(price);
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
        }

        // 3. 检查信用额度
        double currentDebt = customer.getBalance();
        if (currentDebt + totalAmount > customer.getCreditLimit()) {
            throw new BusinessException("订单金额超出信用额度");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setRemark(dto.getRemark());
        order = orderRepository.save(order);

        // 5. 保存订单明细
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
        }

        // 6. 扣减库存
        for (OrderDTO.OrderItemDTO itemDto : dto.getItems()) {
            productService.reduceStock(itemDto.getProductId(), itemDto.getQuantity());
        }

        // 7. 生成应收账款
        financeService.createReceivable(order.getId(), customer.getId(), totalAmount);

        log.info("订单 {} 创建成功，客户: {}, 金额: {}", order.getOrderNo(), customer.getName(), totalAmount);

        return getOrderDetail(order.getId());
    }

    /**
     * 审核订单
     */
    @Transactional
    public void approveOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("订单状态不正确，无法审核");
        }

        order.setStatus(Order.OrderStatus.APPROVED);
        order.setApprovedTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("订单 {} 已审核", order.getOrderNo());
    }

    /**
     * 发货
     */
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.APPROVED) {
            throw new BusinessException("订单未审核，无法发货");
        }

        order.setStatus(Order.OrderStatus.SHIPPED);
        order.setShippedTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("订单 {} 已发货", order.getOrderNo());
    }

    /**
     * 完成订单
     */
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException("订单未发货，无法完成");
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("订单 {} 已完成", order.getOrderNo());
    }

    /**
     * 获取订单详情
     */
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        return convertToVO(order, items);
    }

    /**
     * 获取订单列表（分页）
     */
    public Page<OrderVO> getOrderList(String orderNo, String status, Pageable pageable) {

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 按订单号模糊查询
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo + "%"));
            }

            // 按状态查询
            if (status != null && !status.isBlank()) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), orderStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的订单状态: {}", status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable)
                .map(order -> {
                    // 优化：可以减少 N+1 查询，但此处保持原逻辑
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return convertToVO(order, items);
                });
    }

    /**
     * 获取客户订单列表
     */
    public List<OrderVO> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreateTimeDesc(customerId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return convertToVO(order, items);
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + timestamp + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 转换为 VO
     */
    private OrderVO convertToVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        List<OrderVO.OrderItemVO> itemVOs = items.stream()
                .map(item -> {
                    OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    return itemVO;
                })
                .collect(Collectors.toList());

        vo.setItems(itemVOs);
        return vo;
    }

}
