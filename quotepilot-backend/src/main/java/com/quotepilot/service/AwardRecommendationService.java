
package com.quotepilot.service;

import com.quotepilot.dto.*;
import com.quotepilot.entity.RfxItem;
import com.quotepilot.model.FreightType;
import com.quotepilot.repository.RfxItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AwardRecommendationService {

    private final QuoteComparisonService quoteComparisonService;
    private final RfxItemRepository rfxItemRepository;

    public AwardRecommendation generateRecommendation(String rfxId) {

        List<RfxItem> items = rfxItemRepository.findAll().stream()
                .filter(item -> item.getRfxId().equals(rfxId))
                .toList();

        List<String> itemIds = items.stream()
                .map(RfxItem::getItemId)
                .toList();

        List<RfxComparisonResult> comparisons =
                quoteComparisonService.compareAllItems(rfxId,itemIds);

        Map<String, RfxComparisonResult> comparisonMap = new HashMap<>();

        for (RfxComparisonResult comparison : comparisons) {
            comparisonMap.put(comparison.getItemId(), comparison);
        }

        AwardRecommendation recommendation = new AwardRecommendation();

        List<AwardLine> awardLines = new ArrayList<>();
        List<String> unawardedItems = new ArrayList<>();

        Map<String, VendorAwardSummary> vendorSummaryMap = new HashMap<>();

        double quotedAwardValue = 0;
        double additionalFreight = 0;

        // STEP 1: Select winners and build award lines
        for (RfxItem item : items) {

            RfxComparisonResult comparison =
                    comparisonMap.get(item.getItemId());

            if (comparison == null) {
                unawardedItems.add(item.getItemId());
                continue;
            }

            QuoteComparisonResult winner = comparison.getQuotes()
                    .stream()
                    .filter(QuoteComparisonResult::isWinner)
                    .findFirst()
                    .orElse(null);

            if (winner == null) {
                unawardedItems.add(item.getItemId());
                continue;
            }

            double unitPrice = winner.getNormalizedPriceInr();
            int quantity = item.getRequiredQty();
            double lineTotal = unitPrice * quantity;

            AwardLine awardLine = new AwardLine();

            awardLine.setItemId(item.getItemId());
            awardLine.setVendorId(winner.getVendorId());
            awardLine.setVendorName(winner.getVendorName());
            awardLine.setUnitPriceInr(unitPrice);
            awardLine.setQuantity(quantity);
            awardLine.setTotalPriceInr(lineTotal);
            awardLine.setQuoteId(winner.getQuoteId());

            awardLines.add(awardLine);

            quotedAwardValue += lineTotal;

            VendorAwardSummary summary =
                    vendorSummaryMap.computeIfAbsent(
                            winner.getVendorId(),
                            id -> {
                                VendorAwardSummary s =
                                        new VendorAwardSummary();
                                s.setVendorId(winner.getVendorId());
                                s.setVendorName(winner.getVendorName());
                                s.setItemsAwarded(0);
                                s.setTotalAwardValueInr(0);
                                return s;
                            });

            summary.setItemsAwarded(
                    summary.getItemsAwarded() + 1);

            summary.setTotalAwardValueInr(
                    summary.getTotalAwardValueInr() + lineTotal
            );
        }

        // STEP 2: Calculate freight AFTER all award lines are selected
        Set<String> shipmentVendors = new HashSet<>();

        for (AwardLine line : awardLines) {

            QuoteComparisonResult winningQuote =
                    comparisons.stream()
                            .flatMap(comparison ->
                                    comparison.getQuotes().stream())
                            .filter(QuoteComparisonResult::isWinner)
                            .filter(quote ->
                                    quote.getQuoteId()
                                            .equals(line.getQuoteId()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Winning quote not found: "
                                                    + line.getQuoteId()));

            // Freight already included in the quoted price
            if (winningQuote.isFreightIncluded()) {
                continue;
            }

            FreightType freightType =
                    winningQuote.getFreightType();

            if (freightType == null
                    || freightType == FreightType.NONE) {
                continue;
            }

            double freightAmount =
                    winningQuote.getFreightAmount() == null
                            ? 0.0
                            : winningQuote.getFreightAmount();

            if (freightType == FreightType.PER_SHIPMENT) {

                // One shipment per vendor in our demo
                if (shipmentVendors.add(
                        winningQuote.getVendorId())) {

                    additionalFreight += freightAmount;
                }

            } else if (freightType == FreightType.PER_UNIT) {

                additionalFreight +=
                        freightAmount * line.getQuantity();
            }
        }

        // STEP 3: Populate recommendation
        recommendation.setTotalItems(items.size());
        recommendation.setAwardedItems(awardLines.size());
        recommendation.setUnawardedItems(unawardedItems.size());

        recommendation.setAwardLines(awardLines);
        recommendation.setUnawardedItemIds(unawardedItems);

        recommendation.setVendorSummaries(
                new ArrayList<>(vendorSummaryMap.values())
        );

        // STEP 4: Final financial totals
        double totalLandedCost =
                quotedAwardValue + additionalFreight;

        recommendation.setQuotedAwardValueInr(
                quotedAwardValue);

        recommendation.setAdditionalFreightInr(
                additionalFreight);

        recommendation.setTotalLandedCostInr(
                totalLandedCost);

        // Backward compatibility
        recommendation.setTotalAwardValueInr(
                totalLandedCost);

        return recommendation;
    }
}
