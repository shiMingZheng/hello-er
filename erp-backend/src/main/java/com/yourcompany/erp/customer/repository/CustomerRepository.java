package com.yourcompany.erp.customer.repository;

import com.yourcompany.erp.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// ⬇️ 添加这两行 import
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * 根据客户名称查询
     */
    boolean existsByName(String name);
	// 在 CustomerRepository.java 中添加以下方法

	/**
	* 模糊查询客户名称
	*/
	List<Customer> findByNameContaining(String name);
	
	/**
	* 根据电话查询
	*/
	Optional<Customer> findByPhone(String phone);
	
	/**
	* 根据等级查询
	*/
	List<Customer> findByLevel(Customer.CustomerLevel level);

}
