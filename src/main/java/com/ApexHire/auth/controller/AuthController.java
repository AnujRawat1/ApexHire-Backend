package com.ApexHire.auth.controller;

import com.ApexHire.auth.dto.*;
import com.ApexHire.auth.service.AuthService;
import com.ApexHire.auth.verification.EmailVerificationService;
import com.ApexHire.security.authentication.CustomUserDetailsService;
import com.ApexHire.security.jwt.JwtService;
import com.ApexHire.token.RefreshTokenService;
import com.ApexHire.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequestDto request) {

        authService.signUp(request);
        return ResponseEntity.ok("Verification code sent to your email");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        User user = emailVerificationService.verifyEmail(request.getEmail(), request.getCode());

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {

        emailVerificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok("Verification code resent successfully");

    }
}
