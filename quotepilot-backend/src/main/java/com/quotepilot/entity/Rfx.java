
package com.quotepilot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "rfx")
public class Rfx {

    @Id
    private String rfxId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;

    private LocalDate submissionDeadline;

    private LocalDateTime createdAt;

    private String createdBy;

    public Rfx() {
    }
}
