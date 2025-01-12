package com.testmanagementsystem.dto.test;

import com.testmanagementsystem.dto.question.QuestionResult;
import lombok.Data;

import java.util.List;

@Data
public class TestResultResponse {
    private String name;
    private String surname;
    private String email;
    private double result;
    private List<QuestionResult> questionResults;
    private Integer age;
    private Long id;
}
