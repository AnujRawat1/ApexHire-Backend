package com.ApexHire.resume.service;

import com.ApexHire.resume.exception.ResumeFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResumeFileServiceTest {

    private ResumeFileService resumeFileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        resumeFileService = new ResumeFileService();
    }

    @Test
    void storeFile_ValidPdf_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        assertDoesNotThrow(() -> resumeFileService.storeFile(file, "user123"));
    }

    @Test
    void storeFile_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(ResumeFileException.class, () -> resumeFileService.storeFile(file, "user123"));
    }

    @Test
    void storeFile_InvalidContentType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "test content".getBytes()
        );

        assertThrows(ResumeFileException.class, () -> resumeFileService.storeFile(file, "user123"));
    }
}
