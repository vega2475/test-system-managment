package com.testmanagementsystem.dto.test;

import lombok.Data;

import java.util.Map;

@Data
public class TestSubmissionRequest {
    private Map<Long, Long> answers;
}
