package com.testmanagementsystem.dto.test;

import com.testmanagementsystem.dto.question.QuestionRequest;
import lombok.Data;

import java.util.List;

@Data
public class TestRequest {
    private Long id;
    private String name;
    private Long userId;
    private List<QuestionRequest> questions;
}
