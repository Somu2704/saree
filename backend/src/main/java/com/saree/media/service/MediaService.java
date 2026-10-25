package com.saree.media.service;

import com.saree.catalog.entity.Product;
import com.saree.catalog.repository.ProductRepository;
import com.saree.media.dto.MediaRequest;
import com.saree.media.dto.MediaResponse;
import com.saree.media.entity.Media;
import com.saree.media.entity.MediaStatus;
import com.saree.media.entity.MediaType;
import com.saree.media.repository.MediaRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MediaService {

    private static final String CDN_BASE_URL = "https://cdn.saree.local";

    private final MediaRepository mediaRepository;
    private final ProductRepository productRepository;

    public MediaService(MediaRepository mediaRepository, ProductRepository productRepository) {
        this.mediaRepository = mediaRepository;
        this.productRepository = productRepository;
    }

    public MediaResponse requestUpload(MediaRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        MediaType mediaType = MediaType.valueOf(request.mediaType().trim().toUpperCase(Locale.ROOT));
        String sanitizedFileName = sanitizeFilename(request.originalFilename());
        String storageKey = "products/" + product.getId() + "/" + UUID.randomUUID() + "/" + sanitizedFileName;
        String candidateCdnUrl = CDN_BASE_URL + "/" + storageKey;

        Media media = new Media();
        media.setProduct(product);
        media.setMediaType(mediaType);
        media.setOriginalFilename(sanitizedFileName);
        media.setStorageKey(storageKey);
        media.setMimeType(request.mimeType());
        media.setFileSizeBytes(request.fileSizeBytes());
        media.setCdnUrl(candidateCdnUrl);
        media.setWidth(request.width());
        media.setHeight(request.height());
        media.setDurationSeconds(request.durationSeconds());
        media.setPrimaryImage(Boolean.TRUE.equals(request.primaryImage()));
        media.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        media.setStatus(MediaStatus.PENDING_UPLOAD);

        Media saved = mediaRepository.save(media);
        String uploadUrl = generateUploadUrl(storageKey);
        return MediaResponse.from(saved, uploadUrl);
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> listForProduct(UUID productId) {
        return mediaRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(media -> MediaResponse.from(media, generateUploadUrl(media.getStorageKey())))
                .toList();
    }

    @Transactional(readOnly = true)
    public MediaResponse getById(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        return MediaResponse.from(media, generateUploadUrl(media.getStorageKey()));
    }

    public void delete(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        mediaRepository.delete(media);
    }

    private String generateUploadUrl(String storageKey) {
        return "https://upload.saree.local/" + storageKey + "?policy=temporary&expires=3600";
    }

    private String sanitizeFilename(String value) {
        String clean = value == null ? "upload" : value.trim();
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "-");
        return clean.isBlank() ? "upload" : clean;
    }
}
