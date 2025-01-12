package com.testmanagementsystem.service;

import com.testmanagementsystem.entity.*;
import org.junit.jupiter.api.Test;
import com.testmanagementsystem.dto.answer.AnswerRequest;
import com.testmanagementsystem.dto.question.QuestionRequest;
import com.testmanagementsystem.dto.test.TestRequest;
import com.testmanagementsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestServiceTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InviteTokenService inviteTokenService;

    @Mock
    private InviteTokenRepository inviteTokenRepository;

    @InjectMocks
    private TestService testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTest_ShouldSaveTestAndQuestions() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        TestRequest testRequest = new TestRequest();
        testRequest.setName("Test 1");
        testRequest.setUserId(1L);

        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setText("Test");

        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setText("Test");
        answerRequest.setCorrect(true);

        questionRequest.setAnswers(List.of(answerRequest));
        testRequest.setQuestions(List.of(questionRequest));

        when(testRepository.save(any(com.testmanagementsystem.entity.Test.class))).thenReturn(new com.testmanagementsystem.entity.Test());

        testService.createTest(testRequest);

        verify(testRepository, times(1)).save(any(com.testmanagementsystem.entity.Test.class));
        verify(questionRepository, times(1)).save(any(Question.class));
        verify(answerRepository, times(1)).save(any(Answer.class));
    }

    @Test
    void generateInviteLink_ShouldReturnInviteLink() {
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        test.setId(1L);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken("testToken");
        when(inviteTokenService.createInviteToken(1L)).thenReturn(inviteToken);

        String inviteLink = testService.generateInviteLink(1L);

        assertEquals("http://localhost:3000/invite/register/testToken", inviteLink);
        verify(inviteTokenRepository, times(1)).save(inviteToken);
    }

    @Test
    void deleteTest_ShouldDeleteTest() {
        com.testmanagementsystem.entity.Test test = new com.testmanagementsystem.entity.Test();
        test.setId(1L);

        testService.deleteTest(test);

        verify(testRepository, times(1)).delete(test);
    }
}
