package com.testmanagementsystem.controller;

import com.testmanagementsystem.dto.test.PartialTestResultRequest;
import com.testmanagementsystem.dto.test.TestResultRequest;
import com.testmanagementsystem.dto.test.TestSubmissionRequest;
import com.testmanagementsystem.service.InviteTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

class InviteControllerTest {

    @Mock
    private InviteTokenService inviteTokenService;

    @InjectMocks
    private InviteController inviteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTestDetails_ShouldReturnTestDetails() {
        String token = "test-token";
        ResponseEntity<?> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(inviteTokenService.getTestForInviteUser(eq(token))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<?> response = inviteController.getTestDetails(token);

        assertEquals(expectedResponse, response);
    }

    @Test
    void getTestDetails_ShouldReturnErrorResponse() {
        String token = "test-token";
        String errorMessage = "Error fetching test details";

        when(inviteTokenService.getTestForInviteUser(eq(token)))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = inviteController.getTestDetails(token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error fetching test details", response.getBody());
    }

    @Test
    void startTest_ShouldStartTest() {
        String token = "test-token";
        TestResultRequest testResultRequest = new TestResultRequest();
        ResponseEntity<?> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        given(inviteTokenService.startTest(eq(token), any(TestResultRequest.class))).willReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<?> response = inviteController.startTest(token, testResultRequest);

        assertEquals(expectedResponse, response);
    }

    @Test
    void startTest_ShouldReturnErrorResponse() {
        String token = "test-token";
        TestResultRequest testResultRequest = new TestResultRequest();
        String errorMessage = "Error starting the test";

        when(inviteTokenService.startTest(eq(token), any(TestResultRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = inviteController.startTest(token, testResultRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error starting the test", response.getBody());
    }

    @Test
    void savePartialResult_ShouldSavePartialResult() {
        Long questionId = 1L;
        PartialTestResultRequest request = new PartialTestResultRequest();
        ResponseEntity<?> expectedResponse =new ResponseEntity<>(HttpStatus.OK);

        when(inviteTokenService.savePartialTestResult(eq(questionId), any(PartialTestResultRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<?> response = inviteController.savePartialResult(questionId, request);

        assertEquals(expectedResponse, response);
    }

    @Test
    void savePartialResult_ShouldReturnErrorResponse() {
        Long questionId = 1L;
        PartialTestResultRequest request = new PartialTestResultRequest();
        String errorMessage = "Error saving partial result";

        when(inviteTokenService.savePartialTestResult(eq(questionId), any(PartialTestResultRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = inviteController.savePartialResult(questionId, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void submitTest_ShouldSubmitTest() {
        String token = "test-token";
        TestSubmissionRequest testSubmissionRequest = new TestSubmissionRequest();
        ResponseEntity<?> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(inviteTokenService.submitTest(eq(token), any(TestSubmissionRequest.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<?> response = inviteController.submitTest(token, testSubmissionRequest);

        assertEquals(expectedResponse, response);
    }

    @Test
    void submitTest_ShouldReturnErrorResponse() {
        String token = "test-token";
        TestSubmissionRequest testSubmissionRequest = new TestSubmissionRequest();
        String errorMessage = "Error";

        when(inviteTokenService.submitTest(eq(token), any(TestSubmissionRequest.class)))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = inviteController.submitTest(token, testSubmissionRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error submitting test: Error", response.getBody());
    }
}