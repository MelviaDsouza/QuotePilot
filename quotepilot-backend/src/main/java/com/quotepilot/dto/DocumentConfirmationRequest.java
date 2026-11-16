
package com.quotepilot.dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentConfirmationRequest {

    private String vendorId;
    private String documentName;
    private List<ConfirmedQuoteLine> lines;

    @Data
    public static class ConfirmedQuoteLine {

        private String itemId;
        private String description;

        private Double quotedPrice;
        private String currency;
        private String quotedUnit;
        private Double quantity;

        private String freightTerms;
        private String evidence;

        private Double freightAmount;
    }
}
