package com.saree.catalog.repository;

import com.saree.catalog.entity.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    Optional<Collection> findBySlug(String slug);
}
