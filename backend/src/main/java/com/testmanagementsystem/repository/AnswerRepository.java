package com.testmanagementsystem.repository;

import com.testmanagementsystem.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
