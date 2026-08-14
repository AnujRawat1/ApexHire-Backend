package com.ApexHire.security.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class GithubEmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getPrimaryVerifiedEmail(String accessToken) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github+json");

        RequestEntity<Void> request = new RequestEntity<>(
                headers,
                HttpMethod.GET,
                URI.create("https://api.github.com/user/emails")
        );

        ResponseEntity<GithubEmail[]> response = restTemplate.exchange(request, GithubEmail[].class);

        GithubEmail[] emails = response.getBody();

        if (emails == null || emails.length == 0) {
            return null;
        }

        // Prefer primary + verified email
        return Arrays.stream(emails)
                .filter(email ->
                        Boolean.TRUE.equals(email.getPrimary())
                                && Boolean.TRUE.equals(email.getVerified())
                )
                .map(GithubEmail::getEmail)
                .findFirst()
                .orElseGet(() ->
                        Arrays.stream(emails)
                                .filter(email ->
                                        Boolean.TRUE.equals(email.getVerified())
                                )
                                .map(GithubEmail::getEmail)
                                .findFirst()
                                .orElse(null)
                );
    }

    @Data
    private static class GithubEmail {

        private String email;

        private Boolean primary;

        private Boolean verified;

        @JsonProperty("visibility")
        private String visibility;
    }
}