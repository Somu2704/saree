package com.saree.catalog.service;

import com.saree.catalog.dto.CategoryRequest;
import com.saree.catalog.dto.CategoryResponse;
import com.saree.catalog.entity.Category;
import com.saree.catalog.repository.CategoryRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {
        String slug = normalizeSlug(request.slug());
        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Category with slug already exists");
        }

        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setParentId(null);

        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findBySlug(normalizeSlug(slug))
                .map(CategoryResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String slug = normalizeSlug(request.slug());
        if (!slug.equals(category.getSlug()) && categoryRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Category with slug already exists");
        }

        category.setName(request.name().trim());
        category.setSlug(slug);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
