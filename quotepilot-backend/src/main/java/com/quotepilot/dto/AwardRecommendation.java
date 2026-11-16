package com.quotepilot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AwardRecommendation {

    private double totalAwardValueInr;
    private int totalItems;
    private int awardedItems;
    private int unawardedItems;

    private List<AwardLine> awardLines;
    private List<String> unawardedItemIds;
    private List<VendorAwardSummary> vendorSummaries;

    private double quotedAwardValueInr;
    private double additionalFreightInr;
    private double totalLandedCostInr;
}