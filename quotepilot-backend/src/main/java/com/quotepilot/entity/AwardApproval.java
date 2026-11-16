package com.quotepilot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "award_approvals")
public class AwardApproval {

    @Id
    private String rfxId;

    private String status;

    private LocalDateTime approvedAt;

    private Double approvedAwardValueInr;

    private Integer awardedItems;

    public AwardApproval() {
    }
}