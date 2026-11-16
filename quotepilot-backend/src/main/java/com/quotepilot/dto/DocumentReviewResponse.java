package com.quotepilot.dto;

import lombok.Data;

@Data
public class DocumentReviewResponse {

    private DocumentExtractionResult extraction;

    private VendorMatchInfo vendorMatch;

    @Data
    public static class VendorMatchInfo {

        private String vendorId;
        private String suggestedVendorName;
        private String status;
        private String message;
    }
}
