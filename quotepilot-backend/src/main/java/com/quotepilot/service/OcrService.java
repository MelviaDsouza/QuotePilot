
package com.quotepilot.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@RequiredArgsConstructor
public class OcrService {

    private static final String TESSERACT_DATA_PATH =
            "C:/Program Files/Tesseract-OCR/tessdata";

    public String extractText(BufferedImage image) throws TesseractException {

        ITesseract tesseract = new Tesseract();

        tesseract.setDatapath(TESSERACT_DATA_PATH);
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(6);
        tesseract.setVariable("user_defined_dpi", "300");

        return tesseract.doOCR(image);
    }
}
