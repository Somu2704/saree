package com.saree.media.dto;

import com.saree.media.entity.Media;
import com.saree.media.entity.MediaStatus;
import com.saree.media.entity.MediaType;
import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        UUID productId,
        String originalFilename,
        String storageKey,
        String mimeType,
        MediaType mediaType,
        Long fileSizeBytes,
        String cdnUrl,
        Integer width,
        Integer height,
        Integer durationSeconds,
        Boolean primaryImage,
        Integer sortOrder,
        MediaStatus status,
        Instant createdAt,
        Instant updatedAt,
        String uploadUrl) {

    public static MediaResponse from(Media media, String uploadUrl) {
        return new MediaResponse(
                media.getId(),
                media.getProduct().getId(),
                media.getOriginalFilename(),
                media.getStorageKey(),
                media.getMimeType(),
                media.getMediaType(),
                media.getFileSizeBytes(),
                media.getCdnUrl(),
                media.getWidth(),
                media.getHeight(),
                media.getDurationSeconds(),
                media.getPrimaryImage(),
                media.getSortOrder(),
                media.getStatus(),
                media.getCreatedAt(),
                media.getUpdatedAt(),
                uploadUrl);
    }
}
