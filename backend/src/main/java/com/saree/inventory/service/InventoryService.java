package com.saree.inventory.service;

import com.saree.catalog.repository.ProductRepository;
import com.saree.inventory.dto.InventoryAdjustRequest;
import com.saree.inventory.dto.InventoryResponse;
import com.saree.inventory.entity.InventoryChangeType;
import com.saree.inventory.entity.InventoryItem;
import com.saree.inventory.entity.InventoryLedger;
import com.saree.inventory.repository.InventoryItemRepository;
import com.saree.inventory.repository.InventoryLedgerRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            InventoryLedgerRepository inventoryLedgerRepository,
            ProductRepository productRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.productRepository = productRepository;
    }

    public InventoryResponse getByProductId(UUID productId) {
        ensureProductExists(productId);
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));
        return InventoryResponse.from(item);
    }

    public InventoryResponse adjustStock(UUID productId, InventoryAdjustRequest request) {
        ensureProductExists(productId);
        if (request.quantity() == null) {
            throw new IllegalArgumentException("Quantity is required");
        }

        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));

        int newAvailable = item.getAvailableQuantity() + request.quantity();
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }

        item.setAvailableQuantity(newAvailable);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.ADJUSTMENT, request.quantity(), request.reason(), request.referenceType(), request.referenceId()));
        return InventoryResponse.from(saved);
    }

    public InventoryResponse reserve(UUID productId, Integer quantity) {
        ensureProductExists(productId);
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Reserve quantity must be positive");
        }
        if (item.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough available quantity to reserve");
        }

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.RESERVE, quantity, "product reservation", "order", null));
        return InventoryResponse.from(saved);
    }

    public InventoryResponse release(UUID productId, Integer quantity) {
        ensureProductExists(productId);
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        if (item.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException("Reserved quantity is insufficient to release");
        }

        item.setReservedQuantity(item.getReservedQuantity() - quantity);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.RELEASE, quantity, "release reservation", "order", null));
        return InventoryResponse.from(saved);
    }

    private InventoryItem createItem(UUID productId, int initialQuantity) {
        InventoryItem item = new InventoryItem();
        item.setProductId(productId);
        item.setAvailableQuantity(initialQuantity);
        item.setReservedQuantity(0);
        return inventoryItemRepository.save(item);
    }

    private void ensureProductExists(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
    }

    private InventoryLedger buildLedger(UUID productId, InventoryChangeType type, Integer quantity, String reason, String referenceType, String referenceId) {
        InventoryLedger ledger = new InventoryLedger();
        ledger.setProductId(productId);
        ledger.setChangeType(type);
        ledger.setQuantity(quantity);
        ledger.setReason(reason == null ? "inventory update" : reason);
        ledger.setReferenceType(referenceType);
        if (referenceId != null) {
            try {
                ledger.setReferenceId(UUID.fromString(referenceId));
            } catch (IllegalArgumentException ex) {
                ledger.setReferenceId(null);
            }
        }
        return ledger;
    }
}
