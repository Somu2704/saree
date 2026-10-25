package com.saree.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryAdjustRequest(
        @NotNull(message = "Quantity is required") Integer quantity,
        String reason,
        String referenceType,
        String referenceId) {
}
