package com.quotepilot.controller;

import com.quotepilot.dto.AwardReadiness;
import com.quotepilot.service.AwardReadinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfx/{rfxId}/award")
@RequiredArgsConstructor
public class AwardReadinessController {

    private final AwardReadinessService awardReadinessService;

    @GetMapping("/readiness")
    public AwardReadiness getReadiness(@PathVariable String rfxId) {
        return awardReadinessService.evaluateReadiness(rfxId);
    }
}