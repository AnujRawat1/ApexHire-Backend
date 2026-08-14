package com.ApexHire.security.oauth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OAuthAuthorizationCodeRepository extends MongoRepository<OAuthAuthorizationCode, String> {

    Optional<OAuthAuthorizationCode> findByCode(String code);
}