
package com.quotepilot.controller;

import com.quotepilot.entity.VendorQuote;
import com.quotepilot.service.QuoteBulkImportService;
import com.quotepilot.service.VendorQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rfx/{rfxId}/quotes")
@RequiredArgsConstructor
public class VendorQuoteController {

    private final VendorQuoteService vendorQuoteService;
    private final QuoteBulkImportService quoteBulkImportService;

    @GetMapping
    public List<VendorQuote> getAllQuotes(
            @PathVariable String rfxId) {
        return vendorQuoteService.getQuotesByRfxId(rfxId);
    }

    @GetMapping("/item/{itemId}")
    public List<VendorQuote> getQuotesByItem(
            @PathVariable String rfxId,
            @PathVariable String itemId) {
        return vendorQuoteService.getQuotesByRfxIdAndItemId(
                rfxId, itemId);
    }

    @PostMapping
    public VendorQuote createQuote(
            @PathVariable String rfxId,
            @RequestBody VendorQuote quote) {
        return vendorQuoteService.createQuote(rfxId, quote);
    }
    @PostMapping("/import")
    public Map<String, Object> importQuotes(
            @PathVariable String rfxId,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        int imported = quoteBulkImportService.importQuotes(rfxId, file);

        return Map.of(
                "message", "Quotes imported successfully",
                "importedCount", imported
        );
    }
}
