package com.quotepilot.repository;

import com.quotepilot.entity.RfxItem;
import com.quotepilot.entity.RfxItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RfxItemRepository
        extends JpaRepository<RfxItem, RfxItemId> {

    List<RfxItem> findByRfxId(String rfxId);

    Optional<RfxItem> findByRfxIdAndItemId(
            String rfxId,
            String itemId
    );

    boolean existsByRfxIdAndItemId(
            String rfxId,
            String itemId
    );
}