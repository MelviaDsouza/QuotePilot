
package com.quotepilot.controller;

import com.quotepilot.dto.AwardRecommendation;
import com.quotepilot.service.AwardRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfx/{rfxId}/award")
@RequiredArgsConstructor
public class AwardRecommendationController {

    private final AwardRecommendationService awardRecommendationService;

    @GetMapping("/recommendation")
    public AwardRecommendation getRecommendation(
            @PathVariable String rfxId) {

        return awardRecommendationService.generateRecommendation(rfxId);
    }
}
