package com.testmanagementsystem.controller;

import com.testmanagementsystem.dto.invite.InviteTokenRequest;
import com.testmanagementsystem.dto.test.TestRequest;
import com.testmanagementsystem.entity.TestResult;
import com.testmanagementsystem.exception.TestNotFoundException;
import com.testmanagementsystem.exception.TestServiceException;
import com.testmanagementsystem.service.InviteTokenService;
import com.testmanagementsystem.service.TestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TestControllerTest {

    @Mock
    private TestService testService;

    @Mock
    private InviteTokenService inviteTokenService;

    @InjectMocks
    private TestController testController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTest_ShouldCreateTest() {
        TestRequest testRequest = new TestRequest();

        doNothing().when(testService).createTest(testRequest);

        ResponseEntity<?> response = testController.createTest(testRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test created successfully", response.getBody());
    }

    @Test
    void getAllTests_ShouldReturnAllTests() {
        Authentication authentication = mock(Authentication.class);
        List<TestRequest> testRequests = Collections.singletonList(new TestRequest());

        when(testService.getAllTestForCurrentUser(authentication)).thenReturn(testRequests);

        ResponseEntity<List<TestRequest>> response = testController.getAllTests(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRequests, response.getBody());
    }

    @Test
    void updateTest_ShouldUpdateTest() {
        Long testId = 1L;
        TestRequest updatedTest = new TestRequest();

        doNothing().when(testService).updateTest(eq(testId), any(TestRequest.class));

        ResponseEntity<?> response = testController.updateTest(testId, updatedTest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test updated successfully", response.getBody());
    }

    @Test
    void updateTest_ShouldReturnNotFound_WhenTestNotFound() {
        Long testId = 1L;
        TestRequest updatedTest = new TestRequest();

        doThrow(new TestNotFoundException("Test not found")).when(testService).updateTest(eq(testId), any(TestRequest.class));

        ResponseEntity<?> response = testController.updateTest(testId, updatedTest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Test not found", response.getBody());
    }

    @Test
    void deleteTest_ShouldDeleteTest() {
        Long testId = 1L;

        doNothing().when(testService).deleteTest(any());

        ResponseEntity<?> response = testController.deleteTest(testId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test deleted successfully", response.getBody());
    }

    @Test
    void deleteTest_ShouldReturnNotFound_WhenTestNotFound() {
        Long testId = 1L;

        doThrow(new TestNotFoundException("Test not found")).when(testService).deleteTest(any());

        ResponseEntity<?> response = testController.deleteTest(testId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Test not found", response.getBody());
    }

    @Test
    void generateInviteLink_ShouldGenerateInviteLink() {
        Long testId = 1L;
        String inviteLink = "http://localhost:8081/invite";

        when(testService.generateInviteLink(testId)).thenReturn(inviteLink);

        ResponseEntity<?> response = testController.generateInviteLink(testId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonMap("inviteLink", inviteLink), response.getBody());
    }

    @Test
    void generateInviteLink_ShouldReturnNotFound_WhenTestNotFound() {
        Long testId = 1L;

        when(testService.generateInviteLink(testId)).thenThrow(new TestNotFoundException("Test not found"));

        ResponseEntity<?> response = testController.generateInviteLink(testId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Test not found", response.getBody());
    }
}