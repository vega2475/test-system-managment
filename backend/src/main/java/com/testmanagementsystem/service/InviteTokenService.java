package com.testmanagementsystem.service;

import com.testmanagementsystem.dto.invite.InviteTokenRequest;
import com.testmanagementsystem.dto.test.PartialTestResultRequest;
import com.testmanagementsystem.dto.test.TestRequest;
import com.testmanagementsystem.dto.test.TestResultRequest;
import com.testmanagementsystem.dto.test.TestSubmissionRequest;
import com.testmanagementsystem.entity.*;
import com.testmanagementsystem.exception.TestNotFoundException;
import com.testmanagementsystem.mapper.TestMapper;
import com.testmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InviteTokenService {

    private final InviteTokenRepository inviteTokenRepository;

    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final PartialTestResultRepository partialTestResultRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final EmailService emailService;

    public InviteToken createInviteToken(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new TestNotFoundException("Test not found"));

        InviteToken inviteToken = new InviteToken();
        inviteToken.setTest(test);
        inviteToken.setToken(UUID.randomUUID().toString());

        return inviteTokenRepository.save(inviteToken);
    }

    public ResponseEntity<?> getTestForInviteUser(String token) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invite token not found"));

        if (inviteToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invite token has expired");
        }

        Optional<TestResult> existingTestResult = testResultRepository.findByInviteToken(inviteToken);

        if (existingTestResult.isPresent()) {
            TestResult testResult = existingTestResult.get();
            if (testResult.getTestResult() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Test already completed");
                response.put("result", testResult.getTestResult());

                return ResponseEntity.ok(response);
            }

            List<PartialTestResult> partialResults = partialTestResultRepository.findByTestResultId(testResult);
            TestRequest testDTO = TestMapper.toTestDTO(testResult.getTest(), partialResults);

            return ResponseEntity.ok(testDTO);
        } else {
            Test test = inviteToken.getTest();
            TestRequest testDTO = TestMapper.toTestDTO(test);
            return ResponseEntity.ok(testDTO);
        }
    }

    public ResponseEntity<?> startTest(String token, TestResultRequest testResultRequest) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invite token not found"));

        if (inviteToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invite token has expired");
        }

        Test test = inviteToken.getTest();

        TestResult testResult = new TestResult();
        testResult.setTest(test);
        testResult.setName(testResultRequest.getName());
        testResult.setEmail(testResultRequest.getEmail());
        testResult.setSurname(testResultRequest.getSurname());
        testResult.setAge(testResultRequest.getAge()); // устанавливаем возраст
        testResult.setInviteToken(inviteToken);

        testResultRepository.save(testResult);

        TestRequest testDTO = TestMapper.toTestDTO(test);

        return ResponseEntity.ok(testDTO);
    }

    public ResponseEntity<?> updateExpiration(Long id, InviteTokenRequest inviteTokenRequest) {
        Test test = testRepository.findById(id).orElse(null);
        assert test != null;
        test.setExpirationDate(inviteTokenRequest.getExpirationDate().plusHours(3));
        List<InviteToken> tokens = inviteTokenRepository.findAllByTestId(id);

        if (tokens.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No invite tokens found for the provided test ID.");
        }

        LocalDateTime localDateTime = inviteTokenRequest.getExpirationDate();

        tokens.forEach(token -> token.setExpirationDate(localDateTime.plusHours(3)));

        inviteTokenRepository.saveAll(tokens);

        return ResponseEntity.ok().body("Expiration date updated successfully for all tokens.");
    }

    public ResponseEntity<?> savePartialTestResult(Long questionId, PartialTestResultRequest partialTestResultRequest) {
        Long answerId = partialTestResultRequest.getAnswer();
        InviteToken inviteToken = inviteTokenRepository.findByToken(partialTestResultRequest.getToken()).orElse(null);

        TestResult testResult = testResultRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Test result not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Optional<PartialTestResult> existingPartialResult =
                partialTestResultRepository.findByTestResultIdAndQuestionId(testResult, question);

        PartialTestResult partialTestResult;
        if (existingPartialResult.isPresent()) {
            partialTestResult = existingPartialResult.get();
        } else {
            partialTestResult = new PartialTestResult();
            partialTestResult.setTestResultId(testResult);
            partialTestResult.setQuestionId(question);
        }
        partialTestResult.setAnswer(answer);
        partialTestResult.setCorrect(answer.getCorrect());

        partialTestResultRepository.save(partialTestResult);

        return ResponseEntity.ok("Answer saved successfully");
    }

    public ResponseEntity<?> submitTest(String token, TestSubmissionRequest testSubmissionRequest) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invite token not found"));

        TestResult testResult = testResultRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Test result not found"));

        List<Question> questions = testResult.getTest().getQuestions();
        int correctAnswersCount = 0;

        for (Question question : questions) {
            Answer correctAnswer = question.getAnswers().stream()
                    .filter(Answer::getCorrect)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Correct answer not found"));

            Long userAnswerId = testSubmissionRequest.getAnswers().get(question.getId());
            Answer userAnswer = question.getAnswers().stream()
                    .filter(answer -> answer.getId().equals(userAnswerId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("User answer not found"));

            boolean isCorrect = correctAnswer.getId().equals(userAnswerId);
            if (isCorrect) {
                correctAnswersCount++;
            }

            PartialTestResult partialResult = new PartialTestResult();
            partialResult.setTestResultId(testResult);
            partialResult.setQuestionId(question);
            partialResult.setAnswer(userAnswer);
            partialResult.setCorrect(isCorrect);

            partialTestResultRepository.save(partialResult);
        }

        double result = (double) correctAnswersCount / questions.size() * 100;
        testResult.setTestResult(result);
        testResultRepository.save(testResult);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test already completed");
        response.put("result", result);
        sendTestResult(testResult.getEmail(), result);
        return ResponseEntity.ok(response);
    }

    private void sendTestResult(String email, double result) {
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedResult = df.format(result);
        emailService.sendSimpleMessage(email, "Test result", "Your result is " + formattedResult + "%");
    }
}
