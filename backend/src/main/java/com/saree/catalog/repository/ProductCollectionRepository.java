package com.saree.catalog.repository;

import com.saree.catalog.entity.ProductCollection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCollectionRepository extends JpaRepository<ProductCollection, UUID> {
    List<ProductCollection> findAllByProductId(UUID productId);

    void deleteByProductId(UUID productId);
}
