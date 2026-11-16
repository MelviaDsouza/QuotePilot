package com.quotepilot.controller;

import com.quotepilot.dto.DocumentReviewResponse;
import com.quotepilot.service.DocumentReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rfx/{rfxId}/documents")
@RequiredArgsConstructor
public class DocumentReviewController {

    private final DocumentReviewService documentReviewService;

    @PostMapping(
            value = "/review",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentReviewResponse reviewDocument(
            @PathVariable String rfxId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        return documentReviewService.reviewDocument(file);
    }
}
