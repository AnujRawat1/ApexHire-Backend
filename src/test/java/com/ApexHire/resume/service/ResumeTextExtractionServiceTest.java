package com.ApexHire.resume.service;

import com.ApexHire.resume.exception.ResumeFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ResumeTextExtractionServiceTest {

    private ResumeTextExtractionService resumeTextExtractionService;

    @BeforeEach
    void setUp() {
        resumeTextExtractionService = new ResumeTextExtractionService();
    }

    @Test
    void extractText_ValidPdf_ReturnsText() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        assertThrows(ResumeFileException.class, () -> resumeTextExtractionService.extractText(file));
    }

    @Test
    void extractText_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(ResumeFileException.class, () -> resumeTextExtractionService.extractText(file));
    }
}
