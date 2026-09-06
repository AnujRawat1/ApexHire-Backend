package com.ApexHire.careermentor.client;

import com.ApexHire.resume.exception.PythonServiceException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class PythonCareerMentorClient {

    private final WebClient webClient;

    @Value("${python.resume-service.url}")
    private String pythonServiceUrl;

    @Value("${python.resume-service.api-key}")
    private String pythonServiceApiKey;

    @Value("${python.resume-service.timeout:120s}")
    private Duration timeout;

    public PythonCareerMentorClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public PythonCareerMentorResponse chat(PythonCareerMentorRequest request) {
        log.info("Sending chat request to Python Career Mentor service: targetRole={}, goal={}",
                request.targetRole(), request.careerGoal());

        try {
            return webClient.post()
                    .uri(pythonServiceUrl + "/api/career-mentor/chat")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + pythonServiceApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PythonCareerMentorResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(this::isTransientError))
                    .doOnError(error -> log.error("Python career mentor service error", error))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Python service returned error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw handleWebClientException(e);
        } catch (Exception e) {
            log.error("Failed to call Python career mentor service", e);
            throw new PythonServiceException("Failed to get mentor response: " + e.getMessage(), e);
        }
    }

    private boolean isTransientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int status = ex.getStatusCode().value();
            return status >= 500 && status < 600;
        }
        return false;
    }

    private PythonServiceException handleWebClientException(WebClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> new PythonServiceException("Invalid request to Python career mentor service");
            case 401 -> new PythonServiceException("Python career mentor service authentication failed");
            case 403 -> new PythonServiceException("Python career mentor service access denied");
            case 404 -> new PythonServiceException("Python career mentor service endpoint not found");
            case 429 -> new PythonServiceException("Python career mentor service rate limit exceeded");
            case 500, 502, 503, 504 -> new PythonServiceException("Python career mentor service temporarily unavailable");
            default -> new PythonServiceException("Python career mentor service error: " + e.getMessage());
        };
    }

    public record PythonCareerMentorRequest(
            @JsonProperty("currentMessage") String currentMessage,
            @JsonProperty("targetRole") String targetRole,
            @JsonProperty("careerGoal") String careerGoal,
            @JsonProperty("resumeText") String resumeText,
            @JsonProperty("skills") String skills,
            @JsonProperty("candidateName") String candidateName,
            @JsonProperty("chatHistory") List<PythonChatMessage> chatHistory
    ) {}

    public record PythonChatMessage(
            String role,
            String content
    ) {}

    public record PythonCareerMentorResponse(
            String reply,
            List<String> suggestedFollowUps,
            String targetRole,
            String careerGoal
    ) {}
}
