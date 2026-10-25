package com.saree.inventory.repository;

import com.saree.inventory.entity.InventoryLedger;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, UUID> {
}
