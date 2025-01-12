package com.testmanagementsystem.repository;

import com.testmanagementsystem.entity.Question;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @NotNull
    Optional<Question> findById (@NotNull Long id);
}
