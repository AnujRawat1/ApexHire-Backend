package com.ApexHire.resume.service;

import com.ApexHire.resume.exception.ResumeFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ResumeFileService {

    @Value("${resume.file-storage.location:storage}")
    private String storageLocation = "storage";

    @Value("${resume.file-storage.max-size:10485760}")
    private long maxFileSize = 10485760L;

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";

    public FileMetadata storeFile(MultipartFile file, String userId) {
        validateFile(file);

        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String storedFilename = fileId + PDF_EXTENSION;

        // Structured storage: storage/{userId}/resume/{storedFilename}
        Path userResumeDir = Paths.get(storageLocation, userId, "resume");
        Path filePath = userResumeDir.resolve(storedFilename);

        try {
            Files.createDirectories(userResumeDir);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileStorageKey = Paths.get(userId, "resume", storedFilename).toString();

            log.info("Stored resume file: userId={}, fileId={}, originalFilename={}, size={}",
                    userId, fileId, originalFilename, file.getSize());

            return new FileMetadata(fileId, originalFilename, file.getSize(), fileStorageKey);
        } catch (IOException e) {
            log.error("Failed to store resume file: userId={}, fileId={}", userId, fileId, e);
            throw new ResumeFileException("Failed to store resume file", e);
        }
    }

    public byte[] retrieveFile(String fileStorageKey) {
        try {
            Path filePath = resolveFilePath(fileStorageKey);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to retrieve resume file: fileStorageKey={}", fileStorageKey, e);
            throw new ResumeFileException("Failed to retrieve resume file", e);
        }
    }

    public void deleteFile(String fileStorageKey) {
        try {
            Path filePath = resolveFilePath(fileStorageKey);
            Files.deleteIfExists(filePath);
            log.info("Deleted resume file: fileStorageKey={}", fileStorageKey);
        } catch (Exception e) {
            log.error("Failed to delete resume file: fileStorageKey={}", fileStorageKey, e);
            throw new ResumeFileException("Failed to delete resume file", e);
        }
    }

    private Path resolveFilePath(String fileStorageKey) {
        // 1. Direct path from storage location: storage/{fileStorageKey}
        Path p1 = Paths.get(storageLocation, fileStorageKey);
        if (Files.exists(p1)) return p1;

        // 2. If fileStorageKey was legacy "userId/fileId.pdf", check storage/{userId}/resume/{fileId.pdf}
        if (!fileStorageKey.contains("resume")) {
            String updatedKey = fileStorageKey.replaceFirst("^([^/\\\\]+)[/\\\\]", "$1/resume/");
            Path p2 = Paths.get(storageLocation, updatedKey);
            if (Files.exists(p2)) return p2;
        }

        // 3. Fallback to old storage/resumes/ directory
        Path p3 = Paths.get(storageLocation, "resumes", fileStorageKey);
        if (Files.exists(p3)) return p3;

        Path p4 = Paths.get("storage", "resumes", fileStorageKey);
        if (Files.exists(p4)) return p4;

        throw new ResumeFileException("File not found: " + fileStorageKey);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResumeFileException("File is empty");
        }

        if (!PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new ResumeFileException("Only PDF files are allowed");
        }

        if (file.getSize() > maxFileSize) {
            throw new ResumeFileException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(PDF_EXTENSION)) {
            throw new ResumeFileException("File must have .pdf extension");
        }
    }

    public record FileMetadata(String fileId, String fileName, long fileSize, String fileStorageKey) {
    }
}
