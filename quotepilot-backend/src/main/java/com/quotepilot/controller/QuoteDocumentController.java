
package com.quotepilot.controller;

import com.quotepilot.dto.DocumentExtractionResult;
import com.quotepilot.service.DocumentTextExtractionService;
import com.quotepilot.service.GroqQuoteExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rfx/{rfxId}/documents")
@RequiredArgsConstructor
public class QuoteDocumentController {

    private final DocumentTextExtractionService textExtractionService;
    private final GroqQuoteExtractionService groqExtractionService;

    @PostMapping(
            value = "/extract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentExtractionResult extractDocument(
            @PathVariable Long rfxId,
            @RequestParam("file") MultipartFile file)
            throws Exception {

        String rawText =
                textExtractionService.extractText(file);

        return groqExtractionService.extractQuote(
                file.getOriginalFilename(),
                rawText
        );
    }
}
