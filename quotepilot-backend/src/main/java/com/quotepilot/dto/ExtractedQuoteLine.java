
package com.quotepilot.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExtractedQuoteLine {

    private String itemId;
    private String description;

    private Double quotedPrice;
    private String currency;
    private String quotedUnit;
    private Integer quantity;
    private String freightTerms;

    // LLM self-assessment; not calibrated probability
    private Double confidence;

    // Source text supporting extraction
    private String evidence;

    // Deterministic evidence verification
    private Boolean evidenceVerified;
    private String evidenceStatus;

    // Deterministic validation
    private String validationStatus;
    private List<String> validationErrors = new ArrayList<>();
    private List<String> validationWarnings = new ArrayList<>();
}
