package com.ApexHire.coverletter.service;

import com.ApexHire.coverletter.client.PythonCoverLetterClient;
import com.ApexHire.coverletter.document.CoverLetter;
import com.ApexHire.coverletter.dto.request.UpdateCoverLetterRequest;
import com.ApexHire.coverletter.dto.response.CoverLetterResponse;
import com.ApexHire.coverletter.dto.response.PaginatedCoverLettersResponse;
import com.ApexHire.coverletter.repository.CoverLetterRepository;
import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.repository.ResumeReportRepository;
import com.ApexHire.resume.service.ResumeTextExtractionService;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final PythonCoverLetterClient pythonCoverLetterClient;
    private final ResumeReportRepository resumeReportRepository;
    private final ResumeTextExtractionService resumeTextExtractionService;
    private final UserRepository userRepository;
    private final com.ApexHire.storage.LocalStorageService localStorageService;

    @Transactional
    public CoverLetterResponse generateCoverLetter(
            MultipartFile file,
            String resumeReportId,
            String targetRole,
            String companyName,
            String jobDescription,
            String skills,
            String additionalInfo,
            String tone,
            String userId
    ) {
        log.info("Generating cover letter: userId={}, role={}, company={}, tone={}",
                userId, targetRole, companyName, tone);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String candidateName = user.getName() != null && !user.getName().isBlank()
                ? user.getName()
                : "Candidate";
        String candidateEmail = user.getEmail();

        String resumeText = null;
        if (file != null && !file.isEmpty()) {
            log.info("Extracting resume text from uploaded file: {}", file.getOriginalFilename());
            resumeText = resumeTextExtractionService.extractText(file);
        } else if (resumeReportId != null && !resumeReportId.trim().isEmpty()) {
            log.info("Fetching resume text from existing report: {}", resumeReportId);
            ResumeReport report = resumeReportRepository.findByIdAndUserId(resumeReportId.trim(), userId)
                    .orElse(null);
            if (report != null) {
                resumeText = resumeTextExtractionService.sanitizeExtractedText(report.getResumeText());
            }
        }

        String effectiveTone = (tone != null && !tone.trim().isEmpty()) ? tone.trim() : "Professional";
        String effectiveRole = targetRole != null ? targetRole.trim() : "Software Engineer";
        String effectiveCompany = companyName != null ? companyName.trim() : "Hiring Company";

        PythonCoverLetterClient.PythonCoverLetterRequest pyRequest =
                new PythonCoverLetterClient.PythonCoverLetterRequest(
                        candidateName,
                        candidateEmail,
                        effectiveRole,
                        effectiveCompany,
                        jobDescription,
                        resumeText,
                        skills,
                        additionalInfo,
                        effectiveTone
                );

        PythonCoverLetterClient.PythonCoverLetterResponse pyResponse =
                pythonCoverLetterClient.generateCoverLetter(pyRequest);

        String title = effectiveRole + " — " + effectiveCompany;

        CoverLetter coverLetter = CoverLetter.builder()
                .userId(userId)
                .title(title)
                .companyName(effectiveCompany)
                .targetRole(effectiveRole)
                .tone(effectiveTone)
                .jobDescription(jobDescription)
                .skills(skills)
                .additionalInfo(additionalInfo)
                .resumeReportId(resumeReportId)
                .content(pyResponse.content())
                .keyHighlights(pyResponse.keyHighlights())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CoverLetter saved = coverLetterRepository.save(coverLetter);
        log.info("Successfully generated and saved cover letter: id={}, userId={}", saved.getId(), userId);

        // Store in structured local directory storage/{userId}/cover_letter/{id}.txt
        localStorageService.saveCoverLetter(userId, saved.getId(), saved.getContent());

        return mapToResponse(saved);
    }

    public PaginatedCoverLettersResponse getUserCoverLetters(String userId, int page, int limit) {
        int effectivePage = Math.max(0, page);
        int effectiveLimit = (limit > 0 && limit <= 100) ? limit : 10;

        PageRequest pageRequest = PageRequest.of(
                effectivePage,
                effectiveLimit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CoverLetter> letterPage = coverLetterRepository.findAllByUserId(userId, pageRequest);

        return PaginatedCoverLettersResponse.builder()
                .items(letterPage.getContent().stream().map(this::mapToResponse).toList())
                .total(letterPage.getTotalElements())
                .page(effectivePage)
                .limit(effectiveLimit)
                .totalPages(letterPage.getTotalPages())
                .build();
    }

    public CoverLetterResponse getCoverLetterById(String id, String userId) {
        CoverLetter letter = coverLetterRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cover letter not found"));
        return mapToResponse(letter);
    }

    @Transactional
    public CoverLetterResponse updateCoverLetter(String id, UpdateCoverLetterRequest request, String userId) {
        CoverLetter letter = coverLetterRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cover letter not found"));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            letter.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            letter.setContent(request.getContent());
        }
        letter.setUpdatedAt(LocalDateTime.now());

        CoverLetter updated = coverLetterRepository.save(letter);
        log.info("Updated cover letter: id={}, userId={}", updated.getId(), userId);

        // Update local file storage
        localStorageService.saveCoverLetter(userId, updated.getId(), updated.getContent());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCoverLetter(String id, String userId) {
        if (!coverLetterRepository.existsByIdAndUserId(id, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cover letter not found");
        }
        coverLetterRepository.deleteByIdAndUserId(id, userId);
        localStorageService.deleteCoverLetter(userId, id);
        log.info("Deleted cover letter: id={}, userId={}", id, userId);
    }


    private CoverLetterResponse mapToResponse(CoverLetter letter) {
        return CoverLetterResponse.builder()
                .id(letter.getId())
                .title(letter.getTitle())
                .companyName(letter.getCompanyName())
                .targetRole(letter.getTargetRole())
                .tone(letter.getTone())
                .jobDescription(letter.getJobDescription())
                .skills(letter.getSkills())
                .additionalInfo(letter.getAdditionalInfo())
                .resumeReportId(letter.getResumeReportId())
                .content(letter.getContent())
                .keyHighlights(letter.getKeyHighlights())
                .createdAt(letter.getCreatedAt())
                .updatedAt(letter.getUpdatedAt())
                .build();
    }
}
