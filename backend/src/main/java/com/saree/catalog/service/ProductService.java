package com.saree.catalog.service;

import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.dto.ProductResponse;
import com.saree.catalog.entity.Product;
import com.saree.catalog.entity.ProductStatus;
import com.saree.catalog.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.findBySlug(request.slug()).isPresent()) {
            throw new IllegalArgumentException("Product with slug already exists");
        }

        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setFabric(request.fabric());
        product.setColor(request.color());
        product.setOccasion(request.occasion());
        product.setBasePrice(request.basePrice());
        product.setSalePrice(request.salePrice() == null ? request.basePrice() : request.salePrice());
        product.setCurrency("INR");
        product.setStockQuantity(request.stockQuantity() == null ? 0 : request.stockQuantity());
        product.setStatus(ProductStatus.ACTIVE);

        return ProductResponse.from(productRepository.save(product));
    }

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(ProductResponse::from);
    }

    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(ProductResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setSku(request.sku());
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setFabric(request.fabric());
        product.setColor(request.color());
        product.setOccasion(request.occasion());
        product.setBasePrice(request.basePrice());
        product.setSalePrice(request.salePrice() == null ? request.basePrice() : request.salePrice());
        product.setStockQuantity(request.stockQuantity() == null ? product.getStockQuantity() : request.stockQuantity());
        product.setStatus(request.status() == null ? product.getStatus() : ProductStatus.valueOf(request.status().toUpperCase()));

        return ProductResponse.from(productRepository.save(product));
    }
}
