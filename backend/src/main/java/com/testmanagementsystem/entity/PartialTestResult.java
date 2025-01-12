package com.testmanagementsystem.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class PartialTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_result_id")
    private TestResult testResultId;

    @OneToOne
    @JoinColumn(name = "question_id")
    private Question questionId;

    @OneToOne
    @JoinColumn(name = "answer_id")
    private Answer answer;

    private Boolean correct;
}