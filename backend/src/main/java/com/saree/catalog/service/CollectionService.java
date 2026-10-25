package com.saree.catalog.service;

import com.saree.catalog.dto.CollectionRequest;
import com.saree.catalog.dto.CollectionResponse;
import com.saree.catalog.entity.Collection;
import com.saree.catalog.repository.CollectionRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionResponse create(CollectionRequest request) {
        String slug = normalizeSlug(request.slug());
        if (collectionRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Collection with slug already exists");
        }

        Collection collection = new Collection();
        collection.setName(request.name().trim());
        collection.setSlug(slug);
        collection.setBannerImage(request.bannerImage());
        collection.setActive(request.active() == null || request.active());

        return CollectionResponse.from(collectionRepository.save(collection));
    }

    public List<CollectionResponse> list() {
        return collectionRepository.findAll().stream()
                .map(CollectionResponse::from)
                .toList();
    }

    public CollectionResponse getBySlug(String slug) {
        return collectionRepository.findBySlug(normalizeSlug(slug))
                .map(CollectionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
    }

    public CollectionResponse update(UUID id, CollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        String slug = normalizeSlug(request.slug());
        if (!slug.equals(collection.getSlug()) && collectionRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Collection with slug already exists");
        }

        collection.setName(request.name().trim());
        collection.setSlug(slug);
        collection.setBannerImage(request.bannerImage());
        collection.setActive(request.active() == null || request.active());
        return CollectionResponse.from(collectionRepository.save(collection));
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
