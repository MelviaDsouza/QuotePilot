
package com.quotepilot.service;

import com.quotepilot.entity.Rfx;
import com.quotepilot.repository.RfxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RfxService {

    private final RfxRepository rfxRepository;

    public List<Rfx> getAllRfxs() {
        return rfxRepository.findAll();
    }

    public Rfx getRfx(String rfxId) {
        return rfxRepository.findById(rfxId)
                .orElseThrow(() ->
                        new RuntimeException("RFx not found: " + rfxId));
    }

    public Rfx createRfx(Rfx rfx) {
        rfx.setRfxId(generateRfxId());
        rfx.setStatus("DRAFT");
        rfx.setCreatedAt(LocalDateTime.now());

        return rfxRepository.save(rfx);
    }

    private String generateRfxId() {
        String id;

        do {
            id = "RFX-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        } while (rfxRepository.existsById(id));

        return id;
    }
}
