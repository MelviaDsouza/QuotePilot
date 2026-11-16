
package com.quotepilot.controller;

import com.quotepilot.dto.DocumentConfirmationRequest;
import com.quotepilot.entity.VendorQuote;
import com.quotepilot.service.DocumentConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfx/{rfxId}/documents")
@RequiredArgsConstructor
public class DocumentConfirmationController {

    private final DocumentConfirmationService documentConfirmationService;

    @PostMapping("/confirm")
    public List<VendorQuote> confirmDocument(
            @PathVariable String rfxId,
            @RequestBody DocumentConfirmationRequest request) {

        return documentConfirmationService.confirmDocument(
                rfxId, request);
    }
}
