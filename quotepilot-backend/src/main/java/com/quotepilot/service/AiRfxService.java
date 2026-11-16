
package com.quotepilot.service;

import com.quotepilot.dto.AiRfxDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AiRfxService {

    private final GroqService groqService;
    private final ObjectMapper objectMapper;

    public AiRfxDraft generateDraft(String buyerPrompt) {

        String prompt = """
                Create a procurement RFx draft from the buyer's requirements.

                Buyer requirements:
                %s

                Return a JSON object with these fields:
                - title
                - description
                - scopeOfWork
                - submissionDeadline
                - items
                - questionnaire
                - termsAndConditions

                Each item must contain:
                itemId, description, lengthMm, widthMm, heightMm,
                ply, boardGsm, minBurstingStrength, requiredQty, unit.

                Rules:
                1. Generate unique item IDs such as ITEM-001.
                2. Do not invent technical specifications or quantities.
                3. Use null for unknown numeric specifications.
                4. Use an empty list if questionnaire or terms are unspecified.
                5. Use ISO date format YYYY-MM-DD for submissionDeadline.
                6. Return valid JSON only.
                7. submissionDeadline must be a future date.
                8. If no submission deadline is specified, use null.
                9. Never generate a submission deadline in the past.
                """.formatted(buyerPrompt);

        try {
            String json = groqService.generateJson(prompt);

            return objectMapper.readValue(json, AiRfxDraft.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate RFx draft using AI", e
            );
        }
    }
}
