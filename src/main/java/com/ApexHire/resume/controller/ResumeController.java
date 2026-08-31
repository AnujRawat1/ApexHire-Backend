package com.ApexHire.resume.controller;

import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.dto.request.UpdateResumeRequest;
import com.ApexHire.resume.dto.response.DeleteResponse;
import com.ApexHire.resume.dto.response.FileUploadResponse;
import com.ApexHire.resume.dto.response.PaginatedReportsResponse;
import com.ApexHire.resume.dto.response.ResumeReportResponse;
import com.ApexHire.resume.service.ResumeAnalysisService;
import com.ApexHire.resume.service.ResumeFileService;
import com.ApexHire.resume.service.ResumeService;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Tag(
        name = "Resume Analysis",
        description = "Resume upload, analysis, and report management APIs"
)
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeService resumeService;
    private final ResumeFileService resumeFileService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Analyze resume",
            description = "Upload a PDF resume and get AI-powered analysis"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeReportResponse> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetRole") String targetRole,
            @RequestParam("experienceLevel") String experienceLevel,
            @RequestParam(value = "resumeTitle", required = false) String resumeTitle,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        ResumeReport report = resumeAnalysisService.analyzeResume(
                file, targetRole, experienceLevel, resumeTitle, jobDescription, userId
        );
        return ResponseEntity.ok(mapToResponse(report));
    }

    @Operation(
            summary = "Upload resume file",
            description = "Upload a PDF resume file for storage"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        ResumeFileService.FileMetadata metadata = resumeFileService.storeFile(file, userId);
        return ResponseEntity.ok(FileUploadResponse.builder()
                .fileId(metadata.fileId())
                .fileName(metadata.fileName())
                .fileSize(metadata.fileSize())
                .uploadedAt(java.time.LocalDateTime.now().toString())
                .build());
    }

    @Operation(
            summary = "Get all reports",
            description = "Get paginated list of user's resume reports with filtering and sorting"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/reports")
    public ResponseEntity<PaginatedReportsResponse> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest") String sort,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        PaginatedReportsResponse response = resumeService.getUserReports(
                userId, page, limit, role, level, search, sort
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get report by ID",
            description = "Get detailed resume report by ID"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/reports/{id}")
    public ResponseEntity<ResumeReportResponse> getReport(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        ResumeReportResponse report = resumeService.getReportById(id, userId);
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Download resume file",
            description = "Download the original PDF file for a report"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/reports/{id}/file")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        byte[] fileBytes = resumeService.getReportFile(id, userId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                .body(fileBytes);
    }

    @Operation(
            summary = "Update report",
            description = "Update report metadata (title)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/reports/{id}")
    public ResponseEntity<ResumeReportResponse> updateReport(
            @PathVariable String id,
            @Valid @RequestBody UpdateResumeRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        ResumeReportResponse report = resumeService.updateReport(id, request, userId);
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Delete report",
            description = "Delete a resume report and its associated file"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<DeleteResponse> deleteReport(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        resumeService.deleteReport(id, userId);
        return ResponseEntity.ok(DeleteResponse.builder()
                .success(true)
                .message("Report deleted successfully")
                .build());
    }

    private String getUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
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
