package com.quotepilot.controller;

import com.quotepilot.entity.AwardApproval;
import com.quotepilot.service.AwardApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/rfx/{rfxId}/award")
@RequiredArgsConstructor
public class AwardApprovalController {

    private final AwardApprovalService awardApprovalService;

    @PostMapping("/approve")
    public AwardApproval approve(@PathVariable String rfxId) {
        return awardApprovalService.approveAward(rfxId);
    }

    @GetMapping
    public ResponseEntity<AwardApproval> getApproval(
            @PathVariable String rfxId) {

        AwardApproval approval =
                awardApprovalService.getApproval(rfxId);

        if (approval == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(approval);
    }
}