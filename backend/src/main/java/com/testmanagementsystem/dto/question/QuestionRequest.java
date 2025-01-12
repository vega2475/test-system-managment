package com.testmanagementsystem.dto.question;

import com.testmanagementsystem.dto.answer.AnswerRequest;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private Long id;
    private String text;
    private List<AnswerRequest> answers;
    private String correctAnswer;
    private AnswerRequest selectedAnswer;
}
