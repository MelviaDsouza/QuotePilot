package com.quotepilot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorAwardSummary {

    private String vendorId;
    private String vendorName;

    private int itemsAwarded;
    private double totalAwardValueInr;
}