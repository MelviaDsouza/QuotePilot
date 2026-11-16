
package com.quotepilot.dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentExtractionResult {

    private String documentName;

    private String vendorName;

    private String currency;
    private String freightTerms;
    private String validity;

    private List<ExtractedQuoteLine> lines;
    private List<String> warnings;

    private String extractionStatus;
}
