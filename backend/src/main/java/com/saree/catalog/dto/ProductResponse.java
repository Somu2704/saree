package com.saree.catalog.dto;

import com.saree.catalog.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String slug,
        String description,
        String fabric,
        String color,
        String occasion,
        BigDecimal basePrice,
        BigDecimal salePrice,
        String currency,
        Integer stockQuantity,
        String status,
        List<UUID> categoryIds,
        List<UUID> collectionIds,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return from(product, List.of(), List.of());
    }

    public static ProductResponse from(Product product, List<UUID> categoryIds, List<UUID> collectionIds) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getFabric(),
                product.getColor(),
                product.getOccasion(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getCurrency(),
                product.getStockQuantity(),
                product.getStatus().name(),
                categoryIds,
                collectionIds,
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
