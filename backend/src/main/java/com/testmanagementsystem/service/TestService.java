package com.testmanagementsystem.service;

import com.testmanagementsystem.dto.answer.AnswerRequest;
import com.testmanagementsystem.dto.question.QuestionRequest;
import com.testmanagementsystem.dto.question.QuestionResult;
import com.testmanagementsystem.dto.test.TestRequest;
import com.testmanagementsystem.dto.test.TestResultResponse;
import com.testmanagementsystem.entity.*;
import com.testmanagementsystem.exception.TestNotFoundException;
import com.testmanagementsystem.exception.TestServiceException;
import com.testmanagementsystem.mapper.TestMapper;
import com.testmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final TestResultRepository testResultRepository;
    private final PartialTestResultRepository partialTestResultRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final InviteTokenService inviteTokenService;

    @Autowired
    public TestService(TestRepository testRepository, QuestionRepository questionRepository,
                       AnswerRepository answerRepository, UserRepository userRepository,
                       TestResultRepository testResultRepository, PartialTestResultRepository partialTestResultRepository,
                       InviteTokenRepository inviteTokenRepository, InviteTokenService inviteTokenService) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.testResultRepository = testResultRepository;
        this.partialTestResultRepository = partialTestResultRepository;
        this.inviteTokenRepository = inviteTokenRepository;
        this.inviteTokenService = inviteTokenService;
    }

    public void createTest(TestRequest testRequest) throws TestServiceException {
        Test test = new Test();
        test.setName(testRequest.getName());
        User user = userRepository.findById(testRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        test.setUser(user);

        for (QuestionRequest questionRequest : testRequest.getQuestions()) {
            Question question = new Question();
            question.setQuestionText(questionRequest.getText());
            question.setTest(test);

            for (AnswerRequest answerRequest : questionRequest.getAnswers()) {
                Answer answer = new Answer();
                answer.setAnswerText(answerRequest.getText());
                answer.setCorrect(answerRequest.isCorrect());
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            }
            test.getQuestions().add(question);
        }

        Test savedTest = testRepository.save(test);
        for (Question question : test.getQuestions()) {
            question.setTest(savedTest);
            Question savedQuestion = questionRepository.save(question);
            for (Answer answer : question.getAnswers()) {
                answer.setQuestion(savedQuestion);
                answerRepository.save(answer);
            }
        }
    }

    public void updateTest(Long id, TestRequest updatedTest) throws TestServiceException {
        Test existingTest = findById(id);

        if (existingTest == null) {
            throw new TestNotFoundException("Test not found with id" + id);
        }

        existingTest.setName(updatedTest.getName());

        List<Question> existingQuestions = existingTest.getQuestions();

        for (QuestionRequest updatedQuestion : updatedTest.getQuestions()) {
            Question question;
            question = findQuestionById(existingQuestions, updatedQuestion.getId());
            if (question != null) {
                question.setQuestionText(updatedQuestion.getText());
            }

            assert question != null;
            List<Answer> existingAnswers = question.getAnswers();

            for (AnswerRequest updatedAnswer : updatedQuestion.getAnswers()) {
                Answer answer;
                if (updatedAnswer.getId() != null) {
                    answer = findAnswerById(existingAnswers, updatedAnswer.getId());
                    if (answer != null) {
                        answer.setAnswerText(updatedAnswer.getText());
                        answer.setCorrect(updatedAnswer.isCorrect());
                    }
                }
            }
        }

        save(existingTest);
    }

    public ResponseEntity<?> getTestResult(Long userId) {
        List<Test> tests = testRepository.findByUserId(userId);

        if (tests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tests found for this user");
        }

        List<TestResultResponse> resultResponses = new ArrayList<>();

        for (Test test : tests) {
            List<TestResult> testResults = testResultRepository.findAllByTest(test);

            for (TestResult testResult : testResults) {
                List<PartialTestResult> partialResults = partialTestResultRepository.findByTestResultId(testResult);

                TestResultResponse response = new TestResultResponse();
                response.setName(testResult.getName());
                response.setSurname(testResult.getSurname());
                response.setEmail(testResult.getEmail());
                response.setResult(testResult.getTestResult());
                response.setAge(testResult.getAge());
                response.setId(testResult.getId());

                List<QuestionResult> questionResults = partialResults.stream().map(partialResult -> {
                    QuestionResult questionResult = new QuestionResult();
                    questionResult.setQuestion(partialResult.getQuestionId().getQuestionText());
                    questionResult.setSelectedAnswer(partialResult.getAnswer().getAnswerText());
                    questionResult.setCorrect(partialResult.getCorrect());
                    return questionResult;
                }).collect(Collectors.toList());

                response.setQuestionResults(questionResults);
                resultResponses.add(response);
            }
        }

        return ResponseEntity.ok(resultResponses);
    }


    public void deleteTest(Test test) {
        testRepository.delete(test);
    }

    public String generateInviteLink(Long id) {
        Test test = testRepository.findById(id).orElse(null);
        assert test != null;
        LocalDateTime localDateTime = test.getExpirationDate();
        InviteToken inviteToken = inviteTokenService.createInviteToken(id);
        inviteToken.setExpirationDate(localDateTime);
        inviteTokenRepository.save(inviteToken);
        return String.format("%s/invite/register/%s", getBaseUrl(), inviteToken.getToken());
    }

    public List<TestRequest> getAllTestForCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        List<Test> tests = testRepository.findByUserId(user.getId());

        return tests.stream()
                .map(TestMapper::toTestDTO)
                .collect(Collectors.toList());
    }

    @EntityGraph(attributePaths = {"questions.answers"})
    public Test findById(Long id) {
        return testRepository.findById(id).orElse(null);
    }

    public void save(Test test) {
        testRepository.save(test);
    }

    private Question findQuestionById(List<Question> questions, Long id) {
        return questions.stream().filter(question -> question.getId().equals(id)).findFirst().orElse(null);
    }

    private Answer findAnswerById(List<Answer> answers, Long id) {
        return answers.stream().filter(answer -> answer.getId().equals(id)).findFirst().orElse(null);
    }

    private String getBaseUrl() {
        return "http://localhost:3000";
    }
}