package com.ApexHire.resume.service;

import com.ApexHire.resume.client.PythonResumeClient;
import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.exception.ResumeAnalysisException;
import com.ApexHire.resume.exception.ResumeFileException;
import com.ApexHire.resume.repository.ResumeReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAnalysisService {

    private final ResumeFileService resumeFileService;
    private final ResumeTextExtractionService resumeTextExtractionService;
    private final PythonResumeClient pythonResumeClient;
    private final ResumeReportRepository resumeReportRepository;

    @Transactional
    public ResumeReport analyzeResume(
            MultipartFile file,
            String targetRole,
            String experienceLevel,
            String resumeTitle,
            String jobDescription,
            String userId
    ) {
        log.info("Starting resume analysis: userId={}, targetRole={}, experienceLevel={}", userId, targetRole, experienceLevel);

        try {
            ResumeFileService.FileMetadata fileMetadata = resumeFileService.storeFile(file, userId);

            String resumeText = resumeTextExtractionService.extractText(file);

            PythonResumeClient.PythonAnalysisRequest pythonRequest =
                    new PythonResumeClient.PythonAnalysisRequest(
                            resumeText,
                            targetRole,
                            experienceLevel,
                            jobDescription
                    );

            var analysisResult = pythonResumeClient.analyzeResume(pythonRequest);

            String title = resumeTitle != null && !resumeTitle.trim().isEmpty()
                    ? resumeTitle.trim()
                    : fileMetadata.fileName();

            ResumeReport report = ResumeReport.builder()
                    .userId(userId)
                    .title(title)
                    .fileName(fileMetadata.fileName())
                    .fileSize(fileMetadata.fileSize())
                    .fileStorageKey(fileMetadata.fileStorageKey())
                    .targetRole(targetRole)
                    .experienceLevel(experienceLevel)
                    .jobDescription(jobDescription)
                    .resumeText(resumeText)
                    .analysis(analysisResult)
                    .createdAt(LocalDateTime.now())
                    .analyzedAt(LocalDateTime.now())
                    .build();

            ResumeReport savedReport = resumeReportRepository.save(report);

            log.info("Resume analysis completed successfully: reportId={}, userId={}", savedReport.getId(), userId);

            return savedReport;

        } catch (ResumeFileException | ResumeAnalysisException e) {
            log.error("Resume analysis failed: userId={}", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during resume analysis: userId={}", userId, e);
            throw new ResumeAnalysisException("Failed to analyze resume: " + e.getMessage(), e);
        }
    }
}
