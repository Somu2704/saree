package com.saree.catalog.repository;

import com.saree.catalog.entity.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);

    Page<Product> findByStatus(com.saree.catalog.entity.ProductStatus status, Pageable pageable);
}
