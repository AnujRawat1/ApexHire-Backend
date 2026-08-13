package com.ApexHire.auth.verification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PendingSignupRepository extends MongoRepository<PendingSignup, String> {

    Optional<PendingSignup> findByEmail(String email);
    void deleteByEmail(String email);

}