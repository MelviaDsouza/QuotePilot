
package com.quotepilot.controller;

import com.quotepilot.dto.QuoteComparisonResult;
import com.quotepilot.dto.RfxComparisonResult;
import com.quotepilot.service.QuoteComparisonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfx/{rfxId}/comparison")
public class QuoteComparisonController {

    private final QuoteComparisonService comparisonService;

    public QuoteComparisonController(
            QuoteComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    // Compare quotes for a single item
    @GetMapping("/item/{itemId}")
    public List<QuoteComparisonResult> compareItem( @PathVariable String rfxId,
            @PathVariable String itemId) {

        return comparisonService.compareItem(rfxId,itemId);
    }

    // Compare quotes for multiple items in one request
    @PostMapping("/all")
    public List<RfxComparisonResult> compareAllItems( @PathVariable String rfxId,
            @RequestBody List<String> itemIds) {

        return comparisonService.compareAllItems(rfxId,itemIds);
    }
}
