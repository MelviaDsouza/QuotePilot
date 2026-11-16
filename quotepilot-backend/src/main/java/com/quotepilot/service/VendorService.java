package com.quotepilot.service;

import com.quotepilot.entity.Vendor;
import com.quotepilot.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendor getVendorById(String vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found: " + vendorId));
    }

    public Vendor createVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Vendor updateVendor(String vendorId, Vendor updatedVendor) {
        Vendor existingVendor = getVendorById(vendorId);

        existingVendor.setVendorName(updatedVendor.getVendorName());
        existingVendor.setRejectionRatePercent(
                updatedVendor.getRejectionRatePercent());
        existingVendor.setIso9001Certified(
                updatedVendor.getIso9001Certified());
        existingVendor.setQualityScore(updatedVendor.getQualityScore());

        return vendorRepository.save(existingVendor);
    }

    public void deleteVendor(String vendorId) {
        vendorRepository.deleteById(vendorId);
    }
}
