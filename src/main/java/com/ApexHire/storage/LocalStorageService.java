package com.ApexHire.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class LocalStorageService {

    @Value("${resume.file-storage.location:storage}")
    private String baseLocation = "storage";

    /**
     * Ensures and returns the directory path for a user's specific storage category
     * (e.g. storage/{userId}/resume, storage/{userId}/cover_letter, storage/{userId}/resume_report).
     */
    public Path getUserDirectory(String userId, String category) {
        Path path = Paths.get(baseLocation, userId, category);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("Failed to create storage directory for user={}, category={}: {}", userId, category, path, e);
        }
        return path;
    }

    // Cover Letter storage
    public void saveCoverLetter(String userId, String letterId, String content) {
        try {
            Path dir = getUserDirectory(userId, "cover_letter");
            Path file = dir.resolve(letterId + ".txt");
            Files.writeString(file, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved cover letter file: {}", file);
        } catch (IOException e) {
            log.error("Failed to save cover letter file for userId={}, letterId={}: {}", userId, letterId, e.getMessage(), e);
        }
    }

    public String getCoverLetter(String userId, String letterId) {
        try {
            Path file = Paths.get(baseLocation, userId, "cover_letter", letterId + ".txt");
            if (Files.exists(file)) {
                return Files.readString(file, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to read cover letter file: userId={}, letterId={}", userId, letterId, e);
        }
        return null;
    }

    public void deleteCoverLetter(String userId, String letterId) {
        try {
            Path file = Paths.get(baseLocation, userId, "cover_letter", letterId + ".txt");
            Files.deleteIfExists(file);
            log.info("Deleted cover letter file: {}", file);
        } catch (IOException e) {
            log.warn("Failed to delete cover letter file for letterId={}: {}", letterId, e.getMessage());
        }
    }

    // Resume Report PDF storage
    public void saveReportPdf(String userId, String reportId, byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) return;
        try {
            Path dir = getUserDirectory(userId, "resume_report");
            Path file = dir.resolve(reportId + ".pdf");
            Files.write(file, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved resume report PDF to storage: {}", file);
        } catch (IOException e) {
            log.error("Failed to save resume report PDF for userId={}, reportId={}: {}", userId, reportId, e.getMessage(), e);
        }
    }

    public byte[] getReportPdf(String userId, String reportId) {
        try {
            Path file = Paths.get(baseLocation, userId, "resume_report", reportId + ".pdf");
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }
        } catch (IOException e) {
            log.warn("Failed to read report PDF from storage: reportId={}", reportId, e);
        }
        return null;
    }

    public void deleteReportPdf(String userId, String reportId) {
        try {
            Path file = Paths.get(baseLocation, userId, "resume_report", reportId + ".pdf");
            Files.deleteIfExists(file);
            log.info("Deleted resume report PDF: {}", file);
        } catch (IOException e) {
            log.warn("Failed to delete report PDF for reportId={}: {}", reportId, e.getMessage());
        }
    }
}
