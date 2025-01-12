package com.testmanagementsystem.controller;

import com.testmanagementsystem.dto.test.PartialTestResultRequest;
import com.testmanagementsystem.dto.test.TestResultRequest;
import com.testmanagementsystem.dto.test.TestSubmissionRequest;
import com.testmanagementsystem.service.InviteTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invite/")
public class InviteController {

    private final InviteTokenService inviteTokenService;

    @GetMapping("/register/{token}")
    public ResponseEntity<?> getTestDetails(@PathVariable String token) {
        try {
            return inviteTokenService.getTestForInviteUser(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching test details");
        }
    }

    @PostMapping("/start-test/{token}")
    public ResponseEntity<?> startTest(@PathVariable String token, @RequestBody TestResultRequest testResultRequest) {
        try {
            return inviteTokenService.startTest(token, testResultRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting the test");
        }
    }

    @PostMapping("/partial-save/question/{questionId}")
    public ResponseEntity<?> savePartialResult(
            @PathVariable Long questionId,
            @RequestBody PartialTestResultRequest request) {

        try {
            return inviteTokenService.savePartialTestResult(questionId, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/submit-test/{token}")
    public ResponseEntity<?> submitTest(
            @PathVariable String token,
            @RequestBody TestSubmissionRequest testSubmissionRequest) {
        try {
            return inviteTokenService.submitTest(token, testSubmissionRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting test: " + e.getMessage());
        }
    }
}
