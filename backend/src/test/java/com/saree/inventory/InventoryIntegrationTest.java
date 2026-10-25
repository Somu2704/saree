package com.saree.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.service.ProductService;
import com.saree.inventory.dto.InventoryAdjustRequest;
import com.saree.inventory.service.InventoryService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void reserveAndReleaseFlow_shouldUpdateAvailability() {
        var product = productService.create(new ProductRequest(
                "COT-3001",
                "Cotton Weave Saree",
                "cotton-weave-saree",
                "Crafted cotton saree.",
                "Cotton",
                "Rose",
                "Festive",
                new BigDecimal("1799.00"),
                new BigDecimal("1599.00"),
                15,
                "ACTIVE",
                null,
                null));

        var initial = inventoryService.getByProductId(product.id());
        assertThat(initial.availableQuantity()).isEqualTo(15);

        inventoryService.adjustStock(product.id(), new InventoryAdjustRequest(5, "restock", "supplier", UUID.randomUUID().toString()));
        var afterAdjust = inventoryService.getByProductId(product.id());
        assertThat(afterAdjust.availableQuantity()).isEqualTo(20);

        var reserved = inventoryService.reserve(product.id(), 6);
        assertThat(reserved.availableQuantity()).isEqualTo(14);
        assertThat(reserved.reservedQuantity()).isEqualTo(6);

        var released = inventoryService.release(product.id(), 3);
        assertThat(released.availableQuantity()).isEqualTo(17);
        assertThat(released.reservedQuantity()).isEqualTo(3);
    }
}
