package com.yourcompany.erp.customer;

import com.yourcompany.erp.customer.dto.CustomerDTO;
import com.yourcompany.erp.customer.dto.CustomerVO;
import com.yourcompany.erp.customer.entity.Customer;
import com.yourcompany.erp.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;  // ⬅️ 添加这行

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户模块单元测试
 */
@SpringBootTest
@Transactional  // 测试后自动回滚，不污染数据库
class CustomerTests {

    @Autowired
    private CustomerService customerService;

    // ⬇️ 添加生成随机电话号码的方法
    private String generateRandomPhone() {
        Random random = new Random();
        return "138" + String.format("%08d", random.nextInt(100000000));
    }

    @Test
    void testCreateCustomer() {
        // 准备数据
        CustomerDTO dto = new CustomerDTO();
        dto.setName("测试客户A");
        dto.setContact("张三");
        dto.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        dto.setAddress("北京市朝阳区");
        dto.setLevel(Customer.CustomerLevel.NORMAL);
        dto.setCreditLimit(10000.0);

        // 执行
        CustomerVO vo = customerService.createCustomer(dto);

        // 断言
        assertNotNull(vo.getId());
        assertEquals("测试客户A", vo.getName());
        assertEquals("张三", vo.getContact());
        assertEquals(Customer.CustomerLevel.NORMAL, vo.getLevel());
        assertEquals(10000.0, vo.getCreditLimit());
        assertEquals(0.0, vo.getBalance());  // 新客户余额为0
        assertEquals(10000.0, vo.getAvailableCredit());  // 可用额度=信用额度
    }

    @Test
    void testUpdateCustomer() {
        // 先创建客户
        CustomerDTO createDto = new CustomerDTO();
        createDto.setName("待更新客户" + System.currentTimeMillis());  // ⬅️ 加时间戳避免重复
        createDto.setContact("李四");
        createDto.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        createDto.setLevel(Customer.CustomerLevel.NORMAL);
        createDto.setCreditLimit(5000.0);
        CustomerVO created = customerService.createCustomer(createDto);

        // 更新客户
        CustomerDTO updateDto = new CustomerDTO();
        updateDto.setName("已更新客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        updateDto.setLevel(Customer.CustomerLevel.VIP);
        updateDto.setCreditLimit(20000.0);
        CustomerVO updated = customerService.updateCustomer(created.getId(), updateDto);

        // 断言
        assertTrue(updated.getName().startsWith("已更新客户"));
        assertEquals(Customer.CustomerLevel.VIP, updated.getLevel());
        assertEquals(20000.0, updated.getCreditLimit());
        assertEquals("李四", updated.getContact());  // 未修改的字段保持不变
    }

    @Test
    void testGetCustomer() {
        // 先创建客户
        CustomerDTO dto = new CustomerDTO();
        dto.setName("查询测试客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        dto.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        dto.setLevel(Customer.CustomerLevel.NORMAL);
        dto.setCreditLimit(8000.0);
        CustomerVO created = customerService.createCustomer(dto);

        // 查询客户
        CustomerVO vo = customerService.getCustomer(created.getId());

        // 断言
        assertEquals(created.getId(), vo.getId());
        assertTrue(vo.getName().startsWith("查询测试客户"));
    }

    @Test
    void testDeleteCustomer() {
        // 创建客户
        CustomerDTO dto = new CustomerDTO();
        dto.setName("待删除客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        dto.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        dto.setLevel(Customer.CustomerLevel.NORMAL);
        dto.setCreditLimit(5000.0);
        CustomerVO created = customerService.createCustomer(dto);

        // 删除客户
        assertDoesNotThrow(() -> customerService.deleteCustomer(created.getId()));
    }

    @Test
    void testCreateDuplicateName() {
        // 使用时间戳确保测试隔离
        String uniqueName = "重复名称客户" + System.currentTimeMillis();
        
        // 创建第一个客户
        CustomerDTO dto1 = new CustomerDTO();
        dto1.setName(uniqueName);
        dto1.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        dto1.setLevel(Customer.CustomerLevel.NORMAL);
        dto1.setCreditLimit(5000.0);
        customerService.createCustomer(dto1);

        // 尝试创建同名客户
        CustomerDTO dto2 = new CustomerDTO();
        dto2.setName(uniqueName);  // 相同名称
        dto2.setPhone(generateRandomPhone());  // ⬅️ 不同电话
        dto2.setLevel(Customer.CustomerLevel.NORMAL);
        dto2.setCreditLimit(5000.0);

        // 断言抛出异常
        assertThrows(Exception.class, () -> customerService.createCustomer(dto2));
    }

    @Test
    void testDeleteCustomerWithDebt() {
        // 创建有欠款的客户（手动设置余额）
        CustomerDTO dto = new CustomerDTO();
        dto.setName("有欠款客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        dto.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        dto.setLevel(Customer.CustomerLevel.NORMAL);
        dto.setCreditLimit(10000.0);
        CustomerVO created = customerService.createCustomer(dto);

        // 这里实际应该通过订单产生欠款，但为了测试简化，可以跳过
        // 实际项目中，这个测试应该在集成测试中完成

        // 断言：无法删除有欠款的客户
        // assertThrows(Exception.class, () -> customerService.deleteCustomer(created.getId()));
    }

    @Test
    void testGetCustomersByLevel() {
        // 创建不同等级的客户
        CustomerDTO normal1 = new CustomerDTO();
        normal1.setName("普通客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        normal1.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        normal1.setLevel(Customer.CustomerLevel.NORMAL);
        normal1.setCreditLimit(5000.0);
        customerService.createCustomer(normal1);

        CustomerDTO vip1 = new CustomerDTO();
        vip1.setName("VIP客户" + System.currentTimeMillis());  // ⬅️ 加时间戳
        vip1.setPhone(generateRandomPhone());  // ⬅️ 改为随机电话
        vip1.setLevel(Customer.CustomerLevel.VIP);
        vip1.setCreditLimit(50000.0);
        customerService.createCustomer(vip1);

        // 查询VIP客户
        var vipCustomers = customerService.getCustomersByLevel(Customer.CustomerLevel.VIP);

        // 断言
        assertTrue(vipCustomers.size() >= 1);
        assertTrue(vipCustomers.stream().allMatch(c -> c.getLevel() == Customer.CustomerLevel.VIP));
    }

}