package com.ApexHire.careermentor.repository;

import com.ApexHire.careermentor.document.CareerMentorSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerMentorSessionRepository extends MongoRepository<CareerMentorSession, String> {

    Page<CareerMentorSession> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    Optional<CareerMentorSession> findByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);
}
