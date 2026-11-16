package com.quotepilot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RfxComparisonResult {

    private String itemId;
    private List<QuoteComparisonResult> quotes;

    public RfxComparisonResult(
            String itemId,
            List<QuoteComparisonResult> quotes) {
        this.itemId = itemId;
        this.quotes = quotes;
    }
}
