
package com.quotepilot.service;

import com.quotepilot.entity.VendorQuote;
import com.quotepilot.repository.VendorQuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorQuoteService {

    private final VendorQuoteRepository vendorQuoteRepository;
    private final PriceNormalizationService priceNormalizationService;

    public List<VendorQuote> getAllQuotes() {
        return vendorQuoteRepository.findAll();
    }

    public List<VendorQuote> getQuotesByRfxId(String rfxId) {
        return vendorQuoteRepository.findByRfxId(rfxId);
    }

    public VendorQuote getQuoteById(String quoteId) {
        return vendorQuoteRepository.findById(quoteId)
                .orElseThrow(() ->
                        new RuntimeException("Quote not found: " + quoteId));
    }

    public VendorQuote createQuote(String rfxId, VendorQuote quote) {
        quote.setRfxId(rfxId);
        return vendorQuoteRepository.save(quote);
    }

    public List<VendorQuote> getQuotesByItemId(String itemId) {
        return vendorQuoteRepository.findByItemId(itemId);
    }

    public List<VendorQuote> getQuotesByRfxIdAndItemId(
            String rfxId, String itemId) {
        return vendorQuoteRepository.findByRfxIdAndItemId(rfxId, itemId);
    }

    public double getNormalizedPrice(String quoteId) {
        VendorQuote quote = getQuoteById(quoteId);

        return priceNormalizationService.normalizePrice(
                quote.getQuotedPrice(),
                quote.getCurrency(),
                quote.getQuotedUnit()
        );
    }

    public void deleteQuote(String quoteId) {
        vendorQuoteRepository.deleteById(quoteId);
    }
}
