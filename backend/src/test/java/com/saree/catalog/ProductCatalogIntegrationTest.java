package com.saree.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.saree.catalog.dto.CategoryRequest;
import com.saree.catalog.dto.CollectionRequest;
import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.service.CategoryService;
import com.saree.catalog.service.CollectionService;
import com.saree.catalog.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class ProductCatalogIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CollectionService collectionService;

    @Test
    void createAndListProducts_shouldWork() {
        var category = categoryService.create(new CategoryRequest("Cotton Sarees", "cotton-sarees"));
        var collection = collectionService.create(new CollectionRequest("Festive Edit", "festive-edit", "https://cdn.example.com/festive.jpg", true));

        ProductRequest request = new ProductRequest(
                "SKU-1001",
                "Mira Cotton Saree",
                "mira-cotton-saree",
                "Breathable cotton saree for festive wear.",
                "Cotton",
                "Mustard",
                "Festive",
                new BigDecimal("2499.00"),
                new BigDecimal("2199.00"),
                12,
                "ACTIVE",
                List.of(category.id()),
                List.of(collection.id())
        );

        var created = productService.create(request);
        assertThat(created.name()).isEqualTo("Mira Cotton Saree");
        assertThat(created.slug()).isEqualTo("mira-cotton-saree");
        assertThat(created.categoryIds()).contains(category.id());
        assertThat(created.collectionIds()).contains(collection.id());

        var products = productService.list(PageRequest.of(0, 10));
        assertThat(products.getContent()).isNotEmpty();
    }
}
