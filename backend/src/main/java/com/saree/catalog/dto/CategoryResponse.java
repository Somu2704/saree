package com.saree.catalog.dto;

import com.saree.catalog.entity.Category;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, String slug, UUID parentId, Instant createdAt, Instant updatedAt) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParentId(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
