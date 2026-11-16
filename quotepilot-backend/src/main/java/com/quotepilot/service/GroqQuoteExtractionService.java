
package com.quotepilot.service;

import com.quotepilot.dto.DocumentExtractionResult;
import com.quotepilot.dto.ExtractedQuoteLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroqQuoteExtractionService {

    private final GroqService groqService;
    private final ObjectMapper objectMapper;
    private final QuoteValidationService quoteValidationService;
    public DocumentExtractionResult extractQuote(
            String documentName,
            String rawText) throws Exception {

        String prompt = """
                You are a procurement quote extraction engine.

                Extract supplier quotation information from the document.

                RULES:
                1. Extract only information explicitly present.
                2. Never invent prices, quantities, item IDs or suppliers.
                3. Preserve the original currency.
                4. Preserve the original price basis.
                5. Do not convert per-100 prices into per-box prices.
                6. Do not convert lot prices into per-unit prices.
                7. Represent missing values as null.
                8. Confidence must be between 0 and 1.
                9. Evidence must contain an exact source text snippet.
                10. If a value is ambiguous, lower confidence and add a warning.
                11. Freight must distinguish included from additional freight.
                12. Return valid JSON only. No markdown fences.

                OUTPUT JSON STRUCTURE:
                {
                  "vendorName": "string or null",
                  "currency": "string or null",
                  "freightTerms": "string or null",
                  "validity": "string or null",
                  "lines": [
                    {
                      "itemId": "string or null",
                      "description": "string or null",
                      "quotedPrice": 0,
                      "currency": "string or null",
                      "quotedUnit": "string or null",
                      "quantity": 0,
                      "freightTerms": "string or null",
                      "confidence": 0.0,
                      "evidence": "exact source snippet"
                    }
                  ],
                  "warnings": []
                }

                DOCUMENT NAME:
                %s

                DOCUMENT TEXT:
                %s
                """.formatted(documentName, rawText);

        String response = groqService.generateJson(prompt);

        DocumentExtractionResult result =
                parseResponse(documentName, response);

        validateExtractedLines(result, rawText);

        return result;
    }
    private void validateExtractedLines(
            DocumentExtractionResult result,
            String rawText) {

        List<String> warnings = result.getWarnings() == null
                ? new ArrayList<>()
                : new ArrayList<>(result.getWarnings());

        if (result.getLines() != null) {
            for (ExtractedQuoteLine line : result.getLines()) {

                QuoteValidationService.ValidationResult validation =
                        quoteValidationService.validate(line, rawText);

                line.setEvidenceVerified(
                        validation.evidenceVerified()
                );

                line.setEvidenceStatus(
                        validation.evidenceVerified()
                                ? "VERIFIED"
                                : "UNVERIFIED"
                );

                line.setValidationErrors(
                        validation.errors()
                );

                line.setValidationWarnings(
                        validation.warnings()
                );

                line.setValidationStatus(
                        validation.errors().isEmpty()
                                ? "VALID"
                                : "REVIEW_REQUIRED"
                );

                warnings.addAll(validation.errors());
            }
        }

        result.setWarnings(warnings);
    }

    private DocumentExtractionResult parseResponse(
            String documentName,
            String response) throws Exception {

        JsonNode root = objectMapper.readTree(cleanJson(response));

        DocumentExtractionResult result =
                new DocumentExtractionResult();

        result.setDocumentName(documentName);
        result.setVendorName(textOrNull(root, "vendorName"));
        result.setCurrency(textOrNull(root, "currency"));
        result.setFreightTerms(textOrNull(root, "freightTerms"));
        result.setValidity(textOrNull(root, "validity"));

        List<ExtractedQuoteLine> lines = new ArrayList<>();

        JsonNode lineNodes = root.path("lines");

        if (lineNodes.isArray()) {
            for (JsonNode node : lineNodes) {

                ExtractedQuoteLine line =
                        new ExtractedQuoteLine();

                line.setItemId(textOrNull(node, "itemId"));
                line.setDescription(textOrNull(node, "description"));
                line.setQuotedPrice(numberOrNull(node, "quotedPrice"));
                line.setCurrency(textOrNull(node, "currency"));
                line.setQuotedUnit(textOrNull(node, "quotedUnit"));
                line.setQuantity(integerOrNull(node, "quantity"));
                line.setFreightTerms(textOrNull(node, "freightTerms"));
                line.setConfidence(numberOrNull(node, "confidence"));
                line.setEvidence(textOrNull(node, "evidence"));

                lines.add(line);
            }
        }

        result.setLines(lines);

        List<String> warnings = new ArrayList<>();
        JsonNode warningNodes = root.path("warnings");

        if (warningNodes.isArray()) {
            warningNodes.forEach(w -> warnings.add(w.asString()));
        }

        result.setWarnings(warnings);
        result.setExtractionStatus("EXTRACTED");

        return result;
    }

    private String cleanJson(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "Groq returned an empty extraction response"
            );
        }

        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }

        return cleaned.trim();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asString();
    }

    private Double numberOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()
                || !value.isNumber()) {
            return null;
        }

        return value.doubleValue();
    }

    private Integer integerOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()
                || !value.isNumber()) {
            return null;
        }

        return value.intValue();
    }
}
