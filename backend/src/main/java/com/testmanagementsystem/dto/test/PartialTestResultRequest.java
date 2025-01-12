package com.testmanagementsystem.dto.test;

import lombok.Data;

@Data
public class PartialTestResultRequest {
    private Long answer;
    private String token;
}
