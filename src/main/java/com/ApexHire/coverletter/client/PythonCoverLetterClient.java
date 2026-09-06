package com.ApexHire.coverletter.client;

import com.ApexHire.resume.exception.PythonServiceException;
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
public class PythonCoverLetterClient {

    private final WebClient webClient;

    @Value("${python.resume-service.url}")
    private String pythonServiceUrl;

    @Value("${python.resume-service.api-key}")
    private String pythonServiceApiKey;

    @Value("${python.resume-service.timeout:120s}")
    private Duration timeout;

    public PythonCoverLetterClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public PythonCoverLetterResponse generateCoverLetter(PythonCoverLetterRequest request) {
        log.info("Sending cover letter generation request to Python service: role={}, company={}, tone={}",
                request.targetRole(), request.companyName(), request.tone());

        try {
            return webClient.post()
                    .uri(pythonServiceUrl + "/api/cover-letter/generate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + pythonServiceApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PythonCoverLetterResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(this::isTransientError))
                    .doOnError(error -> log.error("Python cover letter service error", error))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Python service returned error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw handleWebClientException(e);
        } catch (Exception e) {
            log.error("Failed to call Python cover letter service", e);
            throw new PythonServiceException("Failed to generate cover letter: " + e.getMessage(), e);
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
            case 400 -> new PythonServiceException("Invalid request to Python cover letter service");
            case 401 -> new PythonServiceException("Python cover letter service authentication failed");
            case 403 -> new PythonServiceException("Python cover letter service access denied");
            case 404 -> new PythonServiceException("Python cover letter service endpoint not found");
            case 429 -> new PythonServiceException("Python cover letter service rate limit exceeded");
            case 500, 502, 503, 504 -> new PythonServiceException("Python cover letter service temporarily unavailable");
            default -> new PythonServiceException("Python cover letter service error: " + e.getMessage());
        };
    }

    public record PythonCoverLetterRequest(
            String candidateName,
            String candidateEmail,
            String targetRole,
            String companyName,
            String jobDescription,
            String resumeText,
            String skills,
            String additionalInfo,
            String tone
    ) {}

    public record PythonCoverLetterResponse(
            String content,
            List<String> keyHighlights,
            String targetRole,
            String companyName,
            String tone
    ) {}
}
