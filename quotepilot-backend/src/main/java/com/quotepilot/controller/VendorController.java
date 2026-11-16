
package com.quotepilot.controller;

import com.quotepilot.entity.Vendor;
import com.quotepilot.service.QuoteBulkImportService;
import com.quotepilot.service.VendorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService, QuoteBulkImportService quoteBulkImportService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public List<Vendor> getAllVendors() {
        return vendorService.getAllVendors();
    }

    @GetMapping("/{vendorId}")
    public Vendor getVendorById(
            @PathVariable String vendorId) {
        return vendorService.getVendorById(vendorId);
    }

    @PostMapping
    public Vendor createVendor(
            @RequestBody Vendor vendor) {
        return vendorService.createVendor(vendor);
    }

    @PutMapping("/{vendorId}")
    public Vendor updateVendor(
            @PathVariable String vendorId,
            @RequestBody Vendor vendor) {
        return vendorService.updateVendor(vendorId, vendor);
    }

    @DeleteMapping("/{vendorId}")
    public void deleteVendor(
            @PathVariable String vendorId) {
        vendorService.deleteVendor(vendorId);
    }
}
