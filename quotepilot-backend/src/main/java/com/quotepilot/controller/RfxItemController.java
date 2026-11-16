
package com.quotepilot.controller;

import com.quotepilot.entity.RfxItem;
import com.quotepilot.service.RfxItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rfx/{rfxId}/items")
@RequiredArgsConstructor
public class RfxItemController {

    private final RfxItemService rfxItemService;

    @GetMapping
    public List<RfxItem> getAllItems(
            @PathVariable String rfxId) {
        return rfxItemService.getAllItems(rfxId);
    }

    @GetMapping("/{itemId}")
    public RfxItem getItemById(
            @PathVariable String rfxId,
            @PathVariable String itemId) {
        return rfxItemService.getItemById(rfxId, itemId);
    }

    @PostMapping
    public RfxItem createItem(
            @PathVariable String rfxId,
            @RequestBody RfxItem item) {
        return rfxItemService.createItem(rfxId, item);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(
            @PathVariable String rfxId,
            @PathVariable String itemId) {
        rfxItemService.deleteItem(rfxId, itemId);
    }

    @PostMapping("/import")
    public List<RfxItem> importExcel(
            @PathVariable String rfxId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return rfxItemService.importExcel(rfxId, file);
    }
}
