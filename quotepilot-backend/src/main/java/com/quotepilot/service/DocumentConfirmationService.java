
package com.quotepilot.service;

import com.quotepilot.dto.DocumentConfirmationRequest;
import com.quotepilot.entity.Vendor;
import com.quotepilot.entity.VendorQuote;
import com.quotepilot.model.FreightType;
import com.quotepilot.repository.RfxItemRepository;
import com.quotepilot.repository.VendorQuoteRepository;
import com.quotepilot.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentConfirmationService {

    private final VendorRepository vendorRepository;
    private final RfxItemRepository rfxItemRepository;
    private final VendorQuoteRepository vendorQuoteRepository;
    private final PriceNormalizationService priceNormalizationService;

    @Transactional
    public List<VendorQuote> confirmDocument(
            String rfxId,
            DocumentConfirmationRequest request) {

        if (request.getVendorId() == null
                || request.getVendorId().isBlank()) {
            throw new IllegalArgumentException("Vendor ID is required");
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Vendor not found"));

        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new IllegalArgumentException("No quote lines provided");
        }

        List<VendorQuote> quotes = new ArrayList<>();

        for (DocumentConfirmationRequest.ConfirmedQuoteLine line
                : request.getLines()) {

            if (line.getItemId() == null || line.getItemId().isBlank()) {
                throw new IllegalArgumentException("Item ID is required");
            }

            if (!rfxItemRepository.existsByRfxIdAndItemId(
                    rfxId,
                    line.getItemId())) {

                throw new IllegalArgumentException(
                        "RFx item not found: " + line.getItemId()
                );
            }

            if (line.getQuotedPrice() == null
                    || line.getQuotedPrice() <= 0) {
                throw new IllegalArgumentException(
                        "Quoted price must be greater than zero");
            }

            if (line.getCurrency() == null
                    || line.getQuotedUnit() == null) {
                throw new IllegalArgumentException(
                        "Currency and quoted unit are required");
            }

            // Validate supported currency/unit combinations.
            priceNormalizationService.normalizePrice(
                    line.getQuotedPrice(),
                    line.getCurrency(),
                    line.getQuotedUnit(),
                    line.getQuantity()
            );

            VendorQuote quote = vendorQuoteRepository
                    .findByRfxIdAndVendorIdAndItemId(
                            rfxId,
                            vendor.getVendorId(),
                            line.getItemId()
                    )
                    .orElseGet(VendorQuote::new);

            if (quote.getQuoteId() == null) {
                quote.setQuoteId("Q-" + UUID.randomUUID());
            }
            quote.setRfxId(rfxId);
            quote.setVendorId(vendor.getVendorId());
            quote.setVendorName(vendor.getVendorName());
            quote.setItemId(line.getItemId());
            quote.setQuotedPrice(line.getQuotedPrice());
            quote.setCurrency(line.getCurrency());
            quote.setQuotedUnit(line.getQuotedUnit());

            quote.setQuotedQuantity(
                    line.getQuantity() == null
                            ? null
                            : line.getQuantity().intValue());

            quote.setFreightIncluded(
                    "INCLUDED".equalsIgnoreCase(line.getFreightTerms()));

            quote.setFreightAmount(
                    line.getFreightAmount() == null
                            ? 0.0
                            : line.getFreightAmount()
            );
            quote.setFreightType(
                    quote.getFreightIncluded()
                            ? FreightType.NONE
                            : FreightType.PER_SHIPMENT);

            quotes.add(quote);
        }

        return vendorQuoteRepository.saveAll(quotes);
    }
}
