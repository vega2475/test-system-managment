package com.testmanagementsystem.service;

import com.testmanagementsystem.dto.invite.InviteTokenRequest;
import com.testmanagementsystem.dto.test.PartialTestResultRequest;
import com.testmanagementsystem.dto.test.TestRequest;
import com.testmanagementsystem.dto.test.TestResultRequest;
import com.testmanagementsystem.dto.test.TestSubmissionRequest;
import com.testmanagementsystem.entity.*;
import com.testmanagementsystem.exception.TestNotFoundException;
import com.testmanagementsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InviteTokenServiceTest {

    @Mock
    private InviteTokenRepository inviteTokenRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private PartialTestResultRepository partialTestResultRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InviteTokenService inviteTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInviteToken_ShouldCreateAndSaveInviteToken() {
        Long testId = 1L;
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        test.setId(testId);
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(inviteTokenRepository.save(any(InviteToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InviteToken inviteToken = inviteTokenService.createInviteToken(testId);

        assertNotNull(inviteToken.getToken());
        assertEquals(test, inviteToken.getTest());
        verify(inviteTokenRepository).save(inviteToken);
    }

    @Test
    void createInviteToken_ShouldThrowException_WhenTestNotFound() {
        Long testId = 1L;
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(TestNotFoundException.class, () -> inviteTokenService.createInviteToken(testId));
    }

    @Test
    void getTestForInviteUser_ShouldReturnTestDTO_WhenTokenIsValidAndTestResultNotCompleted() {
        String token = "test-token";
        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setExpirationDate(LocalDateTime.now().plusDays(1));
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        inviteToken.setTest(test);

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));
        when(testResultRepository.findByInviteToken(inviteToken)).thenReturn(Optional.empty());

        ResponseEntity<?> response = inviteTokenService.getTestForInviteUser(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TestRequest);
    }

    @Test
    void getTestForInviteUser_ShouldReturnBadRequest_WhenTokenIsExpired() {
        String token = "test-token";
        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setExpirationDate(LocalDateTime.now().minusDays(1));

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));

        ResponseEntity<?> response = inviteTokenService.getTestForInviteUser(token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invite token has expired", response.getBody());
    }

    @Test
    void startTest_ShouldCreateTestResultAndReturnTestDTO() {
        String token = "test-token";
        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setExpirationDate(LocalDateTime.now().plusDays(1));
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        inviteToken.setTest(test);
        TestResultRequest testResultRequest = new TestResultRequest();

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));
        when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = inviteTokenService.startTest(token, testResultRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TestRequest);
        verify(testResultRepository).save(any(TestResult.class));
    }

    @Test
    void updateExpiration_ShouldUpdateExpirationDateForAllTokens() {
        Long testId = 1L;
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        test.setId(testId);
        InviteTokenRequest inviteTokenRequest = new InviteTokenRequest();
        inviteTokenRequest.setExpirationDate(LocalDateTime.now());
        InviteToken token1 = new InviteToken();
        InviteToken token2 = new InviteToken();
        List<InviteToken> tokens = Arrays.asList(token1, token2);

        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(inviteTokenRepository.findAllByTestId(testId)).thenReturn(tokens);

        ResponseEntity<?> response = inviteTokenService.updateExpiration(testId, inviteTokenRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inviteTokenRepository, times(1)).saveAll(tokens);
        assertEquals(inviteTokenRequest.getExpirationDate().plusHours(3), token1.getExpirationDate());
    }

    @Test
    void savePartialTestResult_ShouldSavePartialResult() {
        Long questionId = 1L;
        Long answerId = 1L;
        String token = "test-token";
        PartialTestResultRequest partialTestResultRequest = new PartialTestResultRequest();
        partialTestResultRequest.setToken(token);
        partialTestResultRequest.setAnswer(answerId);
        InviteToken inviteToken = new InviteToken();
        TestResult testResult = new TestResult();
        Question question = new Question();
        Answer answer = new Answer();
        answer.setCorrect(true);

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));
        when(testResultRepository.findByInviteToken(inviteToken)).thenReturn(Optional.of(testResult));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(partialTestResultRepository.findByTestResultIdAndQuestionId(testResult, question)).thenReturn(Optional.empty());

        ResponseEntity<?> response = inviteTokenService.savePartialTestResult(questionId, partialTestResultRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(partialTestResultRepository).save(any(PartialTestResult.class));
    }

    @Test
    void submitTest_ShouldCalculateAndSaveTestResult() {
        String token = "test-token";
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        Question question1 = new Question();
        question1.setId(1L);
        Answer correctAnswer1 = new Answer();
        correctAnswer1.setId(1L);
        correctAnswer1.setCorrect(true);
        question1.setAnswers(Collections.singletonList(correctAnswer1));
        test.setQuestions(Collections.singletonList(question1));

        InviteToken inviteToken = new InviteToken();
        inviteToken.setTest(test);

        TestResult testResult = new TestResult();
        testResult.setTest(test);

        TestSubmissionRequest submissionRequest = new TestSubmissionRequest();
        submissionRequest.setAnswers(Collections.singletonMap(1L, 1L));

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));
        when(testResultRepository.findByInviteToken(inviteToken)).thenReturn(Optional.of(testResult));

        ResponseEntity<?> response = inviteTokenService.submitTest(token, submissionRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testResultRepository).save(testResult);
        verify(emailService).sendSimpleMessage(eq(testResult.getEmail()), eq("Test result"), anyString());
    }
}