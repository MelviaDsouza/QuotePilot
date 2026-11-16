package com.quotepilot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "rfx_items")
@IdClass(RfxItemId.class)
public class RfxItem {

    @Id
    private String rfxId;

    @Id
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

    public RfxItem() {
    }
}