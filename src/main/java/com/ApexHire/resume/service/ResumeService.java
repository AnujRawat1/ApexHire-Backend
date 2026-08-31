package com.ApexHire.resume.service;

import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.dto.request.UpdateResumeRequest;
import com.ApexHire.resume.dto.response.PaginatedReportsResponse;
import com.ApexHire.resume.dto.response.ResumeReportResponse;
import com.ApexHire.resume.exception.ResumeNotFoundException;
import com.ApexHire.resume.repository.ResumeReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeReportRepository resumeReportRepository;
    private final ResumeFileService resumeFileService;

    public PaginatedReportsResponse getUserReports(
            String userId,
            int page,
            int limit,
            String role,
            String level,
            String search,
            String sort
    ) {
        log.info("Fetching user reports: userId={}, page={}, limit={}, role={}, level={}, search={}, sort={}",
                userId, page, limit, role, level, search, sort);

        Sort sortObj = createSort(sort);
        Pageable pageable = PageRequest.of(page, limit, sortObj);

        Page<ResumeReport> reportsPage;

        if (search != null && !search.trim().isEmpty()) {
            reportsPage = resumeReportRepository.findByUserIdAndSearch(userId, search.trim(), pageable);
        } else if (role != null && !role.trim().isEmpty() && !role.equals("all")) {
            reportsPage = resumeReportRepository.findByUserIdAndTargetRole(userId, role, pageable);
        } else if (level != null && !level.trim().isEmpty() && !level.equals("all")) {
            reportsPage = resumeReportRepository.findByUserIdAndExperienceLevel(userId, level, pageable);
        } else {
            reportsPage = resumeReportRepository.findAllByUserId(userId, pageable);
        }

        List<ResumeReportResponse> responses = reportsPage.stream()
                .map(this::mapToResponse)
                .toList();

        return PaginatedReportsResponse.builder()
                .reports(responses)
                .total(reportsPage.getTotalElements())
                .page(page)
                .limit(limit)
                .totalPages(reportsPage.getTotalPages())
                .build();
    }

    public ResumeReportResponse getReportById(String id, String userId) {
        log.info("Fetching report by id: reportId={}, userId={}", id, userId);

        ResumeReport report = resumeReportRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found"));

        return mapToResponse(report);
    }

    @Transactional
    public ResumeReportResponse updateReport(String id, UpdateResumeRequest request, String userId) {
        log.info("Updating report: reportId={}, userId={}, title={}", id, userId, request.getTitle());

        ResumeReport report = resumeReportRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found"));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            report.setTitle(request.getTitle().trim());
        }

        ResumeReport updatedReport = resumeReportRepository.save(report);
        return mapToResponse(updatedReport);
    }

    @Transactional
    public void deleteReport(String id, String userId) {
        log.info("Deleting report: reportId={}, userId={}", id, userId);

        ResumeReport report = resumeReportRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found"));

        if (report.getFileStorageKey() != null) {
            try {
                resumeFileService.deleteFile(report.getFileStorageKey());
            } catch (Exception e) {
                log.error("Failed to delete file for report: reportId={}", id, e);
            }
        }

        resumeReportRepository.deleteByIdAndUserId(id, userId);
        log.info("Report deleted successfully: reportId={}", id);
    }

    public byte[] getReportFile(String id, String userId) {
        log.info("Fetching report file: reportId={}, userId={}", id, userId);

        ResumeReport report = resumeReportRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found"));

        if (report.getFileStorageKey() == null) {
            throw new ResumeNotFoundException("File not available for this report");
        }

        return resumeFileService.retrieveFile(report.getFileStorageKey());
    }

    private Sort createSort(String sort) {
        return switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "score-high" -> Sort.by(Sort.Direction.DESC, "analysis.overallScore");
            case "score-low" -> Sort.by(Sort.Direction.ASC, "analysis.overallScore");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private ResumeReportResponse mapToResponse(ResumeReport report) {
        return ResumeReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .fileName(report.getFileName())
                .fileSize(report.getFileSize())
                .targetRole(report.getTargetRole())
                .experienceLevel(report.getExperienceLevel())
                .jobDescription(report.getJobDescription())
                .analysis(report.getAnalysis())
                .createdAt(report.getCreatedAt())
                .analyzedAt(report.getAnalyzedAt())
                .build();
    }
}
