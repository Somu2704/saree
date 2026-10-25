package com.saree.media.repository;

import com.saree.media.entity.Media;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByProductIdOrderBySortOrderAsc(UUID productId);
}
