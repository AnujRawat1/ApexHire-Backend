package com.ApexHire.resume.repository;

import com.ApexHire.resume.document.ResumeReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeReportRepository extends MongoRepository<ResumeReport, String> {

    Optional<ResumeReport> findByIdAndUserId(String id, String userId);

    boolean existsByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);

    Page<ResumeReport> findAllByUserId(String userId, Pageable pageable);

    @Query("{ 'userId': ?0, 'targetRole': ?1 }")
    Page<ResumeReport> findByUserIdAndTargetRole(String userId, String targetRole, Pageable pageable);

    @Query("{ 'userId': ?0, 'experienceLevel': ?1 }")
    Page<ResumeReport> findByUserIdAndExperienceLevel(String userId, String experienceLevel, Pageable pageable);

    @Query("{ 'userId': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'fileName': { $regex: ?1, $options: 'i' } }, { 'targetRole': { $regex: ?1, $options: 'i' } } ] }")
    Page<ResumeReport> findByUserIdAndSearch(String userId, String search, Pageable pageable);
}
