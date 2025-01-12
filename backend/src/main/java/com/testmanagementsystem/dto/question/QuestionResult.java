package com.testmanagementsystem.dto.question;

import lombok.Data;

@Data
public class QuestionResult {
    private String question;
    private String selectedAnswer;
    private Boolean correct;
}
