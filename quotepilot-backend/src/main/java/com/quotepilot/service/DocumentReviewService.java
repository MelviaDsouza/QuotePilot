package com.quotepilot.service;

import com.quotepilot.dto.DocumentExtractionResult;
import com.quotepilot.dto.DocumentReviewResponse;
import com.quotepilot.service.VendorMatchingService.MatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentReviewService {

    private final DocumentTextExtractionService documentTextExtractionService;
    private final GroqQuoteExtractionService groqQuoteExtractionService;
    private final VendorMatchingService vendorMatchingService;

    public DocumentReviewResponse reviewDocument(
            MultipartFile file) throws Exception {

        String documentName = file.getOriginalFilename();

        // 1. Extract text using PDFBox / POI / OCR
        String rawText = documentTextExtractionService.extractText(file);
        log.info("Text extraction completed. Characters: {}", rawText.length());

        log.info("Starting Groq extraction");

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalStateException(
                    "No text could be extracted from the document"
            );
        }

        // 2. Extract quote information using Groq
        DocumentExtractionResult extraction =
                groqQuoteExtractionService.extractQuote(
                        documentName,
                        rawText
                );

        log.info("Groq extraction completed");

        log.info("Starting vendor matching");
        // 3. Match extracted vendor name against registered vendors
        MatchResult matchResult =
                vendorMatchingService.matchVendor(
                        extraction.getVendorName()
                );
        log.info("Vendor matching completed");
        // 4. Build response
        DocumentReviewResponse response =
                new DocumentReviewResponse();

        response.setExtraction(extraction);

        DocumentReviewResponse.VendorMatchInfo vendorMatch =
                new DocumentReviewResponse.VendorMatchInfo();

        vendorMatch.setVendorId(matchResult.getVendorId());
        vendorMatch.setSuggestedVendorName(
                matchResult.getSuggestedVendorName()
        );
        vendorMatch.setStatus(matchResult.getStatus());
        vendorMatch.setMessage(matchResult.getMessage());

        response.setVendorMatch(vendorMatch);

        return response;
    }
}
