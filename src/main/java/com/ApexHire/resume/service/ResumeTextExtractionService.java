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

            text = text.trim();

            if (text.length() < MIN_TEXT_LENGTH) {
                throw new ResumeFileException("Extracted text is too short for analysis (minimum " + MIN_TEXT_LENGTH + " characters)");
            }

            log.info("Successfully extracted text from PDF: {} characters", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            throw new ResumeFileException("Failed to extract text from PDF", e);
        }
    }
}
