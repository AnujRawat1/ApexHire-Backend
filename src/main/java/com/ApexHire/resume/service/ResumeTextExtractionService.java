package com.ApexHire.resume.service;

import com.ApexHire.resume.exception.ResumeFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class ResumeTextExtractionService {

    private static final int MIN_TEXT_LENGTH = 30;

    public String extractText(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            return extractText(fileBytes);
        } catch (IOException e) {
            log.error("Failed to read file bytes", e);
            throw new ResumeFileException("Failed to read file bytes", e);
        }
    }

    public String extractText(byte[] fileBytes) {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new ResumeFileException("No text could be extracted from the PDF");
            }

            text = sanitizeExtractedText(text);

            if (text.length() < MIN_TEXT_LENGTH) {
                throw new ResumeFileException("Extracted text is too short for analysis (minimum " + MIN_TEXT_LENGTH + " characters)");
            }

            log.info("Successfully extracted and sanitized text from PDF: {} characters", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            throw new ResumeFileException("Failed to extract text from PDF", e);
        }
    }

    public String sanitizeExtractedText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        // 1. Fix broken unit/symbol slashes: "50 /ms" -> "50ms", "100 /%" -> "100%", "10 /MB" -> "10MB"
        String cleaned = text.replaceAll("(?i)(\\d+)\\s*/\\s*([a-z%]+)", "$1$2");
        // 2. Fix broken slash numbers: "Spring Security /6" -> "Spring Security 6"
        cleaned = cleaned.replaceAll("/\\s*(\\d+)", " $1");
        // 3. Fix words split by multiple spaces: "high  performance" -> "high performance"
        cleaned = cleaned.replaceAll("([a-zA-Z0-9])\\s{2,}([a-zA-Z0-9])", "$1 $2");
        // 4. Collapse consecutive spaces/tabs on a single line
        cleaned = cleaned.replaceAll("[ \\t]{2,}", " ");
        return cleaned.trim();
    }
}
