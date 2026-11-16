package com.quotepilot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwardLine {

    private String itemId;
    private String vendorId;
    private String vendorName;

    private double unitPriceInr;
    private int quantity;
    private double totalPriceInr;

    private String quoteId;
}