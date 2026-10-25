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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

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
        log.debug("Inventory fetched for product {} -> available={}, reserved={}", productId, item.getAvailableQuantity(), item.getReservedQuantity());
        return InventoryResponse.from(item);
    }

    public InventoryResponse adjustStock(UUID productId, InventoryAdjustRequest request) {
        ensureProductExists(productId);
        if (request.quantity() == null) {
            log.warn("Inventory adjustment rejected for product {}: quantity missing", productId);
            throw new IllegalArgumentException("Quantity is required");
        }

        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));

        int newAvailable = item.getAvailableQuantity() + request.quantity();
        if (newAvailable < 0) {
            log.warn("Inventory adjustment rejected for product {}: negative value {}", productId, newAvailable);
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }

        item.setAvailableQuantity(newAvailable);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.ADJUSTMENT, request.quantity(), request.reason(), request.referenceType(), request.referenceId()));
        log.info("Inventory adjusted for product {} by {} units; new available={}", productId, request.quantity(), saved.getAvailableQuantity());
        return InventoryResponse.from(saved);
    }

    public InventoryResponse reserve(UUID productId, Integer quantity) {
        ensureProductExists(productId);
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));
        if (quantity == null || quantity <= 0) {
            log.warn("Reservation rejected for product {}: quantity invalid {}", productId, quantity);
            throw new IllegalArgumentException("Reserve quantity must be positive");
        }
        if (item.getAvailableQuantity() < quantity) {
            log.warn("Reservation rejected for product {}: insufficient stock available={}, requested={}", productId, item.getAvailableQuantity(), quantity);
            throw new IllegalArgumentException("Not enough available quantity to reserve");
        }

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.RESERVE, quantity, "product reservation", "order", null));
        log.info("Reserved {} units for product {}. available={}, reserved={}", quantity, productId, saved.getAvailableQuantity(), saved.getReservedQuantity());
        return InventoryResponse.from(saved);
    }

    public InventoryResponse release(UUID productId, Integer quantity) {
        ensureProductExists(productId);
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> createItem(productId, 0));
        if (quantity == null || quantity <= 0) {
            log.warn("Release rejected for product {}: quantity invalid {}", productId, quantity);
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        if (item.getReservedQuantity() < quantity) {
            log.warn("Release rejected for product {}: insufficient reserved stock reserved={}, requested={}", productId, item.getReservedQuantity(), quantity);
            throw new IllegalArgumentException("Reserved quantity is insufficient to release");
        }

        item.setReservedQuantity(item.getReservedQuantity() - quantity);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryLedgerRepository.save(buildLedger(productId, InventoryChangeType.RELEASE, quantity, "release reservation", "order", null));
        log.info("Released {} units for product {}. available={}, reserved={}", quantity, productId, saved.getAvailableQuantity(), saved.getReservedQuantity());
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
