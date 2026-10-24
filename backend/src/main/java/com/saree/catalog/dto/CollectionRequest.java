package com.saree.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CollectionRequest(@NotBlank(message = "Collection name is required") String name,
                               @NotBlank(message = "Collection slug is required") String slug,
                               String bannerImage,
                               Boolean active) {
}
