package com.quotepilot.service;

import com.quotepilot.dto.AwardReadiness;
import com.quotepilot.dto.AwardRecommendation;
import com.quotepilot.entity.Vendor;
import com.quotepilot.repository.VendorRepository;
import com.quotepilot.repository.RfxItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AwardReadinessService {

    private final AwardRecommendationService awardRecommendationService;
    private final VendorRepository vendorRepository;
    private final RfxItemRepository rfxItemRepository;

    public AwardReadiness evaluateReadiness(String rfxId) {

        AwardRecommendation recommendation =
                awardRecommendationService.generateRecommendation(rfxId);

        AwardReadiness readiness = new AwardReadiness();

        List<String> issues = new ArrayList<>();
        List<String> passedChecks = new ArrayList<>();

        // Check 1: RFx contains items and all items have awards
        int actualItemCount =
                rfxItemRepository.findByRfxId(rfxId).size();

        if (actualItemCount == 0) {
            issues.add("RFx contains no items.");
        } else if (recommendation.getUnawardedItems() == 0) {
            passedChecks.add(
                    "All RFx items have an eligible supplier."
            );
        } else {
            issues.add(
                    recommendation.getUnawardedItems()
                            + " items have no eligible supplier."
            );
        }

        // Check 2: Every awarded vendor exists and is qualified
        List<String> awardedVendorIds =
                recommendation.getVendorSummaries()
                        .stream()
                        .map(summary -> summary.getVendorId())
                        .distinct()
                        .toList();

        List<Vendor> awardedVendors =
                vendorRepository.findAllByVendorIdIn(awardedVendorIds);

        boolean allVendorsExist =
                awardedVendors.size() == awardedVendorIds.size();

        boolean allAwardedVendorsQualified =
                allVendorsExist &&
                        !awardedVendors.isEmpty() &&
                        awardedVendors.stream().allMatch(vendor ->
                                Boolean.TRUE.equals(
                                        vendor.getIso9001Certified()
                                ) &&
                                        vendor.getRejectionRatePercent() != null &&
                                        vendor.getRejectionRatePercent() <= 2.0
                        );

        if (allAwardedVendorsQualified) {
            passedChecks.add(
                    "All awarded suppliers meet qualification rules."
            );
        } else {
            issues.add(
                    "One or more awarded suppliers are missing "
                            + "or fail qualification."
            );
        }

        // Check 3: Award covers all requested items
        if (actualItemCount > 0 &&
                recommendation.getAwardedItems() == actualItemCount) {
            passedChecks.add(
                    "Award covers all requested items."
            );
        } else {
            issues.add(
                    "Award does not cover all requested items."
            );
        }

        readiness.setTotalChecks(3);
        readiness.setPassedChecks(passedChecks.size());
        readiness.setUnresolvedIssues(issues.size());
        readiness.setIssues(issues);
        readiness.setPassedChecksList(passedChecks);
        readiness.setReadyForApproval(issues.isEmpty());

        return readiness;
    }
}