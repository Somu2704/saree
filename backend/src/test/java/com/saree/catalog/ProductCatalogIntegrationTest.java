package com.saree.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class ProductCatalogIntegrationTest {

    @Autowired
    private ProductService productService;

    @Test
    void createAndListProducts_shouldWork() {
        ProductRequest request = new ProductRequest(
                "SKU-1001",
                "Mira Silk Saree",
                "mira-silk-saree",
                "Rich silk saree with handwork border.",
                "Silk",
                "Royal Blue",
                "Wedding",
                new BigDecimal("2499.00"),
                new BigDecimal("2199.00"),
                12,
                "ACTIVE"
        );

        var created = productService.create(request);
        assertThat(created.name()).isEqualTo("Mira Silk Saree");
        assertThat(created.slug()).isEqualTo("mira-silk-saree");

        var products = productService.list(PageRequest.of(0, 10));
        assertThat(products.getContent()).isNotEmpty();
    }
}
