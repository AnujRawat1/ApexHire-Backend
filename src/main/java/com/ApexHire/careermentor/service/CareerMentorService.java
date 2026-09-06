package com.ApexHire.careermentor.service;

import com.ApexHire.careermentor.client.PythonCareerMentorClient;
import com.ApexHire.careermentor.document.CareerMentorSession;
import com.ApexHire.careermentor.document.CareerMentorSession.CareerMentorMessage;
import com.ApexHire.careermentor.dto.request.CreateCareerMentorSessionRequest;
import com.ApexHire.careermentor.dto.request.SendMessageRequest;
import com.ApexHire.careermentor.dto.request.UpdateSessionTitleRequest;
import com.ApexHire.careermentor.dto.response.CareerMentorMessageResponse;
import com.ApexHire.careermentor.dto.response.CareerMentorSessionResponse;
import com.ApexHire.careermentor.dto.response.PaginatedCareerMentorSessionsResponse;
import com.ApexHire.careermentor.repository.CareerMentorSessionRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CareerMentorService {

    private final CareerMentorSessionRepository sessionRepository;
    private final PythonCareerMentorClient pythonClient;
    private final ResumeReportRepository resumeReportRepository;
    private final ResumeTextExtractionService resumeTextExtractionService;
    private final UserRepository userRepository;

    @Transactional
    public CareerMentorSessionResponse createSession(CreateCareerMentorSessionRequest request, String userId) {
        log.info("Creating Career Mentor session for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String candidateName = user.getName() != null && !user.getName().isBlank() ? user.getName() : "Candidate";
        String targetRole = request.getTargetRole() != null && !request.getTargetRole().isBlank()
                ? request.getTargetRole().trim()
                : "Software Engineer";
        String careerGoal = request.getCareerGoal() != null && !request.getCareerGoal().isBlank()
                ? request.getCareerGoal().trim()
                : null;

        String resumeReportId = request.getResumeReportId() != null && !request.getResumeReportId().isBlank()
                ? request.getResumeReportId().trim()
                : null;

        // Auto-link latest resume report if not explicitly provided
        if (resumeReportId == null) {
            Page<ResumeReport> userReports = resumeReportRepository.findAllByUserId(
                    userId,
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
            );
            if (!userReports.isEmpty()) {
                resumeReportId = userReports.getContent().get(0).getId();
            }
        }

        String initialTitle = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : "Mentorship: " + targetRole;

        CareerMentorSession session = CareerMentorSession.builder()
                .userId(userId)
                .title(initialTitle)
                .targetRole(targetRole)
                .careerGoal(careerGoal)
                .resumeReportId(resumeReportId)
                .messages(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String resumeText = resolveResumeText(resumeReportId, userId);

        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            String userPrompt = request.getInitialMessage().trim();

            CareerMentorMessage userMessage = CareerMentorMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .role("user")
                    .content(userPrompt)
                    .timestamp(LocalDateTime.now())
                    .suggestedFollowUps(Collections.emptyList())
                    .build();
            session.getMessages().add(userMessage);

            PythonCareerMentorClient.PythonCareerMentorRequest pyRequest =
                    new PythonCareerMentorClient.PythonCareerMentorRequest(
                            userPrompt,
                            targetRole,
                            careerGoal,
                            resumeText,
                            null,
                            candidateName,
                            Collections.emptyList()
                    );

            PythonCareerMentorClient.PythonCareerMentorResponse pyResponse = pythonClient.chat(pyRequest);

            CareerMentorMessage assistantMessage = CareerMentorMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .role("assistant")
                    .content(pyResponse.reply())
                    .timestamp(LocalDateTime.now())
                    .suggestedFollowUps(pyResponse.suggestedFollowUps() != null ? pyResponse.suggestedFollowUps() : Collections.emptyList())
                    .build();
            session.getMessages().add(assistantMessage);

            if (request.getTitle() == null || request.getTitle().isBlank()) {
                session.setTitle(generateTitleFromMessage(userPrompt, targetRole));
            }
        } else {
            // Provide an immediate welcoming greeting from the mentor
            String greeting = String.format(
                    "Hello %s! I'm your ApexHire Career Mentor. Whether you want to prepare for technical interviews, transition to %s, build a high-impact roadmap, or review skills gaps, I'm here to advise you. What's top of mind for you today?",
                    candidateName, targetRole
            );
            List<String> defaultFollowUps = List.of(
                    "What are the highest-impact skills to focus on for " + targetRole + "?",
                    "How should I structure my preparation for technical interviews?",
                    "Can you evaluate my background and suggest a 90-day transition roadmap?"
            );

            CareerMentorMessage welcomeMessage = CareerMentorMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .role("assistant")
                    .content(greeting)
                    .timestamp(LocalDateTime.now())
                    .suggestedFollowUps(defaultFollowUps)
                    .build();
            session.getMessages().add(welcomeMessage);
        }

        CareerMentorSession saved = sessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    public PaginatedCareerMentorSessionsResponse getUserSessions(String userId, int page, int limit) {
        int safePage = Math.max(0, page);
        int safeLimit = Math.min(Math.max(1, limit), 50);

        Page<CareerMentorSession> pageResult = sessionRepository.findByUserIdOrderByUpdatedAtDesc(
                userId,
                PageRequest.of(safePage, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        List<CareerMentorSessionResponse> sessionResponses = pageResult.getContent().stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());

        return PaginatedCareerMentorSessionsResponse.builder()
                .sessions(sessionResponses)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalSessions(pageResult.getTotalElements())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    public CareerMentorSessionResponse getSessionById(String id, String userId) {
        CareerMentorSession session = sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career mentor session not found"));
        return mapToSessionResponse(session);
    }

    @Transactional
    public CareerMentorSessionResponse sendMessage(String sessionId, SendMessageRequest request, String userId) {
        CareerMentorSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career mentor session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String candidateName = user.getName() != null && !user.getName().isBlank() ? user.getName() : "Candidate";

        // Update session metadata if passed in request
        if (request.getTargetRole() != null && !request.getTargetRole().isBlank()) {
            session.setTargetRole(request.getTargetRole().trim());
        }
        if (request.getCareerGoal() != null) {
            session.setCareerGoal(request.getCareerGoal().trim());
        }
        if (request.getResumeReportId() != null && !request.getResumeReportId().isBlank()) {
            session.setResumeReportId(request.getResumeReportId().trim());
        }

        String userPrompt = request.getMessage().trim();

        // Prepare chat history (sliding window of last 10 messages)
        List<CareerMentorMessage> existingMessages = session.getMessages() != null
                ? session.getMessages()
                : new ArrayList<>();

        int historyStart = Math.max(0, existingMessages.size() - 10);
        List<PythonCareerMentorClient.PythonChatMessage> historyPayload = existingMessages.subList(historyStart, existingMessages.size())
                .stream()
                .map(m -> new PythonCareerMentorClient.PythonChatMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toList());

        // Append user message
        CareerMentorMessage userMsg = CareerMentorMessage.builder()
                .id(UUID.randomUUID().toString())
                .role("user")
                .content(userPrompt)
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(Collections.emptyList())
                .build();
        existingMessages.add(userMsg);

        String resumeText = resolveResumeText(session.getResumeReportId(), userId);

        PythonCareerMentorClient.PythonCareerMentorRequest pyRequest =
                new PythonCareerMentorClient.PythonCareerMentorRequest(
                        userPrompt,
                        session.getTargetRole(),
                        session.getCareerGoal(),
                        resumeText,
                        null,
                        candidateName,
                        historyPayload
                );

        PythonCareerMentorClient.PythonCareerMentorResponse pyResponse = pythonClient.chat(pyRequest);

        CareerMentorMessage assistantMsg = CareerMentorMessage.builder()
                .id(UUID.randomUUID().toString())
                .role("assistant")
                .content(pyResponse.reply())
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(pyResponse.suggestedFollowUps() != null ? pyResponse.suggestedFollowUps() : Collections.emptyList())
                .build();
        existingMessages.add(assistantMsg);

        // Update title if session still has generic title and user just asked their first custom question
        if (session.getTitle() != null && session.getTitle().startsWith("Mentorship:")) {
            session.setTitle(generateTitleFromMessage(userPrompt, session.getTargetRole()));
        }

        session.setMessages(existingMessages);
        session.setUpdatedAt(LocalDateTime.now());

        CareerMentorSession saved = sessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    @Transactional
    public CareerMentorSessionResponse updateSessionTitle(String sessionId, UpdateSessionTitleRequest request, String userId) {
        CareerMentorSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career mentor session not found"));

        session.setTitle(request.getTitle().trim());
        session.setUpdatedAt(LocalDateTime.now());

        CareerMentorSession saved = sessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    @Transactional
    public void deleteSession(String sessionId, String userId) {
        CareerMentorSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career mentor session not found"));
        sessionRepository.delete(session);
        log.info("Deleted career mentor session id={} for userId={}", sessionId, userId);
    }

    private String resolveResumeText(String resumeReportId, String userId) {
        if (resumeReportId == null || resumeReportId.isBlank()) {
            return null;
        }
        return resumeReportRepository.findByIdAndUserId(resumeReportId.trim(), userId)
                .map(ResumeReport::getResumeText)
                .map(resumeTextExtractionService::sanitizeExtractedText)
                .orElse(null);
    }

    private String generateTitleFromMessage(String message, String targetRole) {
        if (message == null || message.isBlank()) {
            return "Mentorship: " + (targetRole != null ? targetRole : "Career");
        }
        String clean = message.trim().replaceAll("\\s+", " ");
        if (clean.length() <= 36) {
            return clean;
        }
        return clean.substring(0, 33) + "...";
    }

    private CareerMentorSessionResponse mapToSessionResponse(CareerMentorSession session) {
        List<CareerMentorMessageResponse> messageResponses = session.getMessages() != null
                ? session.getMessages().stream()
                .map(m -> CareerMentorMessageResponse.builder()
                        .id(m.getId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .timestamp(m.getTimestamp())
                        .suggestedFollowUps(m.getSuggestedFollowUps() != null ? m.getSuggestedFollowUps() : Collections.emptyList())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return CareerMentorSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .title(session.getTitle())
                .targetRole(session.getTargetRole())
                .careerGoal(session.getCareerGoal())
                .resumeReportId(session.getResumeReportId())
                .messages(messageResponses)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
