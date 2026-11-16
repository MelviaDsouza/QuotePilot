
package com.quotepilot.service;

import com.quotepilot.dto.ExtractedQuoteLine;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class QuoteValidationService {

    public ValidationResult validate(
            ExtractedQuoteLine line,
            String rawText) {

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        boolean evidenceVerified =
                verifyEvidence(line.getEvidence(), rawText);

        if (line.getEvidence() == null
                || line.getEvidence().isBlank()) {

            errors.add("Missing source evidence");

        } else if (!evidenceVerified) {

            errors.add(
                    "Extracted evidence could not be found in source document"
            );
        }

        if (line.getQuotedPrice() == null) {
            warnings.add("Missing quoted price");
        } else if (line.getQuotedPrice() <= 0) {
            errors.add("Quoted price must be greater than zero");
        }

        if (line.getCurrency() == null
                || line.getCurrency().isBlank()) {
            warnings.add("Missing currency");
        }

        if (line.getQuotedUnit() == null
                || line.getQuotedUnit().isBlank()) {
            warnings.add("Missing quoted unit or price basis");
        }

        if (line.getQuantity() == null) {
            warnings.add("Missing quoted quantity");
        } else if (line.getQuantity() <= 0) {
            errors.add("Quoted quantity must be greater than zero");
        }

        if (line.getItemId() == null
                && (line.getDescription() == null
                || line.getDescription().isBlank())) {
            errors.add(
                    "Quote line has neither item ID nor description"
            );
        }

        return new ValidationResult(
                evidenceVerified,
                errors,
                warnings
        );
    }

    private boolean verifyEvidence(
            String evidence,
            String rawText) {

        if (evidence == null || evidence.isBlank()
                || rawText == null || rawText.isBlank()) {
            return false;
        }

        String normalizedEvidence = normalize(evidence);
        String normalizedSource = normalize(rawText);

        return normalizedSource.contains(normalizedEvidence);
    }

    private String normalize(String text) {

        return Normalizer.normalize(
                        text,
                        Normalizer.Form.NFKC
                )
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record ValidationResult(
            boolean evidenceVerified,
            List<String> errors,
            List<String> warnings) {
    }
}
