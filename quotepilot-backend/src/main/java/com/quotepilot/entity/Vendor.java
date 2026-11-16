
package com.quotepilot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vendors")
@Getter
@Setter
public class Vendor {

    @Id
    private String vendorId;

    private String vendorName;
    private Double rejectionRatePercent;
    private Boolean iso9001Certified;
    private Double qualityScore;
}
