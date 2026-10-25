package com.saree.catalog.repository;

import com.saree.catalog.entity.ProductCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findAllByProductId(UUID productId);

    void deleteByProductId(UUID productId);
}
