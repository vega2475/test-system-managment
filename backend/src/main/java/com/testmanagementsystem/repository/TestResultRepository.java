package com.testmanagementsystem.repository;

import com.testmanagementsystem.entity.InviteToken;
import com.testmanagementsystem.entity.Test;
import com.testmanagementsystem.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByInviteToken(InviteToken token);
    List<TestResult> findAllByTest(Test test);
}
