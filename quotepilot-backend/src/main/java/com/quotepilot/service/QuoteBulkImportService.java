
package com.quotepilot.service;

import com.quotepilot.entity.VendorQuote;
import com.quotepilot.model.FreightType;
import com.quotepilot.repository.VendorQuoteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuoteBulkImportService {

    private final VendorQuoteRepository quoteRepository;

    private static final Map<String, String> VENDOR_ID_MAP = Map.of(
            "V-A", "V-PACKRIGHT",
            "V-B", "V-BOXCRAFT",
            "V-C", "V-CORRUMAX",
            "V-D", "V-ECOSHIP",
            "V-E", "V-PRIMECORR"
    );

    private static final Map<String, String> VENDOR_NAME_MAP = Map.of(
            "V-A", "PackRight Industries",
            "V-B", "BoxCraft Solutions",
            "V-C", "CorruMax Packaging",
            "V-D", "EcoShip Cartons",
            "V-E", "PrimeCorr Supplies"
    );

    private static final Map<String, Double> FREIGHT_AMOUNT_MAP = Map.of(
            "V-A", 0.0,
            "V-B", 0.0,
            "V-C", 18000.0,
            "V-D", 0.0,
            "V-E", 0.0
    );

    private static final Map<String, FreightType> FREIGHT_TYPE_MAP = Map.of(
            "V-A", FreightType.NONE,
            "V-B", FreightType.NONE,
            "V-C", FreightType.PER_SHIPMENT,
            "V-D", FreightType.NONE,
            "V-E", FreightType.NONE
    );

    public int importQuotes(String rfxId, MultipartFile file)
            throws IOException {

        List<VendorQuote> quotes = new ArrayList<>();

        try (Workbook workbook =
                     new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("Quote_GroundTruth");

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Quote_GroundTruth sheet not found");
            }

            DataFormatter formatter = new DataFormatter();

            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                String vendorCode =
                        formatter.formatCellValue(row.getCell(0)).trim();

                String itemId =
                        formatter.formatCellValue(row.getCell(1)).trim();

                String priceText =
                        formatter.formatCellValue(row.getCell(2)).trim();

                String quotedUnit =
                        formatter.formatCellValue(row.getCell(3)).trim();

                String currency =
                        formatter.formatCellValue(row.getCell(4)).trim();

                String freight =
                        formatter.formatCellValue(row.getCell(5)).trim();

                String quoteStatus =
                        formatter.formatCellValue(row.getCell(6)).trim();

                if (vendorCode.isEmpty()
                        || itemId.isEmpty()
                        || priceText.isEmpty()
                        || !"Quoted".equalsIgnoreCase(quoteStatus)) {
                    continue;
                }

                String vendorId = VENDOR_ID_MAP.get(vendorCode);
                String vendorName = VENDOR_NAME_MAP.get(vendorCode);

                if (vendorId == null) {
                    throw new IllegalArgumentException(
                            "Unknown vendor code: " + vendorCode);
                }

                double quotedPrice = Double.parseDouble(
                        priceText.replace(",", ""));

                VendorQuote quote = new VendorQuote();

                quote.setQuoteId(
                        "Q-" + rfxId + "-" + vendorId + "-" + itemId);

                quote.setRfxId(rfxId);
                quote.setVendorId(vendorId);
                quote.setVendorName(vendorName);
                quote.setItemId(itemId);
                quote.setQuotedPrice(quotedPrice);
                quote.setQuotedUnit(quotedUnit);
                quote.setCurrency(currency);

                quote.setFreightIncluded(
                        "Included".equalsIgnoreCase(freight));

                quote.setFreightAmount(
                        FREIGHT_AMOUNT_MAP.get(vendorCode));

                quote.setFreightType(
                        FREIGHT_TYPE_MAP.get(vendorCode));

                quote.setQuotedQuantity(1);

                quotes.add(quote);
            }
        }

        // Delete existing imported IDs for this RFx.
        quoteRepository.deleteAllById(
                quotes.stream()
                        .map(VendorQuote::getQuoteId)
                        .toList());

        quoteRepository.saveAll(quotes);

        return quotes.size();
    }
}
