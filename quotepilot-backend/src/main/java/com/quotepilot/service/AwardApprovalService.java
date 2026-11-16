package com.quotepilot.service;

import com.quotepilot.dto.AwardReadiness;
import com.quotepilot.dto.AwardRecommendation;
import com.quotepilot.entity.AwardApproval;
import com.quotepilot.repository.AwardApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AwardApprovalService {

    private final AwardReadinessService awardReadinessService;
    private final AwardRecommendationService awardRecommendationService;
    private final AwardApprovalRepository awardApprovalRepository;

    public AwardApproval approveAward(String rfxId) {

        AwardReadiness readiness =
                awardReadinessService.evaluateReadiness(rfxId);

        if (!readiness.isReadyForApproval()) {
            throw new IllegalStateException(
                    "Award cannot be approved. Resolve readiness issues first: "
                            + readiness.getIssues()
            );
        }

        AwardRecommendation recommendation =
                awardRecommendationService.generateRecommendation(rfxId);

        AwardApproval approval =
                awardApprovalRepository.findById(rfxId)
                        .orElseGet(AwardApproval::new);

        approval.setRfxId(rfxId);
        approval.setStatus("APPROVED");
        approval.setApprovedAt(LocalDateTime.now());
        approval.setApprovedAwardValueInr(
                recommendation.getTotalLandedCostInr()
        );
        approval.setAwardedItems(
                recommendation.getAwardedItems()
        );

        return awardApprovalRepository.save(approval);
    }

    public AwardApproval getApproval(String rfxId) {
        return awardApprovalRepository.findById(rfxId)
                .orElse(null);
    }
}