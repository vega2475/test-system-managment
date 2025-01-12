package com.testmanagementsystem.repository;

import com.testmanagementsystem.entity.PartialTestResult;
import com.testmanagementsystem.entity.Question;
import com.testmanagementsystem.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartialTestResultRepository extends JpaRepository<PartialTestResult, Long> {
    List<PartialTestResult> findByTestResultId(TestResult testResultId);
    Optional<PartialTestResult> findByTestResultIdAndQuestionId(TestResult testResultId, Question questionId);
}
