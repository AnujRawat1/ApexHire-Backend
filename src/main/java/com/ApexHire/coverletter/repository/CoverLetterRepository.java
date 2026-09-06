package com.ApexHire.coverletter.repository;

import com.ApexHire.coverletter.document.CoverLetter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoverLetterRepository extends MongoRepository<CoverLetter, String> {

    Optional<CoverLetter> findByIdAndUserId(String id, String userId);

    Page<CoverLetter> findAllByUserId(String userId, Pageable pageable);

    boolean existsByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);
}
