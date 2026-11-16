
package com.quotepilot.service;

import com.quotepilot.dto.QuoteComparisonResult;
import com.quotepilot.dto.RfxComparisonResult;
import com.quotepilot.entity.Vendor;
import com.quotepilot.entity.VendorQuote;
import com.quotepilot.repository.VendorQuoteRepository;
import com.quotepilot.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuoteComparisonService {

    private final VendorQuoteRepository quoteRepository;
    private final VendorRepository vendorRepository;
    private final VendorQualificationService qualificationService;
    private final PriceNormalizationService normalizationService;

    public QuoteComparisonService(
            VendorQuoteRepository quoteRepository,
            VendorRepository vendorRepository,
            VendorQualificationService qualificationService,
            PriceNormalizationService normalizationService) {

        this.quoteRepository = quoteRepository;
        this.vendorRepository = vendorRepository;
        this.qualificationService = qualificationService;
        this.normalizationService = normalizationService;
    }

    public List<RfxComparisonResult> compareAllItems(
            String rfxId,
            List<String> itemIds) {

        List<VendorQuote> quotes =
                quoteRepository.findByRfxId(rfxId).stream()
                        .filter(q -> itemIds.contains(q.getItemId()))
                        .toList();

        List<String> vendorIds = quotes.stream()
                .map(VendorQuote::getVendorId)
                .distinct()
                .toList();

        Map<String, Vendor> vendors =
                vendorRepository.findAllByVendorIdIn(vendorIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Vendor::getVendorId,
                                Function.identity()
                        ));

        List<QuoteComparisonResult> results = quotes.stream()
                .map(quote -> buildResult(
                        quote,
                        vendors.get(quote.getVendorId())))
                .toList();

        Map<String, List<QuoteComparisonResult>> grouped =
                results.stream()
                        .collect(Collectors.groupingBy(
                                QuoteComparisonResult::getItemId
                        ));

        grouped.values().forEach(this::markWinner);

        return grouped.entrySet().stream()
                .map(entry -> new RfxComparisonResult(
                        entry.getKey(),
                        entry.getValue()))
                .toList();
    }

    public List<QuoteComparisonResult> compareItem(
            String rfxId,
            String itemId) {

        List<VendorQuote> quotes =
                quoteRepository.findByRfxIdAndItemId(rfxId, itemId);

        List<String> vendorIds = quotes.stream()
                .map(VendorQuote::getVendorId)
                .distinct()
                .toList();

        Map<String, Vendor> vendors =
                vendorRepository.findAllByVendorIdIn(vendorIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Vendor::getVendorId,
                                Function.identity()
                        ));

        List<QuoteComparisonResult> results = quotes.stream()
                .map(quote -> buildResult(
                        quote,
                        vendors.get(quote.getVendorId())))
                .toList();

        markWinner(results);

        return results;
    }

    private void markWinner(List<QuoteComparisonResult> results) {
        results.stream()
                .filter(QuoteComparisonResult::isEligible)
                .min(Comparator.comparingDouble(
                        QuoteComparisonResult::getNormalizedPriceInr))
                .ifPresent(result -> result.setWinner(true));
    }

    private QuoteComparisonResult buildResult(
            VendorQuote quote, Vendor vendor) {

        if (vendor == null) {
            throw new RuntimeException(
                    "Vendor not found: " + quote.getVendorId());
        }

        List<String> reasons =
                qualificationService.getIneligibilityReasons(vendor);

        QuoteComparisonResult result = new QuoteComparisonResult();

        result.setQuoteId(quote.getQuoteId());
        result.setVendorId(quote.getVendorId());
        result.setVendorName(quote.getVendorName());
        result.setItemId(quote.getItemId());

        result.setQuotedPrice(quote.getQuotedPrice());
        result.setCurrency(quote.getCurrency());
        result.setQuotedUnit(quote.getQuotedUnit());

        result.setFreightIncluded(quote.getFreightIncluded());
        result.setFreightAmount(quote.getFreightAmount());
        result.setFreightType(quote.getFreightType());

        result.setNormalizedPriceInr(
                normalizationService.normalizePrice(
                        quote.getQuotedPrice(),
                        quote.getCurrency(),
                        quote.getQuotedUnit(),
                        quote.getQuotedQuantity() == null
                                ? null
                                : quote.getQuotedQuantity().doubleValue()
                )
        );

        result.setEligible(reasons.isEmpty());
        result.setExclusionReason(
                reasons.isEmpty()
                        ? null
                        : String.join("; ", reasons));

        result.setWinner(false);

        return result;
    }
}
