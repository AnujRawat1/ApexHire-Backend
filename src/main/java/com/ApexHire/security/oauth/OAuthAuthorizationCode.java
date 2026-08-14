package com.ApexHire.security.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "oauth_authorization_codes")
public class OAuthAuthorizationCode {

    @Id
    private String id;

    private String code;
    private String userId;
    private Instant expiresAt;
    private boolean used;
}