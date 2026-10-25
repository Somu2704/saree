package com.saree.catalog.service;

import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.dto.ProductResponse;
import com.saree.catalog.entity.Product;
import com.saree.catalog.entity.ProductCategory;
import com.saree.catalog.entity.ProductCollection;
import com.saree.catalog.entity.ProductStatus;
import com.saree.catalog.repository.CategoryRepository;
import com.saree.catalog.repository.CollectionRepository;
import com.saree.catalog.repository.ProductCategoryRepository;
import com.saree.catalog.repository.ProductCollectionRepository;
import com.saree.catalog.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCollectionRepository productCollectionRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CollectionRepository collectionRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductCollectionRepository productCollectionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCollectionRepository = productCollectionRepository;
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
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : ProductStatus.valueOf(request.status().toUpperCase()));

        Product savedProduct = productRepository.save(product);
        syncProductCategories(savedProduct, request.categoryIds());
        syncProductCollections(savedProduct, request.collectionIds());

        return toResponse(savedProduct);
    }

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::toResponse)
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

        Product savedProduct = productRepository.save(product);
        syncProductCategories(savedProduct, request.categoryIds());
        syncProductCollections(savedProduct, request.collectionIds());
        return toResponse(savedProduct);
    }

    private ProductResponse toResponse(Product product) {
        List<UUID> categoryIds = productCategoryRepository.findAllByProductId(product.getId()).stream()
                .map(ProductCategory::getCategoryId)
                .toList();
        List<UUID> collectionIds = productCollectionRepository.findAllByProductId(product.getId()).stream()
                .map(ProductCollection::getCollectionId)
                .toList();
        return ProductResponse.from(product, categoryIds, collectionIds);
    }

    private void syncProductCategories(Product product, List<UUID> categoryIds) {
        productCategoryRepository.deleteByProductId(product.getId());
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        for (UUID categoryId : categoryIds) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new IllegalArgumentException("Category not found: " + categoryId);
            }
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductId(product.getId());
            productCategory.setCategoryId(categoryId);
            productCategoryRepository.save(productCategory);
        }
    }

    private void syncProductCollections(Product product, List<UUID> collectionIds) {
        productCollectionRepository.deleteByProductId(product.getId());
        if (collectionIds == null || collectionIds.isEmpty()) {
            return;
        }

        for (UUID collectionId : collectionIds) {
            if (!collectionRepository.existsById(collectionId)) {
                throw new IllegalArgumentException("Collection not found: " + collectionId);
            }
            ProductCollection productCollection = new ProductCollection();
            productCollection.setProductId(product.getId());
            productCollection.setCollectionId(collectionId);
            productCollectionRepository.save(productCollection);
        }
    }
}
