package com.saree.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "SKU is required") String sku,
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Slug is required") String slug,
        @NotBlank(message = "Description is required") String description,
        @NotBlank(message = "Fabric is required") String fabric,
        @NotBlank(message = "Color is required") String color,
        @NotBlank(message = "Occasion is required") String occasion,
        @NotNull(message = "Base price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero") BigDecimal basePrice,
        BigDecimal salePrice,
        Integer stockQuantity,
        String status,
        List<UUID> categoryIds,
        List<UUID> collectionIds) {
}
