
package com.quotepilot.service;

import com.quotepilot.entity.RfxItem;
import com.quotepilot.repository.RfxItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RfxItemService {

    private final RfxItemRepository rfxItemRepository;

    public List<RfxItem> getAllItems(String rfxId) {
        return rfxItemRepository.findByRfxId(rfxId);
    }

    public RfxItem getItemById(String rfxId, String itemId) {
        return rfxItemRepository
                .findByRfxIdAndItemId(rfxId, itemId)
                .orElseThrow(() -> new RuntimeException(
                        "Item not found in RFx: " + rfxId
                                + ", item: " + itemId
                ));
    }

    // Create only if this RFx does not already contain the item
    public RfxItem createItem(String rfxId, RfxItem item) {

        if (rfxItemRepository.existsByRfxIdAndItemId(
                rfxId, item.getItemId())) {
            throw new RuntimeException(
                    "Item already exists in this RFx: "
                            + item.getItemId()
            );
        }

        item.setRfxId(rfxId);
        return rfxItemRepository.save(item);
    }

    public void deleteItem(String rfxId, String itemId) {
        rfxItemRepository.delete(getItemById(rfxId, itemId));
    }

    @Transactional
    public List<RfxItem> importExcel(
            String rfxId,
            MultipartFile file
    ) throws IOException {

        Map<String, RfxItem> importedItems = new LinkedHashMap<>();

        try (Workbook workbook =
                     WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("RFx_Items");

            if (sheet == null) {
                throw new RuntimeException("RFx_Items sheet not found");
            }

            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String itemId = formatter
                        .formatCellValue(row.getCell(0))
                        .trim();

                if (itemId.isEmpty()) {
                    continue;
                }

                // Reject duplicate IDs within the uploaded workbook
                if (importedItems.containsKey(itemId)) {
                    throw new RuntimeException(
                            "Duplicate item ID in Excel: " + itemId
                    );
                }

                RfxItem item = new RfxItem();

                item.setRfxId(rfxId);
                item.setItemId(itemId);
                item.setDescription(
                        formatter.formatCellValue(row.getCell(1))
                );

                item.setLengthMm((int) row.getCell(2)
                        .getNumericCellValue());

                item.setWidthMm((int) row.getCell(3)
                        .getNumericCellValue());

                item.setHeightMm((int) row.getCell(4)
                        .getNumericCellValue());

                item.setPly((int) row.getCell(5)
                        .getNumericCellValue());

                item.setBoardGsm((int) row.getCell(6)
                        .getNumericCellValue());

                item.setMinBurstingStrength(
                        row.getCell(7).getNumericCellValue()
                );

                item.setRequiredQty((int) row.getCell(8)
                        .getNumericCellValue());

                item.setUnit(
                        formatter.formatCellValue(row.getCell(9))
                );

                importedItems.put(itemId, item);
            }
        }

        // Upsert: update existing items or insert new ones
        List<RfxItem> itemsToSave = new ArrayList<>();

        for (RfxItem imported : importedItems.values()) {

            RfxItem existing = rfxItemRepository
                    .findByRfxIdAndItemId(
                            rfxId,
                            imported.getItemId()
                    )
                    .orElse(null);

            if (existing != null) {
                existing.setDescription(imported.getDescription());
                existing.setLengthMm(imported.getLengthMm());
                existing.setWidthMm(imported.getWidthMm());
                existing.setHeightMm(imported.getHeightMm());
                existing.setPly(imported.getPly());
                existing.setBoardGsm(imported.getBoardGsm());
                existing.setMinBurstingStrength(
                        imported.getMinBurstingStrength()
                );
                existing.setRequiredQty(imported.getRequiredQty());
                existing.setUnit(imported.getUnit());

                itemsToSave.add(existing);
            } else {
                itemsToSave.add(imported);
            }
        }

        return rfxItemRepository.saveAll(itemsToSave);
    }
}
