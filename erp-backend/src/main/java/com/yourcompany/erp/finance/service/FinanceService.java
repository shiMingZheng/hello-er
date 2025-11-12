package com.yourcompany.erp.finance.service;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.repository.CustomerRepository;
import com.yourcompany.erp.finance.dto.*;
import com.yourcompany.erp.finance.entity.Payment;
import com.yourcompany.erp.finance.entity.PaymentReceivable;
import com.yourcompany.erp.finance.entity.Receivable;
import com.yourcompany.erp.finance.repository.PaymentReceivableRepository;
import com.yourcompany.erp.finance.repository.PaymentRepository;
import com.yourcompany.erp.finance.repository.ReceivableRepository;
import com.yourcompany.erp.order.entity.Order;
import com.yourcompany.erp.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.yourcompany.erp.finance.entity.Receivable.ReceivableStatus; // 导入内部枚举

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinanceService {

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentReceivableRepository paymentReceivableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 创建应收账款（订单创建时自动调用）
     */
    @Transactional
    public void createReceivable(Long orderId, Long customerId, Double amount) {
        Receivable receivable = new Receivable();
        receivable.setOrderId(orderId);
        receivable.setCustomerId(customerId);
        receivable.setAmount(amount);
        receivable.setPaidAmount(0.0);
        receivable.setStatus(Receivable.ReceivableStatus.UNPAID);
        receivableRepository.save(receivable);

        // 更新客户欠款余额
        updateCustomerBalance(customerId);

        log.info("应收账款创建成功，订单: {}, 金额: {}", orderId, amount);
    }

    /**
     * 方案一：简单收款（一笔收款对一个订单）- 保留兼容
     */
    @Transactional
    public PaymentVO recordPayment(PaymentDTO dto) {
        // 验证应收账款
        Receivable receivable = receivableRepository.findById(dto.getReceivableId())
                .orElseThrow(() -> new BusinessException("应收账款不存在"));

        if (receivable.getStatus() == Receivable.ReceivableStatus.PAID) {
            throw new BusinessException("该应收账款已收款");
        }

        // 验证收款金额
        double unpaidAmount = receivable.getAmount() - receivable.getPaidAmount();
        if (dto.getAmount() > unpaidAmount) {
            throw new BusinessException("收款金额超过未收金额");
        }

        // 创建收款记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setCustomerId(dto.getCustomerId());
        payment.setAmount(dto.getAmount());
        payment.setAllocatedAmount(dto.getAmount());  // 简单核销，全部分配
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setRemark(dto.getRemark());
        payment = paymentRepository.save(payment);

        // 创建核销记录
        PaymentReceivable pr = new PaymentReceivable();
        pr.setPaymentId(payment.getId());
        pr.setReceivableId(dto.getReceivableId());
        pr.setAmount(dto.getAmount());
        paymentReceivableRepository.save(pr);

        // 更新应收账款
        updateReceivableStatus(receivable.getId());

        // 更新客户欠款余额
        updateCustomerBalance(dto.getCustomerId());

        log.info("收款记录创建成功，客户: {}, 金额: {}", dto.getCustomerId(), dto.getAmount());

        return convertPaymentToVO(payment);
    }

    /**
     * 方案二：批量核销（一笔收款核销多个订单）
     */
    @Transactional
    public PaymentVO recordBatchPayment(PaymentBatchDTO dto) {
        // 1. 验证核销金额总和
        double totalAllocated = dto.getAllocations().stream()
                .mapToDouble(PaymentBatchDTO.AllocationItem::getAmount)
                .sum();

        if (Math.abs(totalAllocated - dto.getTotalAmount()) > 0.01) {
            throw new BusinessException("核销金额总和与收款金额不一致");
        }

        // 2. 验证每笔应收
        for (PaymentBatchDTO.AllocationItem item : dto.getAllocations()) {
            Receivable receivable = receivableRepository.findById(item.getReceivableId())
                    .orElseThrow(() -> new BusinessException("应收账款 " + item.getReceivableId() + " 不存在"));

            if (!receivable.getCustomerId().equals(dto.getCustomerId())) {
                throw new BusinessException("应收账款不属于该客户");
            }

            double unpaidAmount = receivable.getAmount() - receivable.getPaidAmount();
            if (item.getAmount() > unpaidAmount) {
                throw new BusinessException("核销金额超过应收未收金额");
            }
        }

        // 3. 创建收款记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setCustomerId(dto.getCustomerId());
        payment.setAmount(dto.getTotalAmount());
        payment.setAllocatedAmount(totalAllocated);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setRemark(dto.getRemark());
        payment = paymentRepository.save(payment);

        // 4. 创建核销记录并更新应收
        for (PaymentBatchDTO.AllocationItem item : dto.getAllocations()) {
            PaymentReceivable pr = new PaymentReceivable();
            pr.setPaymentId(payment.getId());
            pr.setReceivableId(item.getReceivableId());
            pr.setAmount(item.getAmount());
            paymentReceivableRepository.save(pr);

            // 更新应收状态
            updateReceivableStatus(item.getReceivableId());
        }

        // 5. 更新客户欠款余额
        updateCustomerBalance(dto.getCustomerId());

        log.info("批量核销成功，客户: {}, 收款金额: {}, 核销 {} 笔应收",
                dto.getCustomerId(), dto.getTotalAmount(), dto.getAllocations().size());

        return convertPaymentToVO(payment);
    }

    /**
     * 更新应收账款状态（根据核销记录计算）
     */
    private void updateReceivableStatus(Long receivableId) {
        Receivable receivable = receivableRepository.findById(receivableId)
                .orElseThrow(() -> new BusinessException("应收账款不存在"));

        // 计算已收金额
        Double allocatedAmount = paymentReceivableRepository.sumAllocatedByReceivableId(receivableId);
        receivable.setPaidAmount(allocatedAmount);

        // 更新状态
        if (allocatedAmount.equals(receivable.getAmount())) {
            receivable.setStatus(Receivable.ReceivableStatus.PAID);
        } else if (allocatedAmount > 0) {
            receivable.setStatus(Receivable.ReceivableStatus.PARTIAL);
        } else {
            receivable.setStatus(Receivable.ReceivableStatus.UNPAID);
        }

        receivableRepository.save(receivable);
    }

    /**
     * 获取应收账款列表（分页）
     */
    // --- 改造点 2：重写此方法以支持筛选 ---
    public Page<ReceivableVO> getReceivableList(Long customerId, String status, Pageable pageable) {

        Specification<Receivable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 按客户ID查询
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }

            // 按状态查询
            if (status != null && !status.isBlank()) {
                try {
                    ReceivableStatus receivableStatus = ReceivableStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), receivableStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的应收状态: {}", status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return receivableRepository.findAll(spec, pageable)
                .map(this::convertReceivableToVO);
    }

    /**
     * 获取客户应收列表
     */
    public List<ReceivableVO> getCustomerReceivables(Long customerId) {
        return receivableRepository.findByCustomerId(customerId).stream()
                .map(this::convertReceivableToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取收款记录列表
     */
    public List<PaymentVO> getPaymentList(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreateTimeDesc(customerId).stream()
                .map(this::convertPaymentToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取客户欠款汇总
     */
    public CustomerDebtVO getCustomerDebt(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        Double totalDebt = receivableRepository.sumUnpaidByCustomerId(customerId);
        Double availableCredit = customer.getCreditLimit() - totalDebt;

        return new CustomerDebtVO(
                customer.getId(),
                customer.getName(),
                customer.getCreditLimit(),
                totalDebt,
                availableCredit
        );
    }

    /**
     * 账龄分析
     */
    public ReceivableAgeVO analyzeReceivableAge(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        List<Receivable> receivables = receivableRepository.findByCustomerId(customerId).stream()
                .filter(r -> r.getStatus() != Receivable.ReceivableStatus.PAID)
                .collect(Collectors.toList());

        ReceivableAgeVO vo = new ReceivableAgeVO();
        vo.setCustomerId(customer.getId());
        vo.setCustomerName(customer.getName());

        LocalDateTime now = LocalDateTime.now();

        for (Receivable receivable : receivables) {
            double unpaidAmount = receivable.getAmount() - receivable.getPaidAmount();
            long days = ChronoUnit.DAYS.between(receivable.getCreateTime(), now);

            if (days <= 15) {
                vo.setWithin15Days(vo.getWithin15Days() + unpaidAmount);
            } else if (days <= 30) {
                vo.setWithin30Days(vo.getWithin30Days() + unpaidAmount);
            } else if (days <= 60) {
                vo.setWithin60Days(vo.getWithin60Days() + unpaidAmount);
            } else {
                vo.setOver60Days(vo.getOver60Days() + unpaidAmount);
            }
        }

        vo.calculateTotal();
        return vo;
    }

    /**
     * 生成月度对账单
     */
    public AccountStatementVO generateMonthlyStatement(Long customerId, int year, int month) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        AccountStatementVO vo = new AccountStatementVO();
        vo.setCustomerId(customer.getId());
        vo.setCustomerName(customer.getName());
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);

        // 计算期初余额（本月1日之前的未收款）
        vo.setOpeningBalance(calculateBalanceBeforeDate(customerId, startDate));

        // 本期发生额
        List<AccountStatementVO.StatementDetail> details = new ArrayList<>();
        double runningBalance = vo.getOpeningBalance();

        // 查询本月应收
        List<Receivable> receivables = receivableRepository.findByCustomerId(customerId).stream()
                .filter(r -> {
                    LocalDate createDate = r.getCreateTime().toLocalDate();
                    return !createDate.isBefore(startDate) && !createDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        for (Receivable receivable : receivables) {
            runningBalance += receivable.getAmount();
            vo.setPeriodSales(vo.getPeriodSales() + receivable.getAmount());

            Order order = orderRepository.findById(receivable.getOrderId()).orElse(null);
            details.add(new AccountStatementVO.StatementDetail(
                    receivable.getCreateTime().toLocalDate(),
                    "销售",
                    order != null ? order.getOrderNo() : "",
                    receivable.getAmount(),
                    0.0,
                    runningBalance
            ));
        }

        // 查询本月收款
        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreateTimeDesc(customerId).stream()
                .filter(p -> {
                    LocalDate payDate = p.getCreateTime().toLocalDate();
                    return !payDate.isBefore(startDate) && !payDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        for (Payment payment : payments) {
            runningBalance -= payment.getAmount();
            vo.setPeriodPayments(vo.getPeriodPayments() + payment.getAmount());

            details.add(new AccountStatementVO.StatementDetail(
                    payment.getCreateTime().toLocalDate(),
                    "收款",
                    payment.getPaymentNo(),
                    0.0,
                    payment.getAmount(),
                    runningBalance
            ));
        }

        // 按日期排序
        details.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        vo.setDetails(details);

        // 期末余额
        vo.setClosingBalance(vo.getOpeningBalance() + vo.getPeriodSales() - vo.getPeriodPayments());

        return vo;
    }

    /**
     * 计算指定日期之前的余额
     */
    private Double calculateBalanceBeforeDate(Long customerId, LocalDate date) {
        List<Receivable> receivables = receivableRepository.findByCustomerId(customerId).stream()
                .filter(r -> r.getCreateTime().toLocalDate().isBefore(date))
                .collect(Collectors.toList());

        double totalReceivable = receivables.stream()
                .mapToDouble(Receivable::getAmount)
                .sum();

        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreateTimeDesc(customerId).stream()
                .filter(p -> p.getCreateTime().toLocalDate().isBefore(date))
                .collect(Collectors.toList());

        double totalPayment = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        return totalReceivable - totalPayment;
    }

    /**
     * 更新客户欠款余额
     */
    private void updateCustomerBalance(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        Double totalDebt = receivableRepository.sumUnpaidByCustomerId(customerId);
        customer.setBalance(totalDebt);
        customerRepository.save(customer);
    }

    /**
     * 生成收款编号
     */
    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PAY" + timestamp + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 转换应收为 VO
     */
    private ReceivableVO convertReceivableToVO(Receivable receivable) {
        ReceivableVO vo = new ReceivableVO();
        BeanUtils.copyProperties(receivable, vo);

        // 查询订单编号
        orderRepository.findById(receivable.getOrderId()).ifPresent(order -> {
            vo.setOrderNo(order.getOrderNo());
        });

        // 查询客户名称
        customerRepository.findById(receivable.getCustomerId()).ifPresent(customer -> {
            vo.setCustomerName(customer.getName());
        });

        // 计算未收金额
        vo.setUnpaidAmount(receivable.getAmount() - receivable.getPaidAmount());

        return vo;
    }

    /**
     * 转换收款为 VO
     */
    private PaymentVO convertPaymentToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);

        // 查询客户名称
        customerRepository.findById(payment.getCustomerId()).ifPresent(customer -> {
            vo.setCustomerName(customer.getName());
        });

        // 查询关联的订单编号（如果是批量核销，显示第一个）
        List<PaymentReceivable> allocations = paymentReceivableRepository.findByPaymentId(payment.getId());
        if (!allocations.isEmpty()) {
            receivableRepository.findById(allocations.get(0).getReceivableId()).ifPresent(receivable -> {
                orderRepository.findById(receivable.getOrderId()).ifPresent(order -> {
                    vo.setOrderNo(order.getOrderNo());
                });
            });
        }

        return vo;
    }

}
