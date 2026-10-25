package com.saree.inventory.controller;

import com.saree.inventory.dto.InventoryAdjustRequest;
import com.saree.inventory.dto.InventoryResponse;
import com.saree.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/products/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @PostMapping("/admin/inventory/products/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(@PathVariable UUID productId, @Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, request));
    }

    @PostMapping("/admin/inventory/products/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserve(@PathVariable UUID productId, @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.reserve(productId, request.quantity()));
    }

    @PostMapping("/admin/inventory/products/{productId}/release")
    public ResponseEntity<InventoryResponse> release(@PathVariable UUID productId, @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.release(productId, request.quantity()));
    }
}
