package com.ApexHire.resume.client;

import com.ApexHire.resume.exception.PythonServiceException;
import com.ApexHire.resume.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class PythonResumeClient {

    private final WebClient webClient;

    @Value("${python.resume-service.url}")
    private String pythonServiceUrl;

    @Value("${python.resume-service.api-key}")
    private String pythonServiceApiKey;

    @Value("${python.resume-service.timeout:120s}")
    private Duration timeout;

    public PythonResumeClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public AnalysisResult analyzeResume(PythonAnalysisRequest request) {
        log.info("Sending resume analysis request to Python service: targetRole={}, experienceLevel={}",
                request.targetRole(), request.experienceLevel());

        try {
            return webClient.post()
                    .uri(pythonServiceUrl + "/api/v1/resume/analyze")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + pythonServiceApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AnalysisResult.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(throwable -> isTransientError(throwable)))
                    .doOnError(error -> log.error("Python service error", error))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Python service returned error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw handleWebClientException(e);
        } catch (Exception e) {
            log.error("Failed to call Python service", e);
            throw new PythonServiceException("Failed to analyze resume: " + e.getMessage(), e);
        }
    }

    private boolean isTransientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int status = ex.getStatusCode().value();
            return status >= 500 && status < 600;
        }
        return false;
    }

    private PythonServiceException handleWebClientException(WebClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> new PythonServiceException("Invalid request to Python service");
            case 401 -> new PythonServiceException("Python service authentication failed");
            case 403 -> new PythonServiceException("Python service access denied");
            case 404 -> new PythonServiceException("Python service endpoint not found");
            case 429 -> new PythonServiceException("Python service rate limit exceeded");
            case 500, 502, 503, 504 -> new PythonServiceException("Python service temporarily unavailable");
            default -> new PythonServiceException("Python service error: " + e.getMessage());
        };
    }

    public record PythonAnalysisRequest(
            String resumeText,
            String targetRole,
            String experienceLevel,
            String jobDescription
    ) {}
}
