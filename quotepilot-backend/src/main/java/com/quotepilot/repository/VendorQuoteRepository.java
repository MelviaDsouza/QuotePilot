package com.quotepilot.repository;

import com.quotepilot.entity.VendorQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorQuoteRepository
        extends JpaRepository<VendorQuote, String> {

    List<VendorQuote> findByItemId(String itemId);

    List<VendorQuote> findAllByItemIdIn(List<String> itemIds);

    List<VendorQuote> findByRfxId(String rfxId);

    List<VendorQuote> findByRfxIdAndItemId(String rfxId, String itemId);

    Optional<VendorQuote> findByRfxIdAndVendorIdAndItemId(
            String rfxId,
            String vendorId,
            String itemId
    );

}
