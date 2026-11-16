
package com.quotepilot.controller;

import com.quotepilot.dto.AiRfxDraft;
import com.quotepilot.entity.Rfx;
import com.quotepilot.service.AiRfxService;
import com.quotepilot.service.RfxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rfx")
@RequiredArgsConstructor
public class RfxController {

    private final RfxService rfxService;
    private final AiRfxService aiRfxService;

    @GetMapping
    public List<Rfx> getAllRfxs() {
        return rfxService.getAllRfxs();
    }

    @GetMapping("/{rfxId}")
    public Rfx getRfx(@PathVariable String rfxId) {
        return rfxService.getRfx(rfxId);
    }

    @PostMapping
    public Rfx createRfx(@RequestBody Rfx rfx) {
        return rfxService.createRfx(rfx);
    }

    @PostMapping("/ai-draft")
    public AiRfxDraft generateAiDraft(
            @RequestBody Map<String, String> request) {

        String prompt = request.get("prompt");

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Procurement requirements cannot be empty"
            );
        }

        return aiRfxService.generateDraft(prompt);
    }
}
