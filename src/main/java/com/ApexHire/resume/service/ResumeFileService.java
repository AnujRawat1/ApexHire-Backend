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

    @Value("${resume.file-storage.location:storage/resumes}")
    private String storageLocation;

    @Value("${resume.file-storage.max-size:10485760}")
    private long maxFileSize;

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";

    public FileMetadata storeFile(MultipartFile file, String userId) {
        validateFile(file);

        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String storedFilename = fileId + PDF_EXTENSION;

        Path userStoragePath = Paths.get(storageLocation, userId);
        Path filePath = userStoragePath.resolve(storedFilename);

        try {
            Files.createDirectories(userStoragePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileStorageKey = Paths.get(userId, storedFilename).toString();

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
            Path filePath = Paths.get(storageLocation, fileStorageKey);
            if (!Files.exists(filePath)) {
                throw new ResumeFileException("File not found: " + fileStorageKey);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to retrieve resume file: fileStorageKey={}", fileStorageKey, e);
            throw new ResumeFileException("Failed to retrieve resume file", e);
        }
    }

    public void deleteFile(String fileStorageKey) {
        try {
            Path filePath = Paths.get(storageLocation, fileStorageKey);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted resume file: fileStorageKey={}", fileStorageKey);
            }
        } catch (IOException e) {
            log.error("Failed to delete resume file: fileStorageKey={}", fileStorageKey, e);
            throw new ResumeFileException("Failed to delete resume file", e);
        }
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
