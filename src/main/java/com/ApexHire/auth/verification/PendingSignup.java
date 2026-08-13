package com.ApexHire.auth.verification;

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
@Document(collection = "pending_signups")
public class PendingSignup {

    @Id
    private String id;

    private String name;

    private String email;

    private String passwordHash;

    private String codeHash;

    private Instant createdAt;

    private Instant expiresAt;
}