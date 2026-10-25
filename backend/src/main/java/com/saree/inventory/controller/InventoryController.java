package com.saree.inventory.controller;

import com.saree.inventory.dto.InventoryAdjustRequest;
import com.saree.inventory.dto.InventoryResponse;
import com.saree.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory", description = "Stock visibility and adjustment APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/products/{productId}")
    @Operation(summary = "Fetch inventory for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory returned successfully"),
            @ApiResponse(responseCode = "400", description = "Product not found or invalid ID")
    })
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @PostMapping("/admin/inventory/products/{productId}/adjust")
    @Operation(summary = "Adjust available stock for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock adjusted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or bad request payload")
    })
    public ResponseEntity<InventoryResponse> adjust(@PathVariable UUID productId, @Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, request));
    }

    @PostMapping("/admin/inventory/products/{productId}/reserve")
    @Operation(summary = "Reserve stock against an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid request")
    })
    public ResponseEntity<InventoryResponse> reserve(@PathVariable UUID productId, @Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.reserve(productId, request.quantity()));
    }

    @PostMapping("/admin/inventory/products/{productId}/release")
    @Operation(summary = "Release reserved stock back to available stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid release quantity or bad request")
    })
    public ResponseEntity<InventoryResponse> release(@PathVariable UUID productId, @Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(inventoryService.release(productId, request.quantity()));
    }
}
