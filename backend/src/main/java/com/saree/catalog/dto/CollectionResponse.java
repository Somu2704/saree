package com.saree.catalog.dto;

import com.saree.catalog.entity.Collection;
import java.time.Instant;
import java.util.UUID;

public record CollectionResponse(UUID id, String name, String slug, String bannerImage, Boolean active, Instant createdAt, Instant updatedAt) {

    public static CollectionResponse from(Collection collection) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getSlug(),
                collection.getBannerImage(),
                collection.getActive(),
                collection.getCreatedAt(),
                collection.getUpdatedAt());
    }
}
