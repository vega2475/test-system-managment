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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Validated
public class UserController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        if (userRepository.findByEmail(userRegistrationRequest.getEmail()) != null) {
            throw new RuntimeException("User already exists");
        }
        User user = new User(userRegistrationRequest.getEmail(), passwordEncoder.encode(userRegistrationRequest.getPassword()));
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginRequest.getEmail(), userLoginRequest.getPassword())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
        final User user = userRepository.findByEmail(userLoginRequest.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Email is not verified");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmRegistration(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Email confirmed successfully. You can now log in.");
    }



    @GetMapping("/me")
    public ResponseEntity<?> getInfo(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()); //TODO
        return ResponseEntity.ok(user);
    }

    @PostMapping("/changeEmail")
    public ResponseEntity<?> changeEmail(@RequestBody @Valid EmailChangeRequest emailChangeRequest, Authentication authentication) {
        String newEmail = emailChangeRequest.getNewEmail();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        boolean isUsed = userService.isNewEmailUsed(newEmail);
        if (!isUsed) {
            user.setEnabled(false);
            user.setEmail(newEmail);
            userRepository.save(user);
            String confirmationUrl = "http://localhost:8081/api/auth/confirmChangingEmail?token=" + userService.generateVerificationToken(user);
            emailService.sendSimpleMessage(newEmail, "Email Change Confirmation.",
                    "Please confirm your email change by clicking on the link provided.\n" + confirmationUrl);
            return ResponseEntity.ok("Email successfully updated. Please check your new email for confirmation.");
        } else {
            return ResponseEntity.badRequest().body("Failed to update email. The new email might already be in use.");
        }
    }

    @GetMapping("/confirmChangingEmail")
    public ResponseEntity<?> confirmChangingEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        return ResponseEntity.ok("Email change confirmed successfully. You can now log in.");
    }
}