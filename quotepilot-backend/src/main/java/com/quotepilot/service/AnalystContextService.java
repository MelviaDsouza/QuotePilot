package com.quotepilot.service;

import com.quotepilot.dto.AwardRecommendation;
import com.quotepilot.dto.QuoteComparisonResult;
import com.quotepilot.entity.RfxItem;
import com.quotepilot.repository.RfxItemRepository;
import com.quotepilot.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalystContextService {

    private final VendorRepository vendorRepository;
    private final VendorQualificationService vendorQualificationService;
    private final AwardRecommendationService awardRecommendationService;
    private final QuoteComparisonService quoteComparisonService;
    private final RfxItemRepository rfxItemRepository;

    public String buildContext(String rfxId, String question) {

        // Supplier qualification context
        String supplierContext = vendorRepository.findAll()
                .stream()
                .map(vendor -> {
                    boolean eligible =
                            vendorQualificationService.isEligible(vendor);

                    String reasons = String.valueOf(
                            vendorQualificationService
                                    .getIneligibilityReasons(vendor)
                    );

                    return """
                            Supplier: %s (%s)
                            ISO 9001: %s
                            Rejection Rate: %s%%
                            Eligible: %s
                            Exclusion Reasons: %s
                            """.formatted(
                            vendor.getVendorName(),
                            vendor.getVendorId(),
                            vendor.getIso9001Certified(),
                            vendor.getRejectionRatePercent(),
                            eligible,
                            reasons
                    );
                })
                .collect(Collectors.joining("\n"));

        // Identify RFx item IDs mentioned in the question
        List<String> itemIds = rfxItemRepository.findByRfxId(rfxId)
                .stream()
                .map(RfxItem::getItemId)
                .filter(itemId -> question.toUpperCase()
                        .contains(itemId.toUpperCase()))
                .toList();

        // Specific item questions
        if (!itemIds.isEmpty()) {

            String quoteContext = itemIds.stream()
                    .map(itemId -> {

                        List<QuoteComparisonResult> quotes =
                                quoteComparisonService.compareItem(
                                        rfxId, itemId
                                );

                        String details = quotes.stream()
                                .map(q -> {

                                    String freightTreatment;

                                    if (q.isFreightIncluded()) {
                                        freightTreatment =
                                                "Freight included in quoted price";
                                    } else if ("PER_SHIPMENT".equalsIgnoreCase(
                                            String.valueOf(q.getFreightType()))) {
                                        freightTreatment =
                                                "Additional freight of INR "
                                                        + q.getFreightAmount()
                                                        + " PER SHIPMENT. "
                                                        + "This is NOT a per-box charge. "
                                                        + "Freight allocation per box "
                                                        + "is NOT calculated in this "
                                                        + "comparison.";
                                    } else {
                                        freightTreatment =
                                                "Additional freight: INR "
                                                        + q.getFreightAmount()
                                                        + ", type: "
                                                        + q.getFreightType()
                                                        + ". Per-unit allocation "
                                                        + "not available.";
                                    }

                                    return """
                                            Supplier: %s
                                            Vendor ID: %s
                                            Quote ID: %s
                                            Original Price: %s %s / %s
                                            Normalized Unit Price INR: %.2f
                                            Normalized Price Includes Freight: %s
                                            Freight Treatment: %s
                                            Eligible: %s
                                            Exclusion Reason: %s
                                            Backend Winner: %s

                                            """.formatted(
                                            q.getVendorName(),
                                            q.getVendorId(),
                                            q.getQuoteId(),
                                            q.getQuotedPrice(),
                                            q.getCurrency(),
                                            q.getQuotedUnit(),
                                            q.getNormalizedPriceInr(),
                                            q.isFreightIncluded(),
                                            freightTreatment,
                                            q.isEligible(),
                                            q.getExclusionReason(),
                                            q.isWinner()
                                    );
                                })
                                .collect(Collectors.joining("\n"));

                        return "ITEM: " + itemId + "\n" + details;
                    })
                    .collect(Collectors.joining("\n"));

            return """
                    SUPPLIER QUALIFICATION:
                    %s

                    ITEM QUOTE COMPARISONS:
                    %s

                    IMPORTANT COST INTERPRETATION:
                    - Normalized unit price is the backend-calculated price.
                    - Do not add shipment-level freight directly to a unit price.
                    - Shipment freight is not equivalent to per-unit freight.
                    - Do not claim an exact landed unit price unless
                      freight allocation has been calculated by the backend.
                    - Backend Winner is the authoritative item award decision.
                    """.formatted(
                    supplierContext,
                    quoteContext
            );
        }

        // General questions get compact award summary
        AwardRecommendation recommendation =
                awardRecommendationService.generateRecommendation(rfxId);

        return """
                SUPPLIER QUALIFICATION:
                %s

                AWARD SUMMARY:
                Total Award Value: INR %.2f
                Total Additional Freight: INR %.2f
                Total Landed Cost: INR %.2f
                Awarded Items: %d
                Unawarded Items: %d

                IMPORTANT:
                - These award totals are calculated by the backend.
                - Do not independently recalculate award totals.
                - Do not invent per-unit freight allocations.
                - Explain backend award results using these values.
                """.formatted(
                supplierContext,
                recommendation.getQuotedAwardValueInr(),
                recommendation.getAdditionalFreightInr(),
                recommendation.getTotalLandedCostInr(),
                recommendation.getAwardedItems(),
                recommendation.getUnawardedItems()
        );
    }
}