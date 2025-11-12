package com.yourcompany.erp.finance;

import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import com.yourcompany.erp.finance.dto.*;
import com.yourcompany.erp.finance.entity.Receivable;
import com.yourcompany.erp.finance.repository.ReceivableRepository;
import com.yourcompany.erp.finance.service.FinanceService;
import com.yourcompany.erp.order.entity.Order;
import com.yourcompany.erp.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 测试后自动回滚
class FinanceTestsV2 {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReceivableRepository receivableRepository;

    private Customer testCustomer;
    private List<Order> testOrders = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 创建测试客户
        testCustomer = new Customer();
        testCustomer.setName("测试客户");
        testCustomer.setLevel(Customer.CustomerLevel.NORMAL);
        testCustomer.setCreditLimit(10000.0);
        testCustomer.setBalance(0.0);
        testCustomer = customerRepository.save(testCustomer);

        // 创建多个测试订单
        for (int i = 1; i <= 3; i++) {
            Order order = new Order();
            order.setOrderNo("TEST" + System.currentTimeMillis() + i);
            order.setCustomerId(testCustomer.getId());
            order.setCustomerName(testCustomer.getName());
            order.setTotalAmount(1000.0 * i);
            order.setStatus(Order.OrderStatus.PENDING);
            testOrders.add(orderRepository.save(order));
        }
    }

    @Test
    void testBatchPayment() {
        // 创建3笔应收
        for (Order order : testOrders) {
            financeService.createReceivable(order.getId(), testCustomer.getId(), order.getTotalAmount());
        }

        List<Receivable> receivables = receivableRepository.findByCustomerId(testCustomer.getId());
        assertEquals(3, receivables.size());

        // 批量核销：一笔3000元收款，核销第1笔1000元和第2笔2000元
        PaymentBatchDTO dto = new PaymentBatchDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setTotalAmount(3000.0);
        dto.setPaymentMethod("转账");

        List<PaymentBatchDTO.AllocationItem> allocations = new ArrayList<>();
        allocations.add(createAllocation(receivables.get(0).getId(), 1000.0));  // 第1笔全额
        allocations.add(createAllocation(receivables.get(1).getId(), 2000.0));  // 第2笔全额
        dto.setAllocations(allocations);

        PaymentVO paymentVO = financeService.recordBatchPayment(dto);

        // 断言
        assertNotNull(paymentVO.getId());
        assertEquals(3000.0, paymentVO.getAmount());

        // 验证应收状态
        Receivable r1 = receivableRepository.findById(receivables.get(0).getId()).orElseThrow();
        assertEquals(Receivable.ReceivableStatus.PAID, r1.getStatus());

        Receivable r2 = receivableRepository.findById(receivables.get(1).getId()).orElseThrow();
        assertEquals(Receivable.ReceivableStatus.PAID, r2.getStatus());

        Receivable r3 = receivableRepository.findById(receivables.get(2).getId()).orElseThrow();
        assertEquals(Receivable.ReceivableStatus.UNPAID, r3.getStatus());

        // 验证客户余额
        Customer customer = customerRepository.findById(testCustomer.getId()).orElseThrow();
        assertEquals(3000.0, customer.getBalance());  // 还有第3笔3000未收
    }

    @Test
    void testPartialAllocation() {
        // 创建1笔应收
        financeService.createReceivable(testOrders.get(0).getId(), testCustomer.getId(), 1000.0);
        Receivable receivable = receivableRepository.findByCustomerId(testCustomer.getId()).get(0);

        // 部分核销：收款500元
        PaymentBatchDTO dto = new PaymentBatchDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setTotalAmount(500.0);
        dto.setPaymentMethod("现金");

        List<PaymentBatchDTO.AllocationItem> allocations = new ArrayList<>();
        allocations.add(createAllocation(receivable.getId(), 500.0));
        dto.setAllocations(allocations);

        financeService.recordBatchPayment(dto);

        // 验证应收状态为部分收款
        Receivable updated = receivableRepository.findById(receivable.getId()).orElseThrow();
        assertEquals(500.0, updated.getPaidAmount());
        assertEquals(Receivable.ReceivableStatus.PARTIAL, updated.getStatus());

        // 再次收款500元，完全核销
        PaymentBatchDTO dto2 = new PaymentBatchDTO();
        dto2.setCustomerId(testCustomer.getId());
        dto2.setTotalAmount(500.0);
        dto2.setPaymentMethod("现金");

        List<PaymentBatchDTO.AllocationItem> allocations2 = new ArrayList<>();
        allocations2.add(createAllocation(receivable.getId(), 500.0));
        dto2.setAllocations(allocations2);

        financeService.recordBatchPayment(dto2);

        // 验证应收状态为已收款
        Receivable finalState = receivableRepository.findById(receivable.getId()).orElseThrow();
        assertEquals(1000.0, finalState.getPaidAmount());
        assertEquals(Receivable.ReceivableStatus.PAID, finalState.getStatus());
    }

    @Test
    void testReceivableAgeAnalysis() {
        // 创建不同时间的应收
        Order order1 = testOrders.get(0);
        financeService.createReceivable(order1.getId(), testCustomer.getId(), 1000.0);

        // 修改创建时间模拟不同账龄
        Receivable r1 = receivableRepository.findByCustomerId(testCustomer.getId()).get(0);
        r1.setCreateTime(LocalDateTime.now().minusDays(10));  // 10天前
        receivableRepository.save(r1);

        Order order2 = testOrders.get(1);
        financeService.createReceivable(order2.getId(), testCustomer.getId(), 2000.0);
        Receivable r2 = receivableRepository.findByCustomerId(testCustomer.getId()).get(1);
        r2.setCreateTime(LocalDateTime.now().minusDays(25));  // 25天前
        receivableRepository.save(r2);

        Order order3 = testOrders.get(2);
        financeService.createReceivable(order3.getId(), testCustomer.getId(), 3000.0);
        Receivable r3 = receivableRepository.findByCustomerId(testCustomer.getId()).get(2);
        r3.setCreateTime(LocalDateTime.now().minusDays(70));  // 70天前
        receivableRepository.save(r3);

        // 账龄分析
        ReceivableAgeVO ageVO = financeService.analyzeReceivableAge(testCustomer.getId());

        // 断言
        assertEquals(1000.0, ageVO.getWithin15Days());   // 10天的
        assertEquals(2000.0, ageVO.getWithin30Days());   // 25天的
        assertEquals(3000.0, ageVO.getOver60Days());     // 70天的
        assertEquals(6000.0, ageVO.getTotalUnpaid());
    }

    @Test
    void testMonthlyStatement() {
        // 创建本月的应收和收款
        int currentYear = LocalDateTime.now().getYear();
        int currentMonth = LocalDateTime.now().getMonthValue();

        // 创建应收
        financeService.createReceivable(testOrders.get(0).getId(), testCustomer.getId(), 1000.0);

        // 生成对账单
        AccountStatementVO statement = financeService.generateMonthlyStatement(
                testCustomer.getId(), currentYear, currentMonth);

        // 断言
        assertEquals(testCustomer.getId(), statement.getCustomerId());
        assertEquals(1000.0, statement.getPeriodSales());
        assertTrue(statement.getDetails().size() > 0);
    }

    @Test
    void testInvalidBatchPayment() {
        // 创建应收
        financeService.createReceivable(testOrders.get(0).getId(), testCustomer.getId(), 1000.0);
        Receivable receivable = receivableRepository.findByCustomerId(testCustomer.getId()).get(0);

        // 核销金额总和与收款金额不一致
        PaymentBatchDTO dto = new PaymentBatchDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setTotalAmount(1000.0);  // 收款1000

        List<PaymentBatchDTO.AllocationItem> allocations = new ArrayList<>();
        allocations.add(createAllocation(receivable.getId(), 500.0));  // 只核销500
        dto.setAllocations(allocations);

        // 断言抛出异常
        assertThrows(Exception.class, () -> financeService.recordBatchPayment(dto));
    }

    @Test
    void testExceedUnpaidAmount() {
        // 创建应收
        financeService.createReceivable(testOrders.get(0).getId(), testCustomer.getId(), 1000.0);
        Receivable receivable = receivableRepository.findByCustomerId(testCustomer.getId()).get(0);

        // 核销金额超过未收金额
        PaymentBatchDTO dto = new PaymentBatchDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setTotalAmount(1500.0);

        List<PaymentBatchDTO.AllocationItem> allocations = new ArrayList<>();
        allocations.add(createAllocation(receivable.getId(), 1500.0));  // 超额
        dto.setAllocations(allocations);

        // 断言抛出异常
        assertThrows(Exception.class, () -> financeService.recordBatchPayment(dto));
    }

    private PaymentBatchDTO.AllocationItem createAllocation(Long receivableId, Double amount) {
        PaymentBatchDTO.AllocationItem item = new PaymentBatchDTO.AllocationItem();
        item.setReceivableId(receivableId);
        item.setAmount(amount);
        return item;
    }

}
