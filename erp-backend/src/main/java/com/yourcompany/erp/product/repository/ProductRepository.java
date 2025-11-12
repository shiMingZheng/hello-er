package com.yourcompany.erp.product.repository;

import com.yourcompany.erp.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {


    /**
     * 根据商品编码查询
     */
    Optional<Product> findByCode(String code);

    /**
     * 检查商品编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查询上架商品
     */
    List<Product> findByStatus(Integer status);

    /**
     * 扣减库存（防超卖）,clearAutomatically = true强制JPA在执行更新后清除缓存。
     * @return 影响的行数（1=成功，0=库存不足）
     */
	@Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
           "WHERE p.id = :productId AND p.stock >= :quantity")
    int reduceStock(@Param("productId") Long productId, 
                    @Param("quantity") Integer quantity);

    /**
     * 增加库存
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity " +
           "WHERE p.id = :productId")
    int addStock(@Param("productId") Long productId, 
                 @Param("quantity") Integer quantity);

    /**
     * 查询库存预警商品
     */
    @Query("SELECT p FROM Product p WHERE p.stock <= p.stockWarning AND p.status = 1")
    List<Product> findLowStockProducts();
	

}