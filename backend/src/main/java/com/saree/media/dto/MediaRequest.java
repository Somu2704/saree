package com.saree.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MediaRequest(
        @NotNull(message = "Product id is required") UUID productId,
        @NotBlank(message = "Original file name is required") String originalFilename,
        @NotBlank(message = "Content type is required") String mimeType,
        @NotBlank(message = "Media type is required") String mediaType,
        @NotNull(message = "File size is required") Long fileSizeBytes,
        Integer width,
        Integer height,
        Integer durationSeconds,
        Boolean primaryImage,
        Integer sortOrder) {
}
