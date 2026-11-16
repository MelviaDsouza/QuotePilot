package com.quotepilot.repository;

import com.quotepilot.entity.AwardApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardApprovalRepository
        extends JpaRepository<AwardApproval, String> {
}