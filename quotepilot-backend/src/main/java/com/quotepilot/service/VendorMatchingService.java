package com.quotepilot.service;

import com.quotepilot.entity.Vendor;
import com.quotepilot.repository.VendorRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorMatchingService {

    private final VendorRepository vendorRepository;

    public MatchResult matchVendor(String extractedVendorName) {

        if (extractedVendorName == null
                || extractedVendorName.isBlank()) {
            return new MatchResult(
                    null, null, "UNMATCHED",
                    "Vendor name was not extracted"
            );
        }

        String normalized = normalize(extractedVendorName);

        if (normalized.isBlank()) {
            return new MatchResult(
                    null, null, "UNMATCHED",
                    "Vendor name could not be normalized"
            );
        }

        List<Vendor> vendors = vendorRepository.findAll();

        // 1. Exact normalized match
        List<Vendor> exactMatches = vendors.stream()
                .filter(v -> normalize(v.getVendorName())
                        .equals(normalized))
                .toList();

        if (exactMatches.size() == 1) {
            Vendor vendor = exactMatches.get(0);

            return new MatchResult(
                    vendor.getVendorId(),
                    vendor.getVendorName(),
                    "MATCHED",
                    "Exact normalized vendor name match"
            );
        }

        if (exactMatches.size() > 1) {
            return new MatchResult(
                    null, null, "AMBIGUOUS",
                    "Multiple vendors have the same normalized name"
            );
        }

        // 2. Partial matches: suggestions only, never auto-match
        List<Vendor> possibleMatches = vendors.stream()
                .filter(v -> {
                    String existing = normalize(v.getVendorName());

                    return !existing.isBlank()
                            && (existing.contains(normalized)
                            || normalized.contains(existing));
                })
                .toList();

        if (possibleMatches.size() == 1) {
            Vendor vendor = possibleMatches.get(0);

            return new MatchResult(
                    null,
                    vendor.getVendorName(),
                    "AMBIGUOUS",
                    "Possible vendor match: " + vendor.getVendorName()
            );
        }

        if (possibleMatches.size() > 1) {
            return new MatchResult(
                    null, null, "AMBIGUOUS",
                    "Multiple possible vendor matches"
            );
        }

        return new MatchResult(
                null, null, "UNMATCHED",
                "No registered vendor matched the extracted name"
        );
    }

    private String normalize(String name) {

        if (name == null) {
            return "";
        }

        String normalized = name.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Remove common legal suffixes
        normalized = normalized.replaceAll(
                "\\b(private limited|pvt limited|pvt ltd|"
                        + "private ltd|limited|ltd|incorporated|"
                        + "corporation|corp|inc)\\b",
                ""
        );

        return normalized.replaceAll("[^a-z0-9]", "");
    }

    @Getter
    public static class MatchResult {

        private final String vendorId;
        private final String suggestedVendorName;
        private final String status;
        private final String message;

        public MatchResult(
                String vendorId,
                String suggestedVendorName,
                String status,
                String message) {

            this.vendorId = vendorId;
            this.suggestedVendorName = suggestedVendorName;
            this.status = status;
            this.message = message;
        }
    }
}
