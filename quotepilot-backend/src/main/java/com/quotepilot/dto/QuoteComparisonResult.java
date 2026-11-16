package com.quotepilot.dto;

import com.quotepilot.model.FreightType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteComparisonResult {

    private String quoteId;
    private String vendorId;
    private String vendorName;
    private String itemId;

    private Double quotedPrice;
    private String currency;
    private String quotedUnit;
    private Double normalizedPriceInr;

    private boolean eligible;
    private String exclusionReason;
    private boolean winner;
    private boolean freightIncluded;
    private Double freightAmount;
    private FreightType freightType;

    public QuoteComparisonResult() {}

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public Double getQuotedPrice() { return quotedPrice; }
    public void setQuotedPrice(Double quotedPrice) { this.quotedPrice = quotedPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getQuotedUnit() { return quotedUnit; }
    public void setQuotedUnit(String quotedUnit) { this.quotedUnit = quotedUnit; }

    public Double getNormalizedPriceInr() { return normalizedPriceInr; }
    public void setNormalizedPriceInr(Double normalizedPriceInr) {
        this.normalizedPriceInr = normalizedPriceInr;
    }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public String getExclusionReason() { return exclusionReason; }
    public void setExclusionReason(String exclusionReason) {
        this.exclusionReason = exclusionReason;
    }

    public boolean isWinner() { return winner; }
    public void setWinner(boolean winner) { this.winner = winner; }
}
