package com.quotepilot.service;

import com.quotepilot.entity.Vendor;
import com.quotepilot.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VendorQualificationService {

    private final VendorRepository vendorRepository;

    public VendorQualificationService(
            VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<String> getIneligibilityReasons(Vendor vendor) {

        List<String> reasons = new ArrayList<>();

        if (!Boolean.TRUE.equals(vendor.getIso9001Certified())) {
            reasons.add("ISO 9001 certification missing");
        }

        if (vendor.getRejectionRatePercent() == null
                || vendor.getRejectionRatePercent() > 2.0) {
            reasons.add("Rejection rate exceeds 2%");
        }

        return reasons;
    }

    public boolean isEligible(Vendor vendor) {
        return getIneligibilityReasons(vendor).isEmpty();
    }

    public Vendor getVendor(String vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found: " + vendorId));
    }
}
