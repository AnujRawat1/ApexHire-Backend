package com.ApexHire.coverletter.controller;

import com.ApexHire.coverletter.dto.request.UpdateCoverLetterRequest;
import com.ApexHire.coverletter.dto.response.CoverLetterResponse;
import com.ApexHire.coverletter.dto.response.PaginatedCoverLettersResponse;
import com.ApexHire.coverletter.service.CoverLetterService;
import com.ApexHire.resume.dto.response.DeleteResponse;
import com.ApexHire.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "Cover Letter Generator",
        description = "AI-powered personalized cover letter generation and management APIs"
)
@RestController
@RequestMapping("/api/cover-letters")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterController {

    private final CoverLetterService coverLetterService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Generate cover letter",
            description = "Generate an AI tailored cover letter from job, company, tone, and optional resume"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CoverLetterResponse> generateCoverLetter(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "resumeReportId", required = false) String resumeReportId,
            @RequestParam("targetRole") String targetRole,
            @RequestParam("companyName") String companyName,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            @RequestParam(value = "skills", required = false) String skills,
            @RequestParam(value = "additionalInfo", required = false) String additionalInfo,
            @RequestParam(value = "tone", required = false, defaultValue = "Professional") String tone,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CoverLetterResponse response = coverLetterService.generateCoverLetter(
                file, resumeReportId, targetRole, companyName, jobDescription, skills, additionalInfo, tone, userId
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get user cover letters",
            description = "Get paginated history of user's generated cover letters"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<PaginatedCoverLettersResponse> getUserCoverLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        PaginatedCoverLettersResponse response = coverLetterService.getUserCoverLetters(userId, page, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get cover letter by ID",
            description = "Fetch single cover letter document by ID"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<CoverLetterResponse> getCoverLetter(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CoverLetterResponse response = coverLetterService.getCoverLetterById(id, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update cover letter",
            description = "Save user edits made to cover letter content and title"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<CoverLetterResponse> updateCoverLetter(
            @PathVariable String id,
            @Valid @RequestBody UpdateCoverLetterRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CoverLetterResponse response = coverLetterService.updateCoverLetter(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete cover letter",
            description = "Delete a cover letter by ID"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteCoverLetter(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        coverLetterService.deleteCoverLetter(id, userId);
        return ResponseEntity.ok(DeleteResponse.builder()
                .success(true)
                .message("Cover letter deleted successfully")
                .build());
    }

    private String getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getId();
    }
}
