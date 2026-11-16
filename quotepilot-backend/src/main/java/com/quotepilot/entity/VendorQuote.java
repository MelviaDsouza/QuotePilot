package com.quotepilot.entity;

import com.quotepilot.model.FreightType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "vendor_quotes")
public class VendorQuote {

    @Id
    private String quoteId;

    private String vendorId;
    private String vendorName;
    private String itemId;

    private Double quotedPrice;
    private String quotedUnit;
    private Integer quotedQuantity;

    private Boolean freightIncluded;
    private String currency;
    private Double freightAmount;
    private FreightType freightType;
    private String rfxId;

    public VendorQuote() {}
}
