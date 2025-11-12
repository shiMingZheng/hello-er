package com.yourcompany.erp.product.service;

import com.yourcompany.erp.common.exception.BusinessException;
import com.yourcompany.erp.product.dto.ProductDTO;
import com.yourcompany.erp.product.dto.ProductVO;
import com.yourcompany.erp.product.entity.Product;
import com.yourcompany.erp.product.repository.ProductRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 创建商品
     */
    @Transactional
    public ProductVO createProduct(ProductDTO dto) {
        // 检查商品编码是否重复
        if (dto.getCode() != null && productRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("商品编码已存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(1);  // 默认上架
        
        product = productRepository.save(product);
        log.info("商品 {} 创建成功，ID: {}", product.getName(), product.getId());
        
        return convertToVO(product);
    }

    /**
     * 更新商品
     */
    @Transactional
    public ProductVO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查商品编码是否重复
        if (dto.getCode() != null && !dto.getCode().equals(product.getCode())) {
            if (productRepository.existsByCode(dto.getCode())) {
                throw new BusinessException("商品编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, product, "id", "status", "createTime");
        product = productRepository.save(product);
        
        log.info("商品 {} 更新成功", product.getName());
        return convertToVO(product);
    }

    /**
     * 删除商品（软删除：下架）
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        product.setStatus(0);  // 下架
        productRepository.save(product);
        
        log.info("商品 {} 已下架", product.getName());
    }

    /**
     * 获取商品详情
     */
    public ProductVO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return convertToVO(product);
    }

    /**
     * 获取商品列表（分页）
     */
    public Page<ProductVO> getProductList(String keyword, Integer status, Pageable pageable) {
        
        // 动态构建查询条件
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 关键字查询 (名称 or 编码)
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), likePattern),
                        cb.like(root.get("code"), likePattern)
                ));
            }

            // 2. 状态查询
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable)
                .map(this::convertToVO);
    }

    /**
     * 获取上架商品列表
     */
    public List<ProductVO> getOnlineProducts() {
        return productRepository.findByStatus(1).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取库存预警商品
     */
    public List<ProductVO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 扣减库存
     */
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        int rows = productRepository.reduceStock(productId, quantity);
        if (rows == 0) {
            throw new BusinessException("库存不足");
        }
        log.info("商品 {} 库存扣减 {}", productId, quantity);
    }

    /**
     * 增加库存
     */
    @Transactional
    public void addStock(Long productId, Integer quantity) {
        productRepository.addStock(productId, quantity);
        log.info("商品 {} 库存增加 {}", productId, quantity);
    }

    /**
     * 转换为 VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        vo.setIsLowStock(product.getStock() <= product.getStockWarning());
        return vo;
    }

}