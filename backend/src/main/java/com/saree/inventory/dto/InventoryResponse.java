package com.saree.inventory.dto;

import com.saree.inventory.entity.InventoryItem;
import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        Integer availableQuantity,
        Integer reservedQuantity,
        Integer totalQuantity,
        Instant updatedAt) {

    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getProductId(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getAvailableQuantity() + item.getReservedQuantity(),
                item.getUpdatedAt());
    }
}
