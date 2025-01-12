package com.testmanagementsystem.service;

import com.testmanagementsystem.entity.User;
import com.testmanagementsystem.entity.VerificationToken;
import com.testmanagementsystem.repository.UserRepository;
import com.testmanagementsystem.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void testLoadUserByUsername_UserFound() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");

        when(userRepository.findByEmail(email)).thenReturn(user);

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.save(user)).thenReturn(user);
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.registerUser(user);

        verify(userRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendSimpleMessage(eq(user.getEmail()), eq("Confirm your email"), emailCaptor.capture());

        String emailContent = emailCaptor.getValue();
        assertTrue(emailContent.contains("http://localhost:8081/api/auth/confirm?token="));
    }

    @Test
    void testIsNewEmailUsed_EmailExists() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);

        boolean result = userService.isNewEmailUsed(email);

        assertTrue(result);
    }

    @Test
    void testIsNewEmailUsed_EmailNotExists() {
        String email = "new@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        boolean result = userService.isNewEmailUsed(email);

        assertFalse(result);
    }

    @Test
    void testGenerateVerificationToken() {
        User user = new User();
        user.setEmail("test@example.com");

        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = userService.generateVerificationToken(user);

        assertNotNull(token);
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }
}