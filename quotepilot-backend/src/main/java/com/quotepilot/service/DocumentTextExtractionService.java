package com.quotepilot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Slf4j
public class DocumentTextExtractionService {

    private final OcrService ocrService;

    public DocumentTextExtractionService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    public String extractText(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Filename is missing");
        }

        String extension = getExtension(filename);

        return switch (extension) {
            case "pdf" -> extractPdfText(file.getBytes());
            case "xlsx", "xls" -> extractExcel(file);
            case "docx" -> extractDocx(file);
            case "doc" -> extractDoc(file);
            case "eml" -> extractEmail(file);
            case "txt" -> new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );
            case "png", "jpg", "jpeg" -> extractImageText(file.getBytes());
            default -> throw new IllegalArgumentException(
                    "Unsupported file format: " + extension
            );
        };
    }

    // ---------------------------------------------------------
    // PDF: Extract embedded text first; use OCR for scanned PDFs
    // ---------------------------------------------------------

    private String extractPdfText(byte[] fileBytes) throws Exception {

        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            // Normal PDF containing embedded text
            if (extractedText != null
                    && extractedText.trim().length() > 100) {

                log.info("Extracted PDF text without OCR");
                return extractedText;
            }

            // Scanned PDF: render pages and perform OCR
            log.info("PDF appears scanned. Starting OCR.");

            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder ocrText = new StringBuilder();

            for (int page = 0;
                 page < document.getNumberOfPages();
                 page++) {

                BufferedImage image = renderer.renderImageWithDPI(
                        page,
                        300,
                        ImageType.RGB
                );

                String pageText = ocrService.extractText(image);

                ocrText.append("\n--- PAGE ")
                        .append(page + 1)
                        .append(" ---\n")
                        .append(pageText);
            }

            return ocrText.toString();
        }
    }

    // ---------------------------------------------------------
    // Excel
    // ---------------------------------------------------------

    private String extractExcel(MultipartFile file) throws Exception {

        try (Workbook workbook =
                     WorkbookFactory.create(file.getInputStream())) {

            DataFormatter formatter = new DataFormatter();
            StringBuilder result = new StringBuilder();

            for (Sheet sheet : workbook) {

                result.append("\nSHEET: ")
                        .append(sheet.getSheetName())
                        .append("\n");

                for (Row row : sheet) {

                    for (Cell cell : row) {

                        result.append(formatter.formatCellValue(cell))
                                .append(" | ");
                    }

                    result.append("\n");
                }
            }

            return result.toString();
        }
    }

    // ---------------------------------------------------------
    // DOCX
    // ---------------------------------------------------------

    private String extractDocx(MultipartFile file) throws Exception {

        try (XWPFDocument document =
                     new XWPFDocument(file.getInputStream())) {

            StringBuilder result = new StringBuilder();

            document.getParagraphs().forEach(paragraph ->
                    result.append(paragraph.getText()).append("\n")
            );

            document.getTables().forEach(table -> {

                table.getRows().forEach(row -> {

                    row.getTableCells().forEach(cell ->
                            result.append(cell.getText())
                                    .append(" | ")
                    );

                    result.append("\n");
                });
            });

            return result.toString();
        }
    }

    // ---------------------------------------------------------
    // DOC
    // ---------------------------------------------------------

    private String extractDoc(MultipartFile file) throws Exception {

        try (HWPFDocument document =
                     new HWPFDocument(file.getInputStream());
             WordExtractor extractor =
                     new WordExtractor(document)) {

            return extractor.getText();
        }
    }

    // ---------------------------------------------------------
    // EML
    // ---------------------------------------------------------

    private String extractEmail(MultipartFile file) throws Exception {

        // Basic extraction for text-based email samples.
        // MIME/HTML attachments are not parsed here.

        return new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
    }

    // ---------------------------------------------------------
    // Images: PNG / JPG / JPEG
    // ---------------------------------------------------------

    private String extractImageText(byte[] fileBytes) throws Exception {

        BufferedImage image = ImageIO.read(
                new ByteArrayInputStream(fileBytes)
        );

        if (image == null) {
            throw new IllegalArgumentException(
                    "Unable to read image file"
            );
        }

        log.info("Starting OCR on uploaded image");

        return ocrService.extractText(image);
    }

    // ---------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------

    private String getExtension(String filename) {

        int dot = filename.lastIndexOf('.');

        if (dot < 0) {
            throw new IllegalArgumentException(
                    "File extension is missing"
            );
        }

        return filename.substring(dot + 1)
                .toLowerCase(Locale.ROOT);
    }
}
