
package com.quotepilot.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiRfxDraft {

    private String title;
    private String description;
    private String scopeOfWork;
    private String submissionDeadline;

    private List<ItemDraft> items;
    private List<String> questionnaire;
    private List<String> termsAndConditions;

    @Data
    public static class ItemDraft {
        private String itemId;
        private String description;
        private Integer lengthMm;
        private Integer widthMm;
        private Integer heightMm;
        private Integer ply;
        private Integer boardGsm;
        private Double minBurstingStrength;
        private Integer requiredQty;
        private String unit;
    }
}
