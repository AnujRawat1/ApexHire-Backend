package com.ApexHire.careermentor.controller;

import com.ApexHire.careermentor.dto.request.CreateCareerMentorSessionRequest;
import com.ApexHire.careermentor.dto.request.SendMessageRequest;
import com.ApexHire.careermentor.dto.request.UpdateSessionTitleRequest;
import com.ApexHire.careermentor.dto.response.CareerMentorSessionResponse;
import com.ApexHire.careermentor.dto.response.PaginatedCareerMentorSessionsResponse;
import com.ApexHire.careermentor.service.CareerMentorService;
import com.ApexHire.resume.dto.response.DeleteResponse;
import com.ApexHire.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "AI Career Mentor",
        description = "Executive career coaching, interview strategy, and multi-turn career mentor conversation APIs"
)
@RestController
@RequestMapping("/api/career-mentor")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class CareerMentorController {

    private final CareerMentorService careerMentorService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Create career mentor session",
            description = "Start a new career mentorship conversation session with optional initial message and target role"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/sessions")
    public ResponseEntity<CareerMentorSessionResponse> createSession(
            @Valid @RequestBody(required = false) CreateCareerMentorSessionRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CreateCareerMentorSessionRequest safeRequest = request != null ? request : new CreateCareerMentorSessionRequest();
        CareerMentorSessionResponse response = careerMentorService.createSession(safeRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get user mentor sessions",
            description = "Fetch paginated history of user's career mentor sessions"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/sessions")
    public ResponseEntity<PaginatedCareerMentorSessionsResponse> getUserSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        PaginatedCareerMentorSessionsResponse response = careerMentorService.getUserSessions(userId, page, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get mentor session by ID",
            description = "Retrieve full conversation history for a specific career mentor session"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/sessions/{id}")
    public ResponseEntity<CareerMentorSessionResponse> getSession(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CareerMentorSessionResponse response = careerMentorService.getSessionById(id, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Send message to career mentor",
            description = "Send a prompt to the AI mentor within an existing session and get a contextual response with follow-up suggestions"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<CareerMentorSessionResponse> sendMessage(
            @PathVariable String id,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CareerMentorSessionResponse response = careerMentorService.sendMessage(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update session title",
            description = "Rename an existing career mentor session"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/sessions/{id}/title")
    public ResponseEntity<CareerMentorSessionResponse> updateSessionTitle(
            @PathVariable String id,
            @Valid @RequestBody UpdateSessionTitleRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        CareerMentorSessionResponse response = careerMentorService.updateSessionTitle(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete session",
            description = "Permanently delete a career mentor session and its conversation history"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<DeleteResponse> deleteSession(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);
        careerMentorService.deleteSession(id, userId);
        return ResponseEntity.ok(DeleteResponse.builder()
                .success(true)
                .message("Career mentor session deleted successfully")
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
