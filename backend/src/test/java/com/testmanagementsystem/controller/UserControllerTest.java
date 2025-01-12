package com.testmanagementsystem.controller;

import com.testmanagementsystem.dto.auth.AuthResponse;
import com.testmanagementsystem.dto.email.EmailChangeRequest;
import com.testmanagementsystem.dto.user.UserLoginRequest;
import com.testmanagementsystem.dto.user.UserRegistrationRequest;
import com.testmanagementsystem.entity.User;
import com.testmanagementsystem.entity.VerificationToken;
import com.testmanagementsystem.repository.UserRepository;
import com.testmanagementsystem.repository.VerificationTokenRepository;
import com.testmanagementsystem.security.JwtUtil;
import com.testmanagementsystem.service.EmailService;
import com.testmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ShouldRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(null);

        ResponseEntity<?> response = userController.registerUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void loginUser_ShouldReturnAuthResponse() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User("test@example.com", "encodedPassword");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(user);
        when(userDetailsService.loadUserByUsername(request.getEmail())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwtToken");
        user.setEnabled(true);

        ResponseEntity<AuthResponse> response = (ResponseEntity<AuthResponse>) userController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwtToken", response.getBody().getJwt());
    }

    @Test
    void confirmRegistration_ShouldConfirmRegistration() {
        String token = "validToken";
        User user = new User("test@example.com", "password");
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(verificationTokenRepository.findByToken(token)).thenReturn(verificationToken);

        ResponseEntity<?> response = userController.confirmRegistration(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email confirmed successfully. You can now log in.", response.getBody());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void getInfo_ShouldReturnUserInfo() {
        User user = new User("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mock(UserDetails.class));
        when(((UserDetails) authentication.getPrincipal()).getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<User> response = (ResponseEntity<User>) userController.getInfo(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void confirmChangingEmail_ShouldConfirmChangingEmail() {
        String token = "validToken";
        User user = new User("new@example.com", "password");
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(verificationTokenRepository.findByToken(token)).thenReturn(verificationToken);

        ResponseEntity<?> response = userController.confirmChangingEmail(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email change confirmed successfully. You can now log in.", response.getBody());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
    }
}
